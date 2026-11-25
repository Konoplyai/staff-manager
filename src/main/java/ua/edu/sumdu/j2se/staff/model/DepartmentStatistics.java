package ua.edu.sumdu.j2se.staff.model;

import java.math.BigDecimal;

/**
 * Показники заробітної плати підрозділу: кількість працівників, середня, мінімальна
 * та максимальна заробітна плата, фонд оплати праці і сума нарахованих премій.
 */
public class DepartmentStatistics {

    private final Department department;
    private final int employeeCount;
    private final BigDecimal averageSalary;
    private final BigDecimal minSalary;
    private final BigDecimal maxSalary;
    private final BigDecimal salaryFund;
    private final BigDecimal bonusTotal;

    /**
     * Створює показники підрозділу.
     *
     * @param department підрозділ
     * @param employeeCount кількість працівників
     * @param averageSalary середня заробітна плата
     * @param minSalary мінімальна заробітна плата
     * @param maxSalary максимальна заробітна плата
     * @param salaryFund сума заробітних плат
     * @param bonusTotal сума нарахованих премій
     */
    public DepartmentStatistics(Department department, int employeeCount, BigDecimal averageSalary,
                                BigDecimal minSalary, BigDecimal maxSalary, BigDecimal salaryFund,
                                BigDecimal bonusTotal) {
        this.department = department;
        this.employeeCount = employeeCount;
        this.averageSalary = averageSalary;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.salaryFund = salaryFund;
        this.bonusTotal = bonusTotal;
    }

    /**
     * Повертає підрозділ.
     *
     * @return підрозділ
     */
    public Department getDepartment() {
        return department;
    }

    /**
     * Повертає кількість працівників.
     *
     * @return кількість
     */
    public int getEmployeeCount() {
        return employeeCount;
    }

    /**
     * Повертає середню заробітну плату.
     *
     * @return середня заробітна плата
     */
    public BigDecimal getAverageSalary() {
        return averageSalary;
    }

    /**
     * Повертає мінімальну заробітну плату.
     *
     * @return мінімальна заробітна плата
     */
    public BigDecimal getMinSalary() {
        return minSalary;
    }

    /**
     * Повертає максимальну заробітну плату.
     *
     * @return максимальна заробітна плата
     */
    public BigDecimal getMaxSalary() {
        return maxSalary;
    }

    /**
     * Повертає фонд оплати праці (суму заробітних плат).
     *
     * @return фонд оплати праці
     */
    public BigDecimal getSalaryFund() {
        return salaryFund;
    }

    /**
     * Повертає суму нарахованих премій.
     *
     * @return сума премій
     */
    public BigDecimal getBonusTotal() {
        return bonusTotal;
    }
}
