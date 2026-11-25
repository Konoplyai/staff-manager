package ua.edu.sumdu.j2se.staff.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ua.edu.sumdu.j2se.staff.db.DataAccessException;
import ua.edu.sumdu.j2se.staff.db.DatabaseConfig;
import ua.edu.sumdu.j2se.staff.db.ImportResult;
import ua.edu.sumdu.j2se.staff.io.CompanyData;
import ua.edu.sumdu.j2se.staff.service.StaffService;
import ua.edu.sumdu.j2se.staff.service.ValidationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Головне вікно: меню, три вкладки (працівники, підрозділи, премії та заробітна плата)
 * і рядок стану.
 */
public class MainWindow {

    private static final int MAX_ERRORS_SHOWN = 12;

    private final Stage stage;
    private final StaffService service;
    private final DatabaseConfig config;
    private final Label statusLabel = new Label();
    private final TabPane tabs = new TabPane();
    private final EmployeesView employeesView;
    private final DepartmentsView departmentsView;
    private final BonusView bonusView;

    /**
     * Створює головне вікно.
     *
     * @param stage вікно
     * @param service сервіс
     * @param config параметри підключення (для рядка стану)
     */
    public MainWindow(Stage stage, StaffService service, DatabaseConfig config) {
        this.stage = stage;
        this.service = service;
        this.config = config;
        employeesView = new EmployeesView(service, stage, this::setStatus);
        departmentsView = new DepartmentsView(service, stage, this::setStatus);
        bonusView = new BonusView(service, stage, this::setStatus);
        build();
    }

    /**
     * Показує вікно і завантажує дані.
     */
    public void show() {
        stage.show();
        employeesView.refresh();
    }

    private void build() {
        Tab employeesTab = new Tab("Працівники", employeesView.getContent());
        Tab departmentsTab = new Tab("Підрозділи", departmentsView.getContent());
        Tab bonusTab = new Tab("Премії та заробітна плата", bonusView.getContent());
        tabs.getTabs().addAll(employeesTab, departmentsTab, bonusTab);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setId("mainTabs");
        tabs.getSelectionModel().selectedIndexProperty().addListener((obs, old, now) -> currentView().refresh());

        Label title = new Label(StaffApp.TITLE);
        title.getStyleClass().add("app-title");
        Label connection = new Label("База даних: " + config);
        connection.getStyleClass().add("app-connection");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(title, spacer, connection);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("app-header");

        statusLabel.setId("statusLabel");
        HBox statusBar = new HBox(statusLabel);
        statusBar.getStyleClass().add("status-bar");

        BorderPane root = new BorderPane();
        root.setTop(new VBox(buildMenu(), header));
        root.setCenter(tabs);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1400, 800);
        scene.getStylesheets().add(getClass().getResource(StaffApp.STYLES).toExternalForm());
        stage.setScene(scene);
        stage.setTitle(StaffApp.TITLE);
        stage.setMinWidth(1100);
        stage.setMinHeight(640);
    }

    private MenuBar buildMenu() {
        MenuItem export = new MenuItem("Експорт у JSON...");
        export.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN));
        export.setOnAction(e -> chooseExportFile());
        MenuItem importItem = new MenuItem("Імпорт з JSON...");
        importItem.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN));
        importItem.setOnAction(e -> chooseImportFile());
        MenuItem exit = new MenuItem("Вихід");
        exit.setOnAction(e -> stage.close());
        Menu file = new Menu("Файл");
        file.getItems().addAll(export, importItem, new SeparatorMenuItem(), exit);

        MenuItem refresh = new MenuItem("Оновити");
        refresh.setAccelerator(new KeyCodeCombination(KeyCode.F5));
        refresh.setOnAction(e -> currentView().refresh());
        Menu data = new Menu("Дані");
        data.getItems().add(refresh);

        MenuItem about = new MenuItem("Про програму");
        about.setOnAction(e -> Alerts.info(stage, StaffApp.TITLE,
                "Версія 1.0\n\nЗастосунок для обліку працівників і підрозділів підприємства: "
                + "пошук і сортування працівників, нарахування премій, розрахунок середньої заробітної плати, "
                + "експорт та імпорт даних у форматі JSON.\n\n"
                + "Java 21, JavaFX 21, JDBC, PostgreSQL, Gson.\n\n"
                + "Курсова робота з дисципліни «Об'єктно-орієнтоване програмування на мові Java».\n"
                + "Виконав: студент групи Ін-42/1 Конопля Іван, СумДУ, 2026."));
        Menu help = new Menu("Довідка");
        help.getItems().add(about);

        return new MenuBar(file, data, help);
    }

    private BaseView currentView() {
        int index = tabs.getSelectionModel().getSelectedIndex();
        if (index == 1) {
            return departmentsView;
        }
        if (index == 2) {
            return bonusView;
        }
        return employeesView;
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private FileChooser createChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Файли JSON (*.json)", "*.json"));
        chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        return chooser;
    }

    private void chooseExportFile() {
        FileChooser chooser = createChooser("Експорт даних у JSON");
        chooser.setInitialFileName("company_" + LocalDate.now() + ".json");
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            exportTo(file.toPath());
        }
    }

    private void chooseImportFile() {
        File file = createChooser("Імпорт даних з JSON").showOpenDialog(stage);
        if (file != null) {
            importFrom(file.toPath());
        }
    }

    /**
     * Експортує всі дані у файл і показує результат.
     *
     * @param file файл JSON
     */
    public void exportTo(Path file) {
        try {
            CompanyData data = service.exportToJson(file);
            setStatus("Експорт виконано: " + file.getFileName());
            Alerts.info(stage, "Експорт виконано", "Підрозділів: " + data.getDepartments().size()
                    + "\nПрацівників: " + data.getEmployees().size() + "\n\nФайл: " + file.toAbsolutePath());
        } catch (DataAccessException | IOException e) {
            Alerts.error(stage, "Експорт не виконано", e.getMessage());
        }
    }

    /**
     * Імпортує дані з файлу, показує результат і оновлює вкладки.
     *
     * @param file файл JSON
     */
    public void importFrom(Path file) {
        try {
            ImportResult result = service.importFromJson(file);
            currentView().refresh();
            setStatus("Імпорт виконано: " + file.getFileName());
            Alerts.info(stage, "Імпорт виконано", "Файл: " + file.getFileName() + "\n\n" + result);
        } catch (ValidationException e) {
            List<String> errors = e.getErrors();
            StringBuilder sb = new StringBuilder("Дані не імпортовано, база не змінилась.\n\n");
            for (int i = 0; i < errors.size() && i < MAX_ERRORS_SHOWN; i++) {
                sb.append("• ").append(errors.get(i)).append('\n');
            }
            if (errors.size() > MAX_ERRORS_SHOWN) {
                sb.append("... та ще ").append(errors.size() - MAX_ERRORS_SHOWN).append(" помилок");
            }
            Alerts.error(stage, "У файлі є помилки (" + errors.size() + ")", sb.toString());
        } catch (DataAccessException | IOException e) {
            Alerts.error(stage, "Імпорт не виконано", e.getMessage());
        }
    }
}
