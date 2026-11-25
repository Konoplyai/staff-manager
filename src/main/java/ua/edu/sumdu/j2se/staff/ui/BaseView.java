package ua.edu.sumdu.j2se.staff.ui;

import javafx.scene.Node;
import javafx.stage.Window;
import ua.edu.sumdu.j2se.staff.db.DataAccessException;
import ua.edu.sumdu.j2se.staff.service.StaffService;
import ua.edu.sumdu.j2se.staff.service.ValidationException;

import java.util.function.Consumer;

/**
 * Спільна основа для вкладок головного вікна: доступ до сервісу, рядок стану
 * та однакова обробка помилок.
 */
public abstract class BaseView {

    /** Прикладна логіка. */
    protected final StaffService service;
    /** Вікно, якому належить вкладка (для діалогів). */
    protected final Window owner;

    private final Consumer<String> status;

    /**
     * Створює вкладку.
     *
     * @param service сервіс
     * @param owner головне вікно
     * @param status куди виводити повідомлення для рядка стану
     */
    protected BaseView(StaffService service, Window owner, Consumer<String> status) {
        this.service = service;
        this.owner = owner;
        this.status = status;
    }

    /**
     * Повертає вміст вкладки.
     *
     * @return кореневий вузол
     */
    public abstract Node getContent();

    /**
     * Перечитує дані з бази і оновлює вкладку.
     */
    public abstract void refresh();

    /**
     * Виводить повідомлення в рядок стану.
     *
     * @param message повідомлення
     */
    protected void setStatus(String message) {
        status.accept(message);
    }

    /**
     * Показує помилку користувачу.
     *
     * @param e помилка перевірки даних або помилка бази даних
     */
    protected void showError(Exception e) {
        if (e instanceof ValidationException) {
            Alerts.error(owner, "Перевірте введені дані", e.getMessage());
        } else if (e instanceof DataAccessException) {
            Alerts.error(owner, "Помилка бази даних", e.getMessage());
        } else {
            Alerts.error(owner, "Непередбачена помилка", String.valueOf(e.getMessage()));
        }
        setStatus("Помилка: " + firstLine(e.getMessage()));
    }

    private static String firstLine(String text) {
        if (text == null) {
            return "";
        }
        int end = text.indexOf('\n');
        return end < 0 ? text : text.substring(0, end);
    }
}
