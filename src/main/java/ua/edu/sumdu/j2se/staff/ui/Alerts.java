package ua.edu.sumdu.j2se.staff.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Стандартні вікна повідомлень українською мовою.
 */
public final class Alerts {

    /** Кнопка "Так". */
    public static final ButtonType YES = new ButtonType("Так", ButtonBar.ButtonData.YES);
    /** Кнопка "Ні". */
    public static final ButtonType NO = new ButtonType("Ні", ButtonBar.ButtonData.NO);

    private Alerts() {
    }

    /**
     * Показує повідомлення про помилку.
     *
     * @param owner батьківське вікно
     * @param header короткий заголовок
     * @param message текст помилки
     */
    public static void error(Window owner, String header, String message) {
        show(owner, Alert.AlertType.ERROR, "Помилка", header, message);
    }

    /**
     * Показує інформаційне повідомлення.
     *
     * @param owner батьківське вікно
     * @param header короткий заголовок
     * @param message текст
     */
    public static void info(Window owner, String header, String message) {
        show(owner, Alert.AlertType.INFORMATION, "Інформація", header, message);
    }

    /**
     * Питає підтвердження дії.
     *
     * @param owner батьківське вікно
     * @param header питання
     * @param message пояснення
     * @return true, якщо користувач натиснув "Так"
     */
    public static boolean confirm(Window owner, String header, String message) {
        Alert alert = create(owner, Alert.AlertType.CONFIRMATION, "Підтвердження", header, message);
        alert.getButtonTypes().setAll(YES, NO);
        Optional<ButtonType> answer = alert.showAndWait();
        return answer.isPresent() && answer.get() == YES;
    }

    private static void show(Window owner, Alert.AlertType type, String title, String header, String message) {
        create(owner, type, title, header, message).showAndWait();
    }

    private static Alert create(Window owner, Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.getDialogPane().setMinWidth(460);
        if (owner != null && owner.getScene() != null) {
            alert.getDialogPane().getStylesheets().addAll(owner.getScene().getStylesheets());
        }
        return alert;
    }
}
