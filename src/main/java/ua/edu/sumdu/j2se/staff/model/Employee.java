package ua.edu.sumdu.j2se.staff.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Працівник підприємства (рядок таблиці employees).
 * Поля departmentName і managerName не зберігаються в таблиці,
 * вони заповнюються під час читання із з'єднання таблиць і потрібні для відображення.
 */
public class Employee {

    private int id;
    private String lastName;
    private String firstName;
    private String middleName;
    private String position;
    private BigDecimal salary = BigDecimal.ZERO;
    private BigDecimal bonus = BigDecimal.ZERO;
    private LocalDate hireDate;
    private String email;
    private String phone;
    private Integer departmentId;
    private String departmentName;
    private Integer managerId;
    private String managerName;

    /**
     * Створює порожнього працівника (id = 0 означає, що він ще не збережений у базі).
     */
    public Employee() {
    }

    /**
     * Створює працівника з основними полями.
     *
     * @param lastName прізвище
     * @param firstName ім'я
     * @param middleName по батькові
     * @param position посада
     * @param salary заробітна плата
     * @param hireDate дата прийняття на роботу
     */
    public Employee(String lastName, String firstName, String middleName, String position,
                    BigDecimal salary, LocalDate hireDate) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.position = position;
        this.salary = salary;
        this.hireDate = hireDate;
    }

    /**
     * Повертає прізвище, ім'я та по батькові одним рядком.
     *
     * @return ПІБ
     */
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(lastName == null ? "" : lastName);
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(' ').append(firstName);
        }
        if (middleName != null && !middleName.isEmpty()) {
            sb.append(' ').append(middleName);
        }
        return sb.toString();
    }

    /**
     * Повертає прізвище та ініціали, наприклад "Бойко Д. О.".
     *
     * @return прізвище з ініціалами
     */
    public String getShortName() {
        StringBuilder sb = new StringBuilder(lastName == null ? "" : lastName);
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(' ').append(firstName.charAt(0)).append('.');
        }
        if (middleName != null && !middleName.isEmpty()) {
            sb.append(' ').append(middleName.charAt(0)).append('.');
        }
        return sb.toString();
    }

    /**
     * Повертає ідентифікатор.
     *
     * @return ідентифікатор
     */
    public int getId() {
        return id;
    }

    /**
     * Задає ідентифікатор.
     *
     * @param id ідентифікатор
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Повертає прізвище.
     *
     * @return прізвище
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Задає прізвище.
     *
     * @param lastName прізвище
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Повертає ім'я.
     *
     * @return ім'я
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Задає ім'я.
     *
     * @param firstName ім'я
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Повертає по батькові.
     *
     * @return по батькові
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Задає по батькові.
     *
     * @param middleName по батькові
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * Повертає посаду.
     *
     * @return посада
     */
    public String getPosition() {
        return position;
    }

    /**
     * Задає посаду.
     *
     * @param position посада
     */
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * Повертає заробітну плату.
     *
     * @return заробітна плата
     */
    public BigDecimal getSalary() {
        return salary;
    }

    /**
     * Задає заробітну плату.
     *
     * @param salary заробітна плата
     */
    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    /**
     * Повертає суму нарахованих премій.
     *
     * @return премія
     */
    public BigDecimal getBonus() {
        return bonus;
    }

    /**
     * Задає суму нарахованих премій.
     *
     * @param bonus премія
     */
    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    /**
     * Повертає дату прийняття на роботу.
     *
     * @return дата прийняття
     */
    public LocalDate getHireDate() {
        return hireDate;
    }

    /**
     * Задає дату прийняття на роботу.
     *
     * @param hireDate дата прийняття
     */
    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    /**
     * Повертає електронну пошту.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Задає електронну пошту.
     *
     * @param email email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Повертає телефон.
     *
     * @return телефон
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Задає телефон.
     *
     * @param phone телефон
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Повертає ідентифікатор підрозділу.
     *
     * @return ідентифікатор підрозділу або null
     */
    public Integer getDepartmentId() {
        return departmentId;
    }

    /**
     * Задає ідентифікатор підрозділу.
     *
     * @param departmentId ідентифікатор підрозділу або null
     */
    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    /**
     * Повертає назву підрозділу.
     *
     * @return назва підрозділу
     */
    public String getDepartmentName() {
        return departmentName;
    }

    /**
     * Задає назву підрозділу.
     *
     * @param departmentName назва підрозділу
     */
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    /**
     * Повертає ідентифікатор безпосереднього керівника.
     *
     * @return ідентифікатор керівника або null
     */
    public Integer getManagerId() {
        return managerId;
    }

    /**
     * Задає ідентифікатор безпосереднього керівника.
     *
     * @param managerId ідентифікатор керівника або null
     */
    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    /**
     * Повертає прізвище та ініціали керівника.
     *
     * @return керівник
     */
    public String getManagerName() {
        return managerName;
    }

    /**
     * Задає прізвище та ініціали керівника.
     *
     * @param managerName керівник
     */
    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Employee)) {
            return false;
        }
        Employee other = (Employee) o;
        return id == other.id && Objects.equals(getFullName(), other.getFullName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getFullName());
    }

    /**
     * Прізвище та ініціали (так працівник показується у списку вибору керівника).
     *
     * @return прізвище з ініціалами
     */
    @Override
    public String toString() {
        return getShortName();
    }
}
