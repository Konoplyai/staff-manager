package ua.edu.sumdu.j2se.staff.ui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import ua.edu.sumdu.j2se.staff.db.DataAccessException;
import ua.edu.sumdu.j2se.staff.model.BonusRecord;
import ua.edu.sumdu.j2se.staff.model.Department;
import ua.edu.sumdu.j2se.staff.model.DepartmentStatistics;
import ua.edu.sumdu.j2se.staff.model.Employee;
import ua.edu.sumdu.j2se.staff.service.StaffService;
import ua.edu.sumdu.j2se.staff.service.ValidationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Вкладка "Премії та заробітна плата": нарахування премії працівникам вибраного підрозділу,
 * середня заробітна плата та інші показники підрозділу, діаграма середньої заробітної плати
 * за підрозділами та історія премій.
 */
public class BonusView extends BaseView {

    private final BorderPane root = new BorderPane();
    private final ComboBox<Department> departmentBox = new ComboBox<>();
    private final TextField percentField = new TextField();
    private final Label previewLabel = new Label();

    private final Label countValue = new Label();
    private final Label averageValue = new Label();
    private final Label minValue = new Label();
    private final Label maxValue = new Label();
    private final Label fundValue = new Label();
    private final Label bonusValue = new Label();

    private final CategoryAxis xAxis = new CategoryAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private final BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
    private final TableView<BonusRecord> historyTable = new TableView<>();
    private final Label historyTitle = new Label("Історія премій");

    private List<Employee> departmentEmployees = List.of();

    /**
     * Створює вкладку.
     *
     * @param service сервіс
     * @param owner головне вікно
     * @param status рядок стану
     */
    public BonusView(StaffService service, Window owner, Consumer<String> status) {
        super(service, owner, status);
        build();
    }

    @Override
    public Node getContent() {
        return root;
    }

    @Override
    public void refresh() {
        Department selected = departmentBox.getValue();
        try {
            List<Department> departments = service.getDepartments();
            departmentBox.getItems().setAll(departments);
            departmentBox.setValue(null);
            for (Department d : departments) {
                if (selected != null && d.getId() == selected.getId()) {
                    departmentBox.setValue(d);
                }
            }
            if (departmentBox.getValue() == null && !departments.isEmpty()) {
                departmentBox.setValue(departments.get(0));
            }
            updateChart(service.getAllStatistics());
            showDepartment();
        } catch (DataAccessException e) {
            showError(e);
        }
    }

    private void build() {
        Label bonusTitle = new Label("Нарахування премії");
        bonusTitle.getStyleClass().add("card-title");
        departmentBox.setId("bonusDepartment");
        departmentBox.setMaxWidth(Double.MAX_VALUE);
        departmentBox.setOnAction(e -> showDepartment());
        percentField.setId("bonusPercent");
        percentField.setPromptText("наприклад 10");
        percentField.textProperty().addListener((obs, old, now) -> updatePreview());
        previewLabel.setId("bonusPreview");
        previewLabel.setWrapText(true);
        previewLabel.getStyleClass().add("preview-label");
        Button accrue = new Button("Нарахувати премію");
        accrue.setId("accrueButton");
        accrue.getStyleClass().add("primary");
        accrue.setMaxWidth(Double.MAX_VALUE);
        accrue.setOnAction(e -> accrue());
        VBox bonusCard = new VBox(8, bonusTitle, new Label("Підрозділ:"), departmentBox,
                new Label("Відсоток від поточної заробітної плати, %:"), percentField, previewLabel, accrue);
        bonusCard.getStyleClass().add("card");

        Label statsTitle = new Label("Показники підрозділу");
        statsTitle.getStyleClass().add("card-title");
        averageValue.setId("averageSalary");
        averageValue.getStyleClass().add("big-value");
        Label averageCaption = new Label("Середня заробітна плата");
        averageCaption.getStyleClass().add("hint-label");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        grid.addRow(0, new Label("Працівників:"), countValue);
        grid.addRow(1, new Label("Мінімальна ЗП:"), minValue);
        grid.addRow(2, new Label("Максимальна ЗП:"), maxValue);
        grid.addRow(3, new Label("Фонд оплати праці:"), fundValue);
        grid.addRow(4, new Label("Нараховано премій:"), bonusValue);
        for (Label l : new Label[] {countValue, minValue, maxValue, fundValue, bonusValue}) {
            l.getStyleClass().add("value-label");
        }
        VBox statsCard = new VBox(8, statsTitle, averageCaption, averageValue, grid);
        statsCard.getStyleClass().add("card");

        VBox left = new VBox(12, bonusCard, statsCard);
        left.setPrefWidth(330);
        left.setMinWidth(330);

        chart.setTitle("Середня заробітна плата за підрозділами, грн");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCategoryGap(18);
        chart.setPrefHeight(300);
        chart.setMinHeight(260);
        xAxis.setTickLabelRotation(0);
        yAxis.setForceZeroInRange(true);

        historyTitle.getStyleClass().add("section-title");
        historyTable.setId("bonusHistory");
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        historyTable.setPlaceholder(new Label("Премії в цьому підрозділі ще не нараховувались"));
        historyTable.getColumns().add(column("Дата", 100, b -> Formats.date(b.getAccrualDate())));
        historyTable.getColumns().add(column("Працівник", 300, BonusRecord::getEmployeeName));
        historyTable.getColumns().add(column("Відсоток, %", 100, b -> b.getPercent().stripTrailingZeros().toPlainString()));
        historyTable.getColumns().add(column("Сума, грн", 130, b -> Formats.money(b.getAmount())));
        VBox right = new VBox(8, chart, historyTitle, historyTable);
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox content = new HBox(14, left, right);
        root.setCenter(content);
        root.setPadding(new Insets(12));
    }

    private static TableColumn<BonusRecord, String> column(String title, double width,
                                                           Function<BonusRecord, String> getter) {
        TableColumn<BonusRecord, String> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(data -> new ReadOnlyStringWrapper(getter.apply(data.getValue())));
        return c;
    }

    private void showDepartment() {
        Department d = departmentBox.getValue();
        if (d == null) {
            return;
        }
        try {
            DepartmentStatistics s = service.getStatistics(d);
            departmentEmployees = service.getEmployees(d);
            countValue.setText(String.valueOf(s.getEmployeeCount()));
            averageValue.setText(Formats.hryvnia(s.getAverageSalary()));
            minValue.setText(Formats.hryvnia(s.getMinSalary()));
            maxValue.setText(Formats.hryvnia(s.getMaxSalary()));
            fundValue.setText(Formats.hryvnia(s.getSalaryFund()));
            bonusValue.setText(Formats.hryvnia(s.getBonusTotal()));
            List<BonusRecord> history = service.getBonusHistory(d);
            historyTable.getItems().setAll(history);
            historyTitle.setText("Історія премій підрозділу «" + d.getName() + "» (записів: " + history.size() + ")");
            highlight(d.getName());
            updatePreview();
            setStatus("Підрозділ «" + d.getName() + "»: середня заробітна плата " + Formats.hryvnia(s.getAverageSalary()));
        } catch (DataAccessException e) {
            showError(e);
        }
    }

    private void updatePreview() {
        String text = percentField.getText();
        if (text == null || text.trim().isEmpty()) {
            previewLabel.setText("Введіть відсоток, щоб побачити суму премії");
            return;
        }
        try {
            BigDecimal percent = service.getValidator().parsePercent(text);
            BigDecimal total = StaffService.calculateBonusTotal(departmentEmployees, percent);
            previewLabel.setText("Буде нараховано " + departmentEmployees.size() + " працівникам, разом "
                    + Formats.hryvnia(total));
        } catch (ValidationException e) {
            previewLabel.setText(e.getMessage());
        }
    }

    private void accrue() {
        Department d = departmentBox.getValue();
        try {
            if (d == null) {
                throw new ValidationException("Виберіть підрозділ");
            }
            BigDecimal percent = service.getValidator().parsePercent(percentField.getText());
            BigDecimal total = StaffService.calculateBonusTotal(departmentEmployees, percent);
            boolean yes = Alerts.confirm(owner, "Нарахувати премію " + percent.stripTrailingZeros().toPlainString()
                    + "% працівникам підрозділу «" + d.getName() + "»?",
                    "Кількість працівників: " + departmentEmployees.size() + "\nЗагальна сума премії: "
                    + Formats.hryvnia(total) + "\n\nЗміни будуть записані в базу даних.");
            if (!yes) {
                return;
            }
            int count = service.accrueBonus(d, percent);
            refresh();
            percentField.clear();
            setStatus("Премію нараховано " + count + " працівникам підрозділу «" + d.getName() + "»");
            Alerts.info(owner, "Премію нараховано", "Підрозділ: " + d.getName() + "\nПрацівників: " + count
                    + "\nЗагальна сума: " + Formats.hryvnia(total));
        } catch (ValidationException | DataAccessException e) {
            showError(e);
        }
    }

    private void updateChart(List<DepartmentStatistics> statistics) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (DepartmentStatistics s : statistics) {
            series.getData().add(new XYChart.Data<>(shortName(s.getDepartment().getName()),
                    s.getAverageSalary().doubleValue()));
        }
        chart.getData().clear();
        chart.getData().add(series);
    }

    private void highlight(String departmentName) {
        if (chart.getData().isEmpty()) {
            return;
        }
        String selected = shortName(departmentName);
        for (XYChart.Data<String, Number> item : chart.getData().get(0).getData()) {
            if (item.getNode() != null) {
                item.getNode().getStyleClass().remove("selected-bar");
                if (item.getXValue().equals(selected)) {
                    item.getNode().getStyleClass().add("selected-bar");
                }
            }
        }
    }

    private static String shortName(String name) {
        // довгі назви обрізаються, щоб підписи на осі не накладались
        return name.length() > 22 ? name.substring(0, 21) + "…" : name;
    }
}
