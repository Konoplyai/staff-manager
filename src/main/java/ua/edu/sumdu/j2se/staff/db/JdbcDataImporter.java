package ua.edu.sumdu.j2se.staff.db;

import ua.edu.sumdu.j2se.staff.io.CompanyData;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Імпорт підрозділів і працівників у PostgreSQL в одній транзакції:
 * якщо хоча б один запис не вдалося зберегти, база залишається без змін.
 */
public class JdbcDataImporter implements DataImporter {

    private static final String FIND_DEPARTMENT = "SELECT id FROM departments WHERE name = ?";
    private static final String INSERT_DEPARTMENT =
            "INSERT INTO departments (name, location, phone) VALUES (?, ?, ?) RETURNING id";
    private static final String UPDATE_DEPARTMENT =
            "UPDATE departments SET location = ?, phone = ? WHERE id = ?";
    private static final String FIND_EMPLOYEE = "SELECT id FROM employees WHERE email = ?";
    private static final String INSERT_EMPLOYEE =
            "INSERT INTO employees (last_name, first_name, middle_name, position, salary, bonus, hire_date, "
            + "email, phone, department_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_EMPLOYEE =
            "UPDATE employees SET last_name = ?, first_name = ?, middle_name = ?, position = ?, salary = ?, "
            + "bonus = ?, hire_date = ?, email = ?, phone = ?, department_id = ? WHERE id = ?";
    private static final String UPDATE_MANAGER =
            "UPDATE employees SET manager_id = (SELECT id FROM employees WHERE email = ?) WHERE email = ?";

    private final ConnectionManager connectionManager;

    /**
     * Створює імпортер.
     *
     * @param connectionManager джерело підключень
     */
    public JdbcDataImporter(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public ImportResult importData(CompanyData data) throws DataAccessException {
        ImportResult result = new ImportResult();
        try (Connection c = connectionManager.getConnection()) {
            c.setAutoCommit(false);
            try {
                Map<String, Integer> departmentIds = importDepartments(c, data, result);
                importEmployees(c, data, departmentIds, result);
                importManagers(c, data);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw DataAccessException.of("Імпорт скасовано", e);
        }
        return result;
    }

    private Map<String, Integer> importDepartments(Connection c, CompanyData data, ImportResult result)
            throws SQLException {
        Map<String, Integer> ids = new HashMap<>();
        try (PreparedStatement find = c.prepareStatement(FIND_DEPARTMENT);
             PreparedStatement insert = c.prepareStatement(INSERT_DEPARTMENT);
             PreparedStatement update = c.prepareStatement(UPDATE_DEPARTMENT)) {
            for (CompanyData.DepartmentItem d : data.getDepartments()) {
                Integer id = findId(find, d.name);
                if (id == null) {
                    insert.setString(1, d.name);
                    insert.setString(2, d.location);
                    insert.setString(3, d.phone);
                    try (ResultSet rs = insert.executeQuery()) {
                        rs.next();
                        id = rs.getInt(1);
                    }
                    result.departmentAdded();
                } else {
                    update.setString(1, d.location);
                    update.setString(2, d.phone);
                    update.setInt(3, id);
                    update.executeUpdate();
                    result.departmentUpdated();
                }
                ids.put(d.name, id);
            }
        }
        return ids;
    }

    private void importEmployees(Connection c, CompanyData data, Map<String, Integer> departmentIds,
                                 ImportResult result) throws SQLException {
        try (PreparedStatement findDepartment = c.prepareStatement(FIND_DEPARTMENT);
             PreparedStatement find = c.prepareStatement(FIND_EMPLOYEE);
             PreparedStatement insert = c.prepareStatement(INSERT_EMPLOYEE);
             PreparedStatement update = c.prepareStatement(UPDATE_EMPLOYEE)) {
            for (CompanyData.EmployeeItem e : data.getEmployees()) {
                Integer departmentId = null;
                if (e.department != null) {
                    departmentId = departmentIds.get(e.department);
                    if (departmentId == null) {
                        departmentId = findId(findDepartment, e.department);
                    }
                    if (departmentId == null) {
                        throw new SQLException("підрозділ \"" + e.department + "\" для працівника "
                                + e.lastName + " не знайдено ні у файлі, ні в базі");
                    }
                }
                Integer id = e.email == null ? null : findId(find, e.email);
                PreparedStatement ps = id == null ? insert : update;
                ps.setString(1, e.lastName);
                ps.setString(2, e.firstName);
                ps.setString(3, e.middleName);
                ps.setString(4, e.position);
                ps.setBigDecimal(5, e.salary);
                ps.setBigDecimal(6, e.bonus == null ? BigDecimal.ZERO : e.bonus);
                ps.setDate(7, Date.valueOf(e.hireDate));
                ps.setString(8, e.email);
                ps.setString(9, e.phone);
                if (departmentId == null) {
                    ps.setNull(10, Types.INTEGER);
                } else {
                    ps.setInt(10, departmentId);
                }
                if (id == null) {
                    insert.executeUpdate();
                    result.employeeAdded();
                } else {
                    update.setInt(11, id);
                    update.executeUpdate();
                    result.employeeUpdated();
                }
            }
        }
    }

    private void importManagers(Connection c, CompanyData data) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(UPDATE_MANAGER)) {
            for (CompanyData.EmployeeItem e : data.getEmployees()) {
                if (e.email != null && e.managerEmail != null) {
                    ps.setString(1, e.managerEmail);
                    ps.setString(2, e.email);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private static Integer findId(PreparedStatement find, String key) throws SQLException {
        find.setString(1, key);
        try (ResultSet rs = find.executeQuery()) {
            return rs.next() ? rs.getInt(1) : null;
        }
    }
}
