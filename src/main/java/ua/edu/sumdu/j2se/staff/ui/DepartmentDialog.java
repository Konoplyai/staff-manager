package ua.edu.sumdu.j2se.staff.ui;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import ua.edu.sumdu.j2se.staff.db.DataAccessException;
import ua.edu.sumdu.j2se.staff.model.Department;
import ua.edu.sumdu.j2se.staff.service.ValidationException;

/**
 * Діалог додавання і редагування підрозділу.
 */
public class DepartmentDialog extends Dialog<Department> {

    /**
     * Дія збереження підрозділу.
     */
    public interface Saver {
        /**
         * Зберігає підрозділ.
         *
         * @param department підрозділ
         * @throws ValidationException якщо дані неправильні
         * @throws DataAccessException якщо запис у базу не вдався
         */
        void save(Department department) throws ValidationException, DataAccessException;
    }

    private final TextField nameField = new TextField();
    private final TextField locationField = new TextField();
    private final TextField phoneField = new TextField();
    private final Label errorLabel = new Label();

    /**
     * Створює діалог.
     *
     * @param owner батьківське вікно
     * @param source підрозділ для редагування або null для нового
     * @param saver дія збереження
     */
    public DepartmentDialog(Window owner, Department source, Saver saver) {
        Department department = source == null ? new Department() : source;
        initOwner(owner);
        setTitle(source == null ? "Новий підрозділ" : "Редагування підрозділу");
        setHeaderText(source == null ? "Введіть дані нового підрозділу" : "Підрозділ: " + source.getName());
        if (owner != null && owner.getScene() != null) {
            getDialogPane().getStylesheets().addAll(owner.getScene().getStylesheets());
        }
        nameField.setText(department.getName());
        locationField.setText(department.getLocation());
        phoneField.setText(department.getPhone());
        nameField.setId("depName");
        locationField.setId("depLocation");
        phoneField.setId("depPhone");
        locationField.setPromptText("наприклад Корпус Б, к. 210");
        phoneField.setPromptText("+380XXXXXXXXX");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(9);
        ColumnConstraints c2 = new ColumnConstraints(300);
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(new ColumnConstraints(), c2);
        grid.addRow(0, new Label("Назва *"), nameField);
        grid.addRow(1, new Label("Розташування"), locationField);
        grid.addRow(2, new Label("Телефон"), phoneField);
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(420);
        VBox box = new VBox(10, grid, errorLabel);
        box.setPadding(new Insets(6, 4, 0, 4));

        ButtonType saveType = new ButtonType("Зберегти", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Скасувати", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveType, cancelType);
        getDialogPane().setContent(box);
        Button saveButton = (Button) getDialogPane().lookupButton(saveType);
        saveButton.getStyleClass().add("primary");
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            department.setName(nameField.getText());
            department.setLocation(locationField.getText());
            department.setPhone(phoneField.getText());
            try {
                saver.save(department);
            } catch (ValidationException | DataAccessException e) {
                errorLabel.setText(e.getMessage());
                getDialogPane().getScene().getWindow().sizeToScene();
                event.consume();
            }
        });
        setResultConverter(button -> button == saveType ? department : null);
    }
}
