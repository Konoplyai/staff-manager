package ua.edu.sumdu.j2se.staff.db;

import java.sql.SQLException;

/**
 * Помилка роботи з базою даних. Повідомлення складається українською мовою,
 * щоб його можна було одразу показати користувачу.
 */
public class DataAccessException extends Exception {

    /**
     * Створює виняток з повідомленням.
     *
     * @param message повідомлення
     */
    public DataAccessException(String message) {
        super(message);
    }

    /**
     * Створює виняток з повідомленням і причиною.
     *
     * @param message повідомлення
     * @param cause причина
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Перетворює SQLException на зрозуміле повідомлення за кодом SQLSTATE.
     *
     * @param action що саме не вдалося зробити, наприклад "Не вдалося додати працівника"
     * @param e виняток JDBC
     * @return виняток з поясненням причини
     */
    public static DataAccessException of(String action, SQLException e) {
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        String reason;
        if ("23505".equals(state)) {
            reason = "запис з таким значенням уже існує (назва підрозділу або email мають бути унікальними)";
        } else if ("23503".equals(state)) {
            reason = "на запис посилаються інші дані (наприклад, у підрозділі ще є працівники)";
        } else if ("23514".equals(state)) {
            reason = "значення не пройшло перевірку в базі даних (сума має бути більшою за нуль)";
        } else if ("28P01".equals(state) || "28000".equals(state)) {
            reason = "неправильне ім'я користувача або пароль";
        } else if ("3D000".equals(state)) {
            reason = "бази даних з такою назвою не існує";
        } else if ("42P01".equals(state)) {
            reason = "у базі даних немає потрібних таблиць, виконайте скрипт db/schema.sql";
        } else if (state.startsWith("08")) {
            reason = "сервер PostgreSQL недоступний, перевірте адресу, порт і чи запущено службу";
        } else {
            reason = e.getMessage();
        }
        return new DataAccessException(action + ": " + reason, e);
    }
}
