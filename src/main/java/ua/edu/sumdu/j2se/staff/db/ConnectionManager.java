package ua.edu.sumdu.j2se.staff.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Видає JDBC-підключення до бази даних за параметрами, які ввів користувач.
 * Для кожної операції відкривається окреме підключення і закривається в try-with-resources.
 */
public class ConnectionManager {

    private final DatabaseConfig config;

    /**
     * Створює менеджер підключень.
     *
     * @param config параметри підключення
     */
    public ConnectionManager(DatabaseConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Параметри підключення не задано");
        }
        this.config = config;
    }

    /**
     * Відкриває нове підключення.
     *
     * @return підключення
     * @throws SQLException якщо підключитися не вдалося
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
    }

    /**
     * Перевіряє, що до бази можна підключитися і в ній є потрібні таблиці.
     *
     * @throws DataAccessException якщо підключення або перевірка не вдалися
     */
    public void testConnection() throws DataAccessException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT COUNT(*) FROM departments").close();
            statement.executeQuery("SELECT COUNT(*) FROM employees").close();
        } catch (SQLException e) {
            throw DataAccessException.of("Не вдалося підключитися до бази даних", e);
        }
    }

    /**
     * Повертає параметри підключення.
     *
     * @return параметри
     */
    public DatabaseConfig getConfig() {
        return config;
    }
}
