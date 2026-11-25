package ua.edu.sumdu.j2se.staff.db;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Параметри підключення до PostgreSQL: сервер, порт, назва бази, користувач і пароль.
 * Сервер, порт, база та користувач за замовчуванням читаються з файлу app.properties,
 * пароль користувач вводить у вікні входу і у файлі він не зберігається.
 */
public class DatabaseConfig {

    /** Файл з параметрами за замовчуванням. */
    public static final String DEFAULT_FILE = "app.properties";

    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;

    /**
     * Створює параметри підключення.
     *
     * @param host адреса сервера
     * @param port порт (1..65535)
     * @param database назва бази даних
     * @param user ім'я користувача
     * @param password пароль
     * @throws IllegalArgumentException якщо якийсь параметр порожній або порт неправильний
     */
    public DatabaseConfig(String host, int port, String database, String user, String password) {
        if (isBlank(host)) {
            throw new IllegalArgumentException("Не вказано адресу сервера");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Порт має бути числом від 1 до 65535");
        }
        if (isBlank(database)) {
            throw new IllegalArgumentException("Не вказано назву бази даних");
        }
        if (isBlank(user)) {
            throw new IllegalArgumentException("Не вказано ім'я користувача");
        }
        this.host = host.trim();
        this.port = port;
        this.database = database.trim();
        this.user = user.trim();
        this.password = password == null ? "" : password;
    }

    /**
     * Читає параметри за замовчуванням з файлу (без пароля).
     * Якщо файлу немає, повертаються стандартні значення localhost:5432/company_db.
     *
     * @param file файл .properties
     * @return параметри з порожнім паролем
     * @throws IOException якщо файл існує, але прочитати його не вдалося
     */
    public static DatabaseConfig loadDefaults(Path file) throws IOException {
        Properties p = new Properties();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                p.load(reader);
            }
        }
        int port;
        try {
            port = Integer.parseInt(p.getProperty("db.port", "5432").trim());
        } catch (NumberFormatException e) {
            port = 5432;
        }
        return new DatabaseConfig(p.getProperty("db.host", "localhost"), port,
                p.getProperty("db.name", "company_db"), p.getProperty("db.user", "company_user"), "");
    }

    /**
     * Повертає JDBC-адресу бази даних.
     *
     * @return адреса виду jdbc:postgresql://host:port/database
     */
    public String getUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + database;
    }

    /**
     * Повертає адресу сервера.
     *
     * @return сервер
     */
    public String getHost() {
        return host;
    }

    /**
     * Повертає порт.
     *
     * @return порт
     */
    public int getPort() {
        return port;
    }

    /**
     * Повертає назву бази даних.
     *
     * @return назва бази
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Повертає ім'я користувача.
     *
     * @return користувач
     */
    public String getUser() {
        return user;
    }

    /**
     * Повертає пароль.
     *
     * @return пароль
     */
    public String getPassword() {
        return password;
    }

    /**
     * Короткий опис підключення для рядка стану (без пароля).
     *
     * @return рядок виду user@host:port/database
     */
    @Override
    public String toString() {
        return user + "@" + host + ":" + port + "/" + database;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
