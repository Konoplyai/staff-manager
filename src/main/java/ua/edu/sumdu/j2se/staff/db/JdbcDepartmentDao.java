package ua.edu.sumdu.j2se.staff.db;

import ua.edu.sumdu.j2se.staff.model.Department;
import ua.edu.sumdu.j2se.staff.model.DepartmentStatistics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Реалізація DepartmentDao через JDBC для PostgreSQL.
 */
public class JdbcDepartmentDao implements DepartmentDao {

    private static final String SELECT_ALL =
            "SELECT id, name, location, phone FROM departments ORDER BY name";
    private static final String INSERT =
            "INSERT INTO departments (name, location, phone) VALUES (?, ?, ?)";
    private static final String UPDATE =
            "UPDATE departments SET name = ?, location = ?, phone = ? WHERE id = ?";
    private static final String DELETE =
            "DELETE FROM departments WHERE id = ?";
    private static final String STATISTICS =
            "SELECT d.id, d.name, d.location, d.phone, COUNT(e.id) AS cnt, "
            + "AVG(e.salary) AS avg_salary, MIN(e.salary) AS min_salary, MAX(e.salary) AS max_salary, "
            + "COALESCE(SUM(e.salary), 0) AS fund, COALESCE(SUM(e.bonus), 0) AS bonus_total "
            + "FROM departments d LEFT JOIN employees e ON e.department_id = d.id ";
    private static final String STATISTICS_GROUP =
            "GROUP BY d.id, d.name, d.location, d.phone ";

    private final ConnectionManager connectionManager;

    /**
     * Створює DAO.
     *
     * @param connectionManager джерело підключень
     */
    public JdbcDepartmentDao(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public List<Department> findAll() throws DataAccessException {
        List<Department> result = new ArrayList<>();
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(readDepartment(rs));
            }
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося отримати список підрозділів", e);
        }
        return result;
    }

    @Override
    public int add(Department department) throws DataAccessException {
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, department);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                department.setId(keys.getInt(1));
            }
            return department.getId();
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося додати підрозділ", e);
        }
    }

    @Override
    public void update(Department department) throws DataAccessException {
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(UPDATE)) {
            fillStatement(ps, department);
            ps.setInt(4, department.getId());
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("Підрозділ не знайдено, можливо його вже видалено");
            }
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося змінити підрозділ", e);
        }
    }

    @Override
    public void delete(int id) throws DataAccessException {
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося видалити підрозділ", e);
        }
    }

    @Override
    public List<DepartmentStatistics> getStatistics() throws DataAccessException {
        List<DepartmentStatistics> result = new ArrayList<>();
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(STATISTICS + STATISTICS_GROUP + "ORDER BY d.name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(readStatistics(rs));
            }
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося обчислити показники підрозділів", e);
        }
        return result;
    }

    @Override
    public DepartmentStatistics getStatistics(Department department) throws DataAccessException {
        try (Connection c = connectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(STATISTICS + "WHERE d.id = ? " + STATISTICS_GROUP)) {
            ps.setInt(1, department.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DataAccessException("Підрозділ \"" + department.getName() + "\" не знайдено");
                }
                return readStatistics(rs);
            }
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося обчислити показники підрозділу", e);
        }
    }

    private static void fillStatement(PreparedStatement ps, Department d) throws SQLException {
        ps.setString(1, d.getName());
        ps.setString(2, d.getLocation());
        ps.setString(3, d.getPhone());
    }

    private static Department readDepartment(ResultSet rs) throws SQLException {
        return new Department(rs.getInt("id"), rs.getString("name"), rs.getString("location"),
                rs.getString("phone"));
    }

    private static DepartmentStatistics readStatistics(ResultSet rs) throws SQLException {
        BigDecimal avg = rs.getBigDecimal("avg_salary");
        return new DepartmentStatistics(readDepartment(rs), rs.getInt("cnt"),
                avg == null ? BigDecimal.ZERO : avg.setScale(2, RoundingMode.HALF_UP),
                orZero(rs.getBigDecimal("min_salary")), orZero(rs.getBigDecimal("max_salary")),
                orZero(rs.getBigDecimal("fund")), orZero(rs.getBigDecimal("bonus_total")));
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
