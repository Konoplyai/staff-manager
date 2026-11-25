package ua.edu.sumdu.j2se.staff.ui;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import ua.edu.sumdu.j2se.staff.db.DataAccessException;
import ua.edu.sumdu.j2se.staff.model.Department;
import ua.edu.sumdu.j2se.staff.model.Employee;
import ua.edu.sumdu.j2se.staff.service.EmployeeComparators;
import ua.edu.sumdu.j2se.staff.service.StaffService;
import ua.edu.sumdu.j2se.staff.service.ValidationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Вкладка "Працівники": таблиця працівників, пошук за прізвищем, сортування за ПІБ,
 * додавання, редагування і видалення.
 */
public class EmployeesView extends BaseView {

    /** Варіант сортування: без сортування (за номером). */
    public static final String SORT_NONE = "за номером";
    /** Варіант сортування: ПІБ від А до Я. */
    public static final String SORT_ASC = "ПІБ: від А до Я";
    /** Варіант сортування: ПІБ від Я до А. */
    public static final String SORT_DESC = "ПІБ: від Я до А";

    private final BorderPane root = new BorderPane();
    private final TableView<Employee> table = new TableView<>();
    private final TextField searchField = new TextField();
    private final ComboBox<String> sortBox = new ComboBox<>();
    private final Label countLabel = new Label();

    private List<Employee> loaded = new ArrayList<>();
    private int totalCount;
    private String lastSearch = "";

    /**
     * Створює вкладку.
     *
     * @param service сервіс
     * @param owner головне вікно
     * @param status рядок стану
     */
    public EmployeesView(StaffService service, Window owner, Consumer<String> status) {
        super(service, owner, status);
        build();
    }

    @Override
    public Node getContent() {
        return root;
    }

    @Override
    public void refresh() {
        load(lastSearch);
    }

    private void build() {
        Label searchLabel = new Label("Пошук за прізвищем:");
        searchField.setId("searchField");
        searchField.setPromptText("прізвище або його частина");
        searchField.setPrefWidth(240);
        searchField.setOnAction(e -> search());
        Button searchButton = new Button("Знайти");
        searchButton.setId("searchButton");
        searchButton.getStyleClass().add("primary");
        searchButton.setOnAction(e -> search());
        Button resetButton = new Button("Скинути");
        resetButton.setId("resetButton");
        resetButton.setOnAction(e -> {
            searchField.clear();
            load("");
        });

        Label sortLabel = new Label("Сортування:");
        sortBox.setId("sortBox");
        sortBox.getItems().addAll(SORT_NONE, SORT_ASC, SORT_DESC);
        sortBox.setValue(SORT_NONE);
        sortBox.setOnAction(e -> show());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(8, searchLabel, searchField, searchButton, resetButton, spacer, sortLabel, sortBox);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar-panel");

        buildTable();

        Button addButton = new Button("Додати працівника");
        addButton.setId("addEmployee");
        addButton.getStyleClass().add("primary");
        addButton.setOnAction(e -> openDialog(null));
        Button editButton = new Button("Редагувати");
        editButton.setId("editEmployee");
        editButton.setOnAction(e -> openDialog(table.getSelectionModel().getSelectedItem()));
        Button deleteButton = new Button("Видалити");
        deleteButton.setId("deleteEmployee");
        deleteButton.getStyleClass().add("danger");
        deleteButton.setOnAction(e -> delete());
        editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        countLabel.setId("employeeCount");
        countLabel.getStyleClass().add("count-label");
        HBox bottom = new HBox(8, addButton, editButton, deleteButton, spacer2, countLabel);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(10, 0, 0, 0));

        root.setTop(toolbar);
        root.setCenter(table);
        root.setBottom(bottom);
        root.setPadding(new Insets(12));
        BorderPane.setMargin(table, new Insets(10, 0, 0, 0));
    }

    private void buildTable() {
        table.setId("employeeTable");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Працівників не знайдено"));
        table.getColumns().add(column("№", 40, e -> String.valueOf(e.getId())));
        TableColumn<Employee, String> name = column("ПІБ", 235, Employee::getFullName);
        table.getColumns().add(name);
        table.getColumns().add(column("Посада", 205, Employee::getPosition));
        table.getColumns().add(column("Підрозділ", 155, Employee::getDepartmentName));
        table.getColumns().add(moneyColumn("Заробітна плата", 118, Employee::getSalary));
        table.getColumns().add(moneyColumn("Премія", 88, Employee::getBonus));
        table.getColumns().add(column("Прийнятий", 88, e -> Formats.date(e.getHireDate())));
        table.getColumns().add(column("Керівник", 130, Employee::getManagerName));
        table.getColumns().add(column("Телефон", 115, Employee::getPhone));
        table.getColumns().add(column("Email", 170, Employee::getEmail));
        for (TableColumn<Employee, ?> c : table.getColumns()) {
            c.setSortable(false);
        }
        table.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDialog(row.getItem());
                }
            });
            return row;
        });
    }

    private static TableColumn<Employee, String> column(String title, double width, Function<Employee, String> getter) {
        TableColumn<Employee, String> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(data -> new ReadOnlyStringWrapper(getter.apply(data.getValue())));
        return c;
    }

    private static TableColumn<Employee, BigDecimal> moneyColumn(String title, double width,
                                                                 Function<Employee, BigDecimal> getter) {
        TableColumn<Employee, BigDecimal> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(getter.apply(data.getValue())));
        c.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : Formats.money(value));
            }
        });
        c.getStyleClass().add("money-column");
        return c;
    }

    private void search() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim();
        load(text);
    }

    private void load(String fragment) {
        try {
            loaded = service.searchByLastName(fragment);
            lastSearch = fragment;
            if (fragment.isEmpty()) {
                totalCount = loaded.size();
                setStatus("Завантажено працівників: " + loaded.size());
            } else {
                setStatus("Пошук \"" + fragment + "\": знайдено " + loaded.size());
            }
            show();
        } catch (ValidationException | DataAccessException e) {
            showError(e);
        }
    }

    private void show() {
        List<Employee> shown;
        String sort = sortBox.getValue();
        if (SORT_ASC.equals(sort)) {
            shown = EmployeeComparators.sortByFullName(loaded, true);
        } else if (SORT_DESC.equals(sort)) {
            shown = EmployeeComparators.sortByFullName(loaded, false);
        } else {
            shown = loaded;
        }
        table.getItems().setAll(shown);
        if (lastSearch.isEmpty()) {
            countLabel.setText("Усього працівників: " + shown.size());
        } else {
            countLabel.setText("Знайдено: " + shown.size() + " з " + totalCount);
        }
    }

    private void openDialog(Employee selected) {
        List<Department> departments;
        List<Employee> all;
        try {
            departments = service.getDepartments();
            all = service.getEmployees();
        } catch (DataAccessException e) {
            showError(e);
            return;
        }
        boolean isNew = selected == null;
        EmployeeDialog dialog = new EmployeeDialog(owner, selected, departments, all, service.getValidator(),
                isNew ? service::addEmployee : service::updateEmployee);
        dialog.showAndWait().ifPresent(e -> setStatus((isNew ? "Додано працівника: " : "Збережено зміни: ")
                + e.getFullName()));
        refresh();
    }

    private void delete() {
        Employee selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        boolean yes = Alerts.confirm(owner, "Видалити працівника " + selected.getFullName() + "?",
                "Запис буде видалено з бази даних разом з історією премій. "
                + "Якщо працівник є керівником, у його підлеглих поле \"Керівник\" стане порожнім.");
        if (!yes) {
            return;
        }
        try {
            service.deleteEmployee(selected);
            setStatus("Видалено працівника: " + selected.getFullName());
            refresh();
        } catch (DataAccessException e) {
            showError(e);
        }
    }
}
