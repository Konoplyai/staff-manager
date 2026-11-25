package ua.edu.sumdu.j2se.staff;

import ua.edu.sumdu.j2se.staff.db.DataAccessException;
import ua.edu.sumdu.j2se.staff.db.DataImporter;
import ua.edu.sumdu.j2se.staff.db.DepartmentDao;
import ua.edu.sumdu.j2se.staff.db.EmployeeDao;
import ua.edu.sumdu.j2se.staff.db.ImportResult;
import ua.edu.sumdu.j2se.staff.io.CompanyData;
import ua.edu.sumdu.j2se.staff.model.BonusRecord;
import ua.edu.sumdu.j2se.staff.model.Department;
import ua.edu.sumdu.j2se.staff.model.DepartmentStatistics;
import ua.edu.sumdu.j2se.staff.model.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Тестові дані та реалізації DAO у пам'яті, щоб перевіряти сервіс без PostgreSQL.
 */
public final class TestData {

    private TestData() {
    }

    /**
     * Створює працівника.
     *
     * @param id ідентифікатор
     * @param lastName прізвище
     * @param firstName ім'я
     * @param salary заробітна плата
     * @param departmentId підрозділ
     * @return працівник
     */
    public static Employee employee(int id, String lastName, String firstName, String salary, Integer departmentId) {
        Employee e = new Employee(lastName, firstName, "Іванович", "Інженер", new BigDecimal(salary),
                LocalDate.of(2020, 1, 15));
        e.setId(id);
        e.setDepartmentId(departmentId);
        e.setDepartmentName(departmentId == null ? null : "Підрозділ " + departmentId);
        e.setEmail("user" + id + "@company.ua");
        return e;
    }

    /**
     * Працівники в пам'яті.
     */
    public static class MemoryEmployeeDao implements EmployeeDao {
        /** Список працівників. */
        public final List<Employee> employees = new ArrayList<>();
        /** Скільки разів викликали accrueBonus. */
        public int accrueCalls;
        private int nextId = 100;

        @Override
        public List<Employee> findAll() {
            return new ArrayList<>(employees);
        }

        @Override
        public List<Employee> findByLastName(String fragment) {
            List<Employee> result = new ArrayList<>();
            for (Employee e : employees) {
                if (e.getLastName().toLowerCase().contains(fragment.toLowerCase())) {
                    result.add(e);
                }
            }
            return result;
        }

        @Override
        public List<Employee> findByDepartment(int departmentId) {
            List<Employee> result = new ArrayList<>();
            for (Employee e : employees) {
                if (e.getDepartmentId() != null && e.getDepartmentId() == departmentId) {
                    result.add(e);
                }
            }
            return result;
        }

        @Override
        public int add(Employee employee) {
            employee.setId(nextId++);
            employees.add(employee);
            return employee.getId();
        }

        @Override
        public void update(Employee employee) {
        }

        @Override
        public void delete(int id) {
            employees.removeIf(e -> e.getId() == id);
        }

        @Override
        public int accrueBonus(int departmentId, BigDecimal percent) {
            accrueCalls++;
            List<Employee> list = findByDepartment(departmentId);
            for (Employee e : list) {
                e.setBonus(e.getBonus().add(ua.edu.sumdu.j2se.staff.service.StaffService
                        .calculateBonus(e.getSalary(), percent)));
            }
            return list.size();
        }

        @Override
        public List<BonusRecord> findBonusHistory(int departmentId) {
            return new ArrayList<>();
        }
    }

    /**
     * Підрозділи в пам'яті.
     */
    public static class MemoryDepartmentDao implements DepartmentDao {
        /** Список підрозділів. */
        public final List<Department> departments = new ArrayList<>();
        /** Скільки разів викликали delete. */
        public int deleteCalls;

        @Override
        public List<Department> findAll() {
            return new ArrayList<>(departments);
        }

        @Override
        public int add(Department department) {
            department.setId(departments.size() + 1);
            departments.add(department);
            return department.getId();
        }

        @Override
        public void update(Department department) {
        }

        @Override
        public void delete(int id) {
            deleteCalls++;
            departments.removeIf(d -> d.getId() == id);
        }

        @Override
        public List<DepartmentStatistics> getStatistics() throws DataAccessException {
            throw new DataAccessException("не потрібно в тестах");
        }

        @Override
        public DepartmentStatistics getStatistics(Department department) throws DataAccessException {
            throw new DataAccessException("не потрібно в тестах");
        }
    }

    /**
     * Імпортер, який лише запам'ятовує, що його викликали.
     */
    public static class MemoryImporter implements DataImporter {
        /** Отримані дані. */
        public CompanyData received;

        @Override
        public ImportResult importData(CompanyData data) {
            received = data;
            ImportResult result = new ImportResult();
            for (int i = 0; i < data.getEmployees().size(); i++) {
                result.employeeAdded();
            }
            return result;
        }
    }
}
