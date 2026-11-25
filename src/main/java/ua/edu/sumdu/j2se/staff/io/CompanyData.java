package ua.edu.sumdu.j2se.staff.io;

import ua.edu.sumdu.j2se.staff.model.Department;
import ua.edu.sumdu.j2se.staff.model.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Дані про підрозділи та працівників у вигляді, який записується у файл JSON.
 * Замість ідентифікаторів бази тут зберігаються назва підрозділу та email керівника,
 * тому файл можна імпортувати в іншу базу, де ідентифікатори будуть іншими.
 */
public class CompanyData {

    /** Дата і час формування файлу. */
    private String exportedAt;
    /** Підрозділи. */
    private List<DepartmentItem> departments;
    /** Працівники. */
    private List<EmployeeItem> employees;

    /**
     * Підрозділ у файлі JSON.
     */
    public static class DepartmentItem {
        /** Назва. */
        public String name;
        /** Розташування. */
        public String location;
        /** Телефон. */
        public String phone;
    }

    /**
     * Працівник у файлі JSON.
     */
    public static class EmployeeItem {
        /** Прізвище. */
        public String lastName;
        /** Ім'я. */
        public String firstName;
        /** По батькові. */
        public String middleName;
        /** Посада. */
        public String position;
        /** Заробітна плата. */
        public BigDecimal salary;
        /** Нараховані премії. */
        public BigDecimal bonus;
        /** Дата прийняття на роботу. */
        public LocalDate hireDate;
        /** Email (за ним працівник шукається під час імпорту). */
        public String email;
        /** Телефон. */
        public String phone;
        /** Назва підрозділу. */
        public String department;
        /** Email безпосереднього керівника. */
        public String managerEmail;
    }

    /**
     * Формує дані для експорту зі списків підрозділів і працівників.
     *
     * @param departmentList підрозділи
     * @param employeeList працівники
     * @return дані для запису у файл
     */
    public static CompanyData from(List<Department> departmentList, List<Employee> employeeList) {
        CompanyData data = new CompanyData();
        data.departments = new ArrayList<>();
        data.employees = new ArrayList<>();
        data.exportedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        for (Department d : departmentList) {
            DepartmentItem item = new DepartmentItem();
            item.name = d.getName();
            item.location = d.getLocation();
            item.phone = d.getPhone();
            data.departments.add(item);
        }
        Map<Integer, String> emailById = new HashMap<>();
        for (Employee e : employeeList) {
            emailById.put(e.getId(), e.getEmail());
        }
        for (Employee e : employeeList) {
            EmployeeItem item = new EmployeeItem();
            item.lastName = e.getLastName();
            item.firstName = e.getFirstName();
            item.middleName = e.getMiddleName();
            item.position = e.getPosition();
            item.salary = e.getSalary();
            item.bonus = e.getBonus();
            item.hireDate = e.getHireDate();
            item.email = e.getEmail();
            item.phone = e.getPhone();
            item.department = e.getDepartmentName();
            item.managerEmail = e.getManagerId() == null ? null : emailById.get(e.getManagerId());
            data.employees.add(item);
        }
        return data;
    }

    /**
     * Перетворює запис файлу на об'єкт Employee (без ідентифікаторів підрозділу та керівника).
     *
     * @param item запис з файлу
     * @return працівник
     */
    public static Employee toEmployee(EmployeeItem item) {
        Employee e = new Employee(item.lastName, item.firstName, item.middleName, item.position,
                item.salary, item.hireDate);
        e.setBonus(item.bonus == null ? BigDecimal.ZERO : item.bonus);
        e.setEmail(item.email);
        e.setPhone(item.phone);
        e.setDepartmentName(item.department);
        return e;
    }

    /**
     * Перетворює запис файлу на об'єкт Department.
     *
     * @param item запис з файлу
     * @return підрозділ
     */
    public static Department toDepartment(DepartmentItem item) {
        return new Department(0, item.name, item.location, item.phone);
    }

    /**
     * Повертає дату формування файлу.
     *
     * @return дата і час
     */
    public String getExportedAt() {
        return exportedAt;
    }

    /**
     * Повертає підрозділи.
     *
     * @return підрозділи
     */
    public List<DepartmentItem> getDepartments() {
        return departments;
    }

    /**
     * Повертає працівників.
     *
     * @return працівники
     */
    public List<EmployeeItem> getEmployees() {
        return employees;
    }
}
