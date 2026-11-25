package ua.edu.sumdu.j2se.staff.ui;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ua.edu.sumdu.j2se.staff.db.ConnectionManager;
import ua.edu.sumdu.j2se.staff.db.DataAccessException;
import ua.edu.sumdu.j2se.staff.db.DatabaseConfig;

import java.util.function.Consumer;

/**
 * Вікно входу: користувач вводить параметри сервера, логін і пароль PostgreSQL.
 * Після успішної перевірки підключення вікно закривається і викликається onSuccess.
 */
public class LoginWindow {

    private final Stage stage;
    private final Consumer<ConnectionManager> onSuccess;

    private final TextField hostField = new TextField();
    private final TextField portField = new TextField();
    private final TextField databaseField = new TextField();
    private final TextField userField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label errorLabel = new Label();

    /**
     * Створює вікно входу.
     *
     * @param stage вікно
     * @param defaults значення полів за замовчуванням
     * @param onSuccess що зробити після успішного підключення
     */
    public LoginWindow(Stage stage, DatabaseConfig defaults, Consumer<ConnectionManager> onSuccess) {
        this.stage = stage;
        this.onSuccess = onSuccess;
        hostField.setText(defaults.getHost());
        portField.setText(String.valueOf(defaults.getPort()));
        databaseField.setText(defaults.getDatabase());
        userField.setText(defaults.getUser());
        build();
    }

    /**
     * Показує вікно.
     */
    public void show() {
        stage.show();
        passwordField.requestFocus();
    }

    private void build() {
        Label title = new Label(StaffApp.TITLE);
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("Вхід до бази даних PostgreSQL");
        subtitle.getStyleClass().add("login-subtitle");
        VBox header = new VBox(4, title, subtitle);
        header.getStyleClass().add("login-header");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(20, 28, 8, 28));
        ColumnConstraints labels = new ColumnConstraints();
        labels.setHalignment(HPos.RIGHT);
        ColumnConstraints fields = new ColumnConstraints();
        fields.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labels, fields);

        hostField.setId("loginHost");
        portField.setId("loginPort");
        databaseField.setId("loginDatabase");
        userField.setId("loginUser");
        passwordField.setId("loginPassword");
        passwordField.setPromptText("пароль користувача бази");

        form.addRow(0, new Label("Сервер:"), hostField);
        form.addRow(1, new Label("Порт:"), portField);
        form.addRow(2, new Label("База даних:"), databaseField);
        form.addRow(3, new Label("Користувач:"), userField);
        form.addRow(4, new Label("Пароль:"), passwordField);

        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setId("loginError");
        VBox errorBox = new VBox(errorLabel);
        errorBox.setPadding(new Insets(0, 28, 0, 28));

        Button connect = new Button("Підключитися");
        connect.setId("loginButton");
        connect.setDefaultButton(true);
        connect.getStyleClass().add("primary");
        connect.setOnAction(e -> connect());
        Button cancel = new Button("Вихід");
        cancel.setCancelButton(true);
        cancel.setOnAction(e -> stage.close());
        HBox buttons = new HBox(10, cancel, connect);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(12, 28, 20, 28));

        VBox root = new VBox(header, form, errorBox, buttons);
        root.getStyleClass().add("login-root");
        Scene scene = new Scene(root, 440, 410);
        scene.getStylesheets().add(getClass().getResource(StaffApp.STYLES).toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Вхід - " + StaffApp.TITLE);
        stage.setResizable(false);
    }

    private void connect() {
        errorLabel.setText("");
        DatabaseConfig config;
        try {
            int port = Integer.parseInt(portField.getText().trim());
            config = new DatabaseConfig(hostField.getText(), port, databaseField.getText(), userField.getText(),
                    passwordField.getText());
        } catch (NumberFormatException e) {
            errorLabel.setText("Порт має бути цілим числом, наприклад 5432");
            return;
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
            return;
        }
        ConnectionManager manager = new ConnectionManager(config);
        try {
            manager.testConnection();
        } catch (DataAccessException e) {
            errorLabel.setText(e.getMessage());
            passwordField.selectAll();
            passwordField.requestFocus();
            return;
        }
        stage.close();
        onSuccess.accept(manager);
    }
}
