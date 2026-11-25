package ua.edu.sumdu.j2se.staff.ui;

import javafx.application.Application;
import javafx.stage.Stage;
import ua.edu.sumdu.j2se.staff.db.ConnectionManager;
import ua.edu.sumdu.j2se.staff.db.DatabaseConfig;
import ua.edu.sumdu.j2se.staff.db.JdbcDataImporter;
import ua.edu.sumdu.j2se.staff.db.JdbcDepartmentDao;
import ua.edu.sumdu.j2se.staff.db.JdbcEmployeeDao;
import ua.edu.sumdu.j2se.staff.io.JsonDataFile;
import ua.edu.sumdu.j2se.staff.service.StaffService;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Застосунок JavaFX: спочатку показує вікно входу, після успішного підключення - головне вікно.
 */
public class StaffApp extends Application {

    /** Назва застосунку для заголовків вікон. */
    public static final String TITLE = "Облік працівників підприємства";
    /** Таблиця стилів. */
    public static final String STYLES = "/ua/edu/sumdu/j2se/staff/ui/styles.css";

    @Override
    public void start(Stage stage) {
        DatabaseConfig defaults;
        try {
            defaults = DatabaseConfig.loadDefaults(Paths.get(DatabaseConfig.DEFAULT_FILE));
        } catch (IOException e) {
            defaults = new DatabaseConfig("localhost", 5432, "company_db", "company_user", "");
        }
        LoginWindow login = new LoginWindow(stage, defaults, this::openMainWindow);
        login.show();
    }

    private void openMainWindow(ConnectionManager connectionManager) {
        StaffService service = new StaffService(new JdbcEmployeeDao(connectionManager),
                new JdbcDepartmentDao(connectionManager), new JdbcDataImporter(connectionManager),
                new JsonDataFile());
        MainWindow window = new MainWindow(new Stage(), service, connectionManager.getConfig());
        window.show();
    }
}
