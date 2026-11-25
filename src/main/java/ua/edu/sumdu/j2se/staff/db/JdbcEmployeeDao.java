package ua.edu.sumdu.j2se.staff.db;

import ua.edu.sumdu.j2se.staff.model.BonusRecord;
import ua.edu.sumdu.j2se.staff.model.Employee;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Реалізація EmployeeDao через JDBC для PostgreSQL.
 */
public class JdbcEmployeeDao implements EmployeeDao {

    private static final String SELECT =
            "SELECT e.id, e.last_name, e.first_name, e.middle_name, e.position, e.salary, e.bonus, "
            + "e.hire_date, e.email, e.phone, e.department_id, d.name AS department_name, e.manager_id, "
            + "m.last_name AS m_last, m.first_name AS m_first, m.middle_name AS m_middle "
            + "FROM employees e "
            + "LEFT JOIN departments d ON d.id = e.department_id "
            + "LEFT JOIN employees m ON m.id = e.manager_id ";
    private static final String ORDER = "ORDER BY e.id";
    private static final String INSERT =
            "INSERT INTO employees (last_name, first_name, middle_name, position, salary, hire_date, "
            + "email, phone, department_id, manager_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE employees SET last_name = ?, first_name = ?, middle_name = ?, position = ?, salary = ?, "
            + "hire_date = ?, email = ?, phone = ?, department_id = ?, manager_id = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM employees WHERE id = ?";
    private static final String INSERT_BONUS_HISTORY =
            "INSERT INTO bonus_history (employee_id, percent, amount) "
            + "SELECT id, ?, ROUND(salary * ? / 100, 2) FROM employees WHERE department_id = ?";
    private static final String UPDATE_BONUS =
            "UPDATE employees SET bonus = bonus + ROUND(salary * ? / 100, 2) WHERE department_id = ?";
    private static final String SELECT_BONUS_HISTORY =
            "SELECT b.id, b.employee_id, e.last_name, e.first_name, e.middle_name, b.percent, b.amount, "
            + "b.accrual_date FROM bonus_history b JOIN employees e ON e.id = b.employee_id "
            + "WHERE e.department_id = ? ORDER BY b.accrual_date DESC, b.id DESC";

    private final ConnectionManager connectionManager;

    /**
     * Створює DAO.
     *
     * @param connectionManager джерело підключень
     */
    public JdbcEmployeeDao(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public List<Employee> findAll() throws DataAccessException {
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + ORDER)) {
            return readEmployees(ps);
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося отримати список працівників", e);
        }
    }

    @Override
    public List<Employee> findByLastName(String fragment) throws DataAccessException {
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE e.last_name ILIKE ? " + ORDER)) {
            ps.setString(1, "%" + escapeLike(fragment.trim()) + "%");
            return readEmployees(ps);
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося виконати пошук", e);
        }
    }

    @Override
    public List<Employee> findByDepartment(int departmentId) throws DataAccessException {
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE e.department_id = ? " + ORDER)) {
            ps.setInt(1, departmentId);
            return readEmployees(ps);
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося отримати працівників підрозділу", e);
        }
    }

    @Override
    public int add(Employee employee) throws DataAccessException {
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, employee);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                employee.setId(keys.getInt(1));
            }
            return employee.getId();
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося додати працівника", e);
        }
    }

    @Override
    public void update(Employee employee) throws DataAccessException {
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(UPDATE)) {
            fillStatement(ps, employee);
            ps.setInt(11, employee.getId());
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("Працівника не знайдено, можливо його вже видалено");
            }
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося змінити дані працівника", e);
        }
    }

    @Override
    public void delete(int id) throws DataAccessException {
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося видалити працівника", e);
        }
    }

    @Override
    public int accrueBonus(int departmentId, BigDecimal percent) throws DataAccessException {
        try (Connection c = connectionManager.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement history = c.prepareStatement(INSERT_BONUS_HISTORY);
                 PreparedStatement update = c.prepareStatement(UPDATE_BONUS)) {
                history.setBigDecimal(1, percent);
                history.setBigDecimal(2, percent);
                history.setInt(3, departmentId);
                int count = history.executeUpdate();

                update.setBigDecimal(1, percent);
                update.setInt(2, departmentId);
                update.executeUpdate();

                c.commit();
                return count;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося нарахувати премію", e);
        }
    }

    @Override
    public List<BonusRecord> findBonusHistory(int departmentId) throws DataAccessException {
        List<BonusRecord> result = new ArrayList<>();
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BONUS_HISTORY)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Employee e = new Employee();
                    e.setLastName(rs.getString("last_name"));
                    e.setFirstName(rs.getString("first_name"));
                    e.setMiddleName(rs.getString("middle_name"));
                    result.add(new BonusRecord(rs.getInt("id"), rs.getInt("employee_id"), e.getFullName(),
                            rs.getBigDecimal("percent"), rs.getBigDecimal("amount"),
                            rs.getDate("accrual_date").toLocalDate()));
                }
            }
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося отримати історію премій", e);
        }
        return result;
    }

    /**
     * Екранує символи % і _ , щоб вони шукалися як звичайні символи.
     *
     * @param text текст пошуку
     * @return текст для шаблону LIKE
     */
    static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static List<Employee> readEmployees(PreparedStatement ps) throws SQLException {
        List<Employee> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(readEmployee(rs));
            }
        }
        return result;
    }

    private static Employee readEmployee(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("id"));
        e.setLastName(rs.getString("last_name"));
        e.setFirstName(rs.getString("first_name"));
        e.setMiddleName(rs.getString("middle_name"));
        e.setPosition(rs.getString("position"));
        e.setSalary(rs.getBigDecimal("salary"));
        e.setBonus(rs.getBigDecimal("bonus"));
        e.setHireDate(rs.getDate("hire_date").toLocalDate());
        e.setEmail(rs.getString("email"));
        e.setPhone(rs.getString("phone"));
        e.setDepartmentId((Integer) rs.getObject("department_id"));
        e.setDepartmentName(rs.getString("department_name"));
        e.setManagerId((Integer) rs.getObject("manager_id"));
        if (e.getManagerId() != null) {
            Employee manager = new Employee();
            manager.setLastName(rs.getString("m_last"));
            manager.setFirstName(rs.getString("m_first"));
            manager.setMiddleName(rs.getString("m_middle"));
            e.setManagerName(manager.getShortName());
        }
        return e;
    }

    private static void fillStatement(PreparedStatement ps, Employee e) throws SQLException {
        ps.setString(1, e.getLastName());
        ps.setString(2, e.getFirstName());
        ps.setString(3, e.getMiddleName());
        ps.setString(4, e.getPosition());
        ps.setBigDecimal(5, e.getSalary());
        ps.setDate(6, Date.valueOf(e.getHireDate()));
        ps.setString(7, e.getEmail());
        ps.setString(8, e.getPhone());
        setNullableInt(ps, 9, e.getDepartmentId());
        setNullableInt(ps, 10, e.getManagerId());
    }

    private static void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }
}
