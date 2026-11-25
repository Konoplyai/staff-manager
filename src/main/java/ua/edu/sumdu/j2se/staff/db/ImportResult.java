package ua.edu.sumdu.j2se.staff.db;

/**
 * Результат імпорту: скільки записів додано і скільки оновлено.
 */
public class ImportResult {

    private int departmentsAdded;
    private int departmentsUpdated;
    private int employeesAdded;
    private int employeesUpdated;

    /** Збільшує лічильник доданих підрозділів. */
    public void departmentAdded() {
        departmentsAdded++;
    }

    /** Збільшує лічильник оновлених підрозділів. */
    public void departmentUpdated() {
        departmentsUpdated++;
    }

    /** Збільшує лічильник доданих працівників. */
    public void employeeAdded() {
        employeesAdded++;
    }

    /** Збільшує лічильник оновлених працівників. */
    public void employeeUpdated() {
        employeesUpdated++;
    }

    /**
     * Повертає кількість доданих підрозділів.
     *
     * @return кількість
     */
    public int getDepartmentsAdded() {
        return departmentsAdded;
    }

    /**
     * Повертає кількість оновлених підрозділів.
     *
     * @return кількість
     */
    public int getDepartmentsUpdated() {
        return departmentsUpdated;
    }

    /**
     * Повертає кількість доданих працівників.
     *
     * @return кількість
     */
    public int getEmployeesAdded() {
        return employeesAdded;
    }

    /**
     * Повертає кількість оновлених працівників.
     *
     * @return кількість
     */
    public int getEmployeesUpdated() {
        return employeesUpdated;
    }

    @Override
    public String toString() {
        return "Підрозділів додано: " + departmentsAdded + ", оновлено: " + departmentsUpdated
                + "\nПрацівників додано: " + employeesAdded + ", оновлено: " + employeesUpdated;
    }
}
