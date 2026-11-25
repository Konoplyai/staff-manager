package ua.edu.sumdu.j2se.staff.ui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import ua.edu.sumdu.j2se.staff.db.DataAccessException;
import ua.edu.sumdu.j2se.staff.model.Department;
import ua.edu.sumdu.j2se.staff.model.DepartmentStatistics;
import ua.edu.sumdu.j2se.staff.model.Employee;
import ua.edu.sumdu.j2se.staff.service.StaffService;
import ua.edu.sumdu.j2se.staff.service.ValidationException;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Вкладка "Підрозділи": таблиця підрозділів з кількістю працівників і заробітною платою,
 * список працівників вибраного підрозділу, додавання, редагування і видалення підрозділів.
 */
public class DepartmentsView extends BaseView {

    private final BorderPane root = new BorderPane();
    private final TableView<DepartmentStatistics> table = new TableView<>();
    private final TableView<Employee> employeeTable = new TableView<>();
    private final Label employeesTitle = new Label("Працівники підрозділу");

    /**
     * Створює вкладку.
     *
     * @param service сервіс
     * @param owner головне вікно
     * @param status рядок стану
     */
    public DepartmentsView(StaffService service, Window owner, Consumer<String> status) {
        super(service, owner, status);
        build();
    }

    @Override
    public Node getContent() {
        return root;
    }

    @Override
    public void refresh() {
        Department selected = selectedDepartment();
        try {
            List<DepartmentStatistics> statistics = service.getAllStatistics();
            table.getItems().setAll(statistics);
            for (DepartmentStatistics s : statistics) {
                if (selected != null && s.getDepartment().getId() == selected.getId()) {
                    table.getSelectionModel().select(s);
                }
            }
            if (table.getSelectionModel().isEmpty() && !statistics.isEmpty()) {
                table.getSelectionModel().selectFirst();
            }
            setStatus("Завантажено підрозділів: " + statistics.size());
        } catch (DataAccessException e) {
            showError(e);
        }
    }

    private void build() {
        table.setId("departmentTable");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Підрозділів немає"));
        table.getColumns().add(column("Назва підрозділу", 220, s -> s.getDepartment().getName()));
        table.getColumns().add(column("Розташування", 170, s -> s.getDepartment().getLocation()));
        table.getColumns().add(column("Телефон", 130, s -> s.getDepartment().getPhone()));
        table.getColumns().add(column("Працівників", 100, s -> String.valueOf(s.getEmployeeCount())));
        table.getColumns().add(column("Середня ЗП, грн", 130, s -> Formats.money(s.getAverageSalary())));
        table.getColumns().add(column("Фонд ЗП, грн", 130, s -> Formats.money(s.getSalaryFund())));
        table.getColumns().add(column("Премії, грн", 110, s -> Formats.money(s.getBonusTotal())));
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> showEmployees(now));
        table.setRowFactory(tv -> {
            TableRow<DepartmentStatistics> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDialog(row.getItem().getDepartment());
                }
            });
            return row;
        });

        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        employeeTable.setPlaceholder(new Label("У підрозділі немає працівників"));
        employeeTable.getColumns().add(employeeColumn("ПІБ", 280, Employee::getFullName));
        employeeTable.getColumns().add(employeeColumn("Посада", 240, Employee::getPosition));
        employeeTable.getColumns().add(employeeColumn("Заробітна плата, грн", 150, e -> Formats.money(e.getSalary())));
        employeeTable.getColumns().add(employeeColumn("Прийнятий", 100, e -> Formats.date(e.getHireDate())));
        employeeTable.getColumns().add(employeeColumn("Керівник", 150, Employee::getManagerName));
        employeesTitle.getStyleClass().add("section-title");
        VBox lower = new VBox(6, employeesTitle, employeeTable);
        VBox.setVgrow(employeeTable, Priority.ALWAYS);
        lower.setPadding(new Insets(8, 0, 0, 0));

        SplitPane split = new SplitPane(table, lower);
        split.setOrientation(Orientation.VERTICAL);
        split.setDividerPositions(0.48);

        Button add = new Button("Додати підрозділ");
        add.getStyleClass().add("primary");
        add.setOnAction(e -> openDialog(null));
        Button edit = new Button("Редагувати");
        edit.setOnAction(e -> openDialog(selectedDepartment()));
        Button delete = new Button("Видалити");
        delete.getStyleClass().add("danger");
        delete.setOnAction(e -> delete());
        edit.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        delete.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label hint = new Label("Подвійне клацання по рядку відкриває редагування");
        hint.getStyleClass().add("hint-label");
        HBox bottom = new HBox(8, add, edit, delete, spacer, hint);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(10, 0, 0, 0));

        root.setCenter(split);
        root.setBottom(bottom);
        root.setPadding(new Insets(12));
    }

    private static TableColumn<DepartmentStatistics, String> column(String title, double width,
                                                                    Function<DepartmentStatistics, String> getter) {
        TableColumn<DepartmentStatistics, String> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(data -> new ReadOnlyStringWrapper(getter.apply(data.getValue())));
        return c;
    }

    private static TableColumn<Employee, String> employeeColumn(String title, double width,
                                                               Function<Employee, String> getter) {
        TableColumn<Employee, String> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(data -> new ReadOnlyStringWrapper(getter.apply(data.getValue())));
        return c;
    }

    private Department selectedDepartment() {
        DepartmentStatistics s = table.getSelectionModel().getSelectedItem();
        return s == null ? null : s.getDepartment();
    }

    private void showEmployees(DepartmentStatistics statistics) {
        if (statistics == null) {
            employeeTable.getItems().clear();
            employeesTitle.setText("Працівники підрозділу");
            return;
        }
        try {
            List<Employee> employees = service.getEmployees(statistics.getDepartment());
            employeeTable.getItems().setAll(employees);
            employeesTitle.setText("Працівники підрозділу «" + statistics.getDepartment().getName() + "»: "
                    + employees.size() + ", середня заробітна плата "
                    + Formats.hryvnia(StaffService.averageSalary(employees)));
        } catch (DataAccessException e) {
            showError(e);
        }
    }

    private void openDialog(Department selected) {
        boolean isNew = selected == null;
        DepartmentDialog dialog = new DepartmentDialog(owner, selected,
                isNew ? service::addDepartment : service::updateDepartment);
        dialog.showAndWait().ifPresent(d -> setStatus((isNew ? "Додано підрозділ: " : "Збережено підрозділ: ")
                + d.getName()));
        refresh();
    }

    private void delete() {
        Department selected = selectedDepartment();
        if (selected == null) {
            return;
        }
        if (!Alerts.confirm(owner, "Видалити підрозділ «" + selected.getName() + "»?",
                "Підрозділ можна видалити, лише якщо в ньому немає працівників.")) {
            return;
        }
        try {
            service.deleteDepartment(selected);
            setStatus("Видалено підрозділ: " + selected.getName());
            refresh();
        } catch (ValidationException | DataAccessException e) {
            showError(e);
        }
    }
}
