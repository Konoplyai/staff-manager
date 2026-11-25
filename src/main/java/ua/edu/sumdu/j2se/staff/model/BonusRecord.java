package ua.edu.sumdu.j2se.staff.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Запис про нараховану премію (рядок таблиці bonus_history).
 */
public class BonusRecord {

    private final int id;
    private final int employeeId;
    private final String employeeName;
    private final BigDecimal percent;
    private final BigDecimal amount;
    private final LocalDate accrualDate;

    /**
     * Створює запис про премію.
     *
     * @param id ідентифікатор запису
     * @param employeeId ідентифікатор працівника
     * @param employeeName ПІБ працівника
     * @param percent відсоток від заробітної плати
     * @param amount сума премії
     * @param accrualDate дата нарахування
     */
    public BonusRecord(int id, int employeeId, String employeeName, BigDecimal percent,
                       BigDecimal amount, LocalDate accrualDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.percent = percent;
        this.amount = amount;
        this.accrualDate = accrualDate;
    }

    /**
     * Повертає ідентифікатор запису.
     *
     * @return ідентифікатор
     */
    public int getId() {
        return id;
    }

    /**
     * Повертає ідентифікатор працівника.
     *
     * @return ідентифікатор працівника
     */
    public int getEmployeeId() {
        return employeeId;
    }

    /**
     * Повертає ПІБ працівника.
     *
     * @return ПІБ
     */
    public String getEmployeeName() {
        return employeeName;
    }

    /**
     * Повертає відсоток.
     *
     * @return відсоток
     */
    public BigDecimal getPercent() {
        return percent;
    }

    /**
     * Повертає суму премії.
     *
     * @return сума
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Повертає дату нарахування.
     *
     * @return дата
     */
    public LocalDate getAccrualDate() {
        return accrualDate;
    }
}
