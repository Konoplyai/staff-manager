package ua.edu.sumdu.j2se.staff.ui;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.StringConverter;
import ua.edu.sumdu.j2se.staff.db.DataAccessException;
import ua.edu.sumdu.j2se.staff.model.Department;
import ua.edu.sumdu.j2se.staff.model.Employee;
import ua.edu.sumdu.j2se.staff.service.InputValidator;
import ua.edu.sumdu.j2se.staff.service.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Діалог додавання і редагування працівника. Вікно не закривається,
 * поки дані не пройдуть перевірку і не будуть збережені в базі.
 */
public class EmployeeDialog extends Dialog<Employee> {

    /**
     * Дія збереження, яку виконує діалог після натискання "Зберегти".
     */
    public interface Saver {
        /**
         * Зберігає працівника.
         *
         * @param employee працівник
         * @throws ValidationException якщо дані неправильні
         * @throws DataAccessException якщо запис у базу не вдався
         */
        void save(Employee employee) throws ValidationException, DataAccessException;
    }

    private static final Employee NO_MANAGER = new Employee("- немає -", null, null, null, null, null);

    private final Employee employee;
    private final InputValidator validator;

    private final TextField lastNameField = new TextField();
    private final TextField firstNameField = new TextField();
    private final TextField middleNameField = new TextField();
    private final TextField positionField = new TextField();
    private final ComboBox<Department> departmentBox = new ComboBox<>();
    private final ComboBox<Employee> managerBox = new ComboBox<>();
    private final TextField salaryField = new TextField();
    private final DatePicker hireDatePicker = new DatePicker();
    private final TextField emailField = new TextField();
    private final TextField phoneField = new TextField();
    private final Label errorLabel = new Label();

    /**
     * Створює діалог.
     *
     * @param owner батьківське вікно
     * @param source працівник для редагування або null для нового
     * @param departments підрозділи для вибору
     * @param employees усі працівники (для вибору керівника)
     * @param validator валідатор для розбору чисел
     * @param saver дія збереження
     */
    public EmployeeDialog(Window owner, Employee source, List<Department> departments, List<Employee> employees,
                          InputValidator validator, Saver saver) {
        this.employee = source == null ? new Employee() : source;
        this.validator = validator;
        initOwner(owner);
        setTitle(source == null ? "Новий працівник" : "Редагування працівника");
        setHeaderText(source == null ? "Введіть дані нового працівника"
                : "Працівник: " + source.getFullName());
        if (owner != null && owner.getScene() != null) {
            getDialogPane().getStylesheets().addAll(owner.getScene().getStylesheets());
        }

        departmentBox.getItems().setAll(departments);
        managerBox.getItems().add(NO_MANAGER);
        for (Employee e : employees) {
            if (e.getId() != employee.getId()) {
                managerBox.getItems().add(e);
            }
        }
        hireDatePicker.setConverter(new DateConverter());
        hireDatePicker.setPromptText("дд.мм.рррр");
        fillFields(departments, employees);

        ButtonType saveType = new ButtonType("Зберегти", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Скасувати", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveType, cancelType);
        getDialogPane().setContent(buildForm());

        Button saveButton = (Button) getDialogPane().lookupButton(saveType);
        saveButton.setId("dialogSave");
        saveButton.getStyleClass().add("primary");
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                readFields();
                saver.save(employee);
            } catch (ValidationException | DataAccessException e) {
                errorLabel.setText(e.getMessage());
                getDialogPane().getScene().getWindow().sizeToScene();
                event.consume();
            }
        });
        setResultConverter(button -> button == saveType ? employee : null);
    }

    private Node buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(9);
        ColumnConstraints c1 = new ColumnConstraints();
        ColumnConstraints c2 = new ColumnConstraints(300);
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        lastNameField.setId("empLastName");
        firstNameField.setId("empFirstName");
        middleNameField.setId("empMiddleName");
        positionField.setId("empPosition");
        departmentBox.setId("empDepartment");
        managerBox.setId("empManager");
        salaryField.setId("empSalary");
        hireDatePicker.setId("empHireDate");
        emailField.setId("empEmail");
        phoneField.setId("empPhone");
        salaryField.setPromptText("наприклад 32000,00");
        emailField.setPromptText("name@company.ua");
        phoneField.setPromptText("+380XXXXXXXXX");
        departmentBox.setMaxWidth(Double.MAX_VALUE);
        managerBox.setMaxWidth(Double.MAX_VALUE);
        hireDatePicker.setMaxWidth(Double.MAX_VALUE);

        int row = 0;
        grid.addRow(row++, new Label("Прізвище *"), lastNameField);
        grid.addRow(row++, new Label("Ім'я *"), firstNameField);
        grid.addRow(row++, new Label("По батькові"), middleNameField);
        grid.addRow(row++, new Label("Посада *"), positionField);
        grid.addRow(row++, new Label("Підрозділ *"), departmentBox);
        grid.addRow(row++, new Label("Керівник"), managerBox);
        grid.addRow(row++, new Label("Заробітна плата, грн *"), salaryField);
        grid.addRow(row++, new Label("Дата прийняття *"), hireDatePicker);
        grid.addRow(row++, new Label("Email"), emailField);
        grid.addRow(row, new Label("Телефон"), phoneField);

        errorLabel.setId("dialogError");
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(470);
        Label hint = new Label("* - обов'язкові поля");
        hint.getStyleClass().add("hint-label");
        VBox box = new VBox(10, grid, hint, errorLabel);
        box.setPadding(new Insets(6, 4, 0, 4));
        return box;
    }

    private void fillFields(List<Department> departments, List<Employee> employees) {
        lastNameField.setText(employee.getLastName());
        firstNameField.setText(employee.getFirstName());
        middleNameField.setText(employee.getMiddleName());
        positionField.setText(employee.getPosition());
        if (employee.getSalary() != null && employee.getSalary().signum() > 0) {
            salaryField.setText(employee.getSalary().toPlainString().replace('.', ','));
        }
        hireDatePicker.setValue(employee.getHireDate() == null ? LocalDate.now() : employee.getHireDate());
        emailField.setText(employee.getEmail());
        phoneField.setText(employee.getPhone());
        for (Department d : departments) {
            if (employee.getDepartmentId() != null && d.getId() == employee.getDepartmentId()) {
                departmentBox.setValue(d);
            }
        }
        managerBox.setValue(NO_MANAGER);
        for (Employee e : employees) {
            if (employee.getManagerId() != null && e.getId() == employee.getManagerId()) {
                managerBox.setValue(e);
            }
        }
    }

    private void readFields() throws ValidationException {
        employee.setLastName(lastNameField.getText());
        employee.setFirstName(firstNameField.getText());
        employee.setMiddleName(middleNameField.getText());
        employee.setPosition(positionField.getText());
        BigDecimal salary = validator.parseMoney(salaryField.getText(), "Заробітна плата");
        employee.setSalary(salary);
        employee.setHireDate(readDate());
        employee.setEmail(emailField.getText());
        employee.setPhone(phoneField.getText());
        Department department = departmentBox.getValue();
        employee.setDepartmentId(department == null ? null : department.getId());
        employee.setDepartmentName(department == null ? null : department.getName());
        Employee manager = managerBox.getValue();
        employee.setManagerId(manager == null || manager == NO_MANAGER ? null : manager.getId());
    }

    private LocalDate readDate() throws ValidationException {
        String text = hireDatePicker.getEditor().getText();
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Дата прийняття: не вказано");
        }
        try {
            return LocalDate.parse(text.trim(), Formats.DATE);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Дата прийняття: введіть дату у форматі дд.мм.рррр");
        }
    }

    /**
     * Перетворення дати для DatePicker у формат дд.мм.рррр.
     */
    private static class DateConverter extends StringConverter<LocalDate> {
        @Override
        public String toString(LocalDate date) {
            return Formats.date(date);
        }

        @Override
        public LocalDate fromString(String text) {
            try {
                return text == null || text.trim().isEmpty() ? null : LocalDate.parse(text.trim(), Formats.DATE);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }
}
