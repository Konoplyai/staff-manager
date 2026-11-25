package ua.edu.sumdu.j2se.staff.service;

import ua.edu.sumdu.j2se.staff.db.DataAccessException;
import ua.edu.sumdu.j2se.staff.db.DataImporter;
import ua.edu.sumdu.j2se.staff.db.DepartmentDao;
import ua.edu.sumdu.j2se.staff.db.EmployeeDao;
import ua.edu.sumdu.j2se.staff.db.ImportResult;
import ua.edu.sumdu.j2se.staff.io.CompanyData;
import ua.edu.sumdu.j2se.staff.io.JsonDataFile;
import ua.edu.sumdu.j2se.staff.model.BonusRecord;
import ua.edu.sumdu.j2se.staff.model.Department;
import ua.edu.sumdu.j2se.staff.model.DepartmentStatistics;
import ua.edu.sumdu.j2se.staff.model.Employee;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Прикладна логіка застосунку. Графічний інтерфейс звертається лише до цього класу,
 * а він перевіряє дані і викликає відповідні DAO.
 */
public class StaffService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final EmployeeDao employeeDao;
    private final DepartmentDao departmentDao;
    private final DataImporter importer;
    private final JsonDataFile jsonFile;
    private final InputValidator validator = new InputValidator();

    /**
     * Створює сервіс.
     *
     * @param employeeDao доступ до працівників
     * @param departmentDao доступ до підрозділів
     * @param importer запис імпортованих даних у базу
     * @param jsonFile робота з файлами JSON
     */
    public StaffService(EmployeeDao employeeDao, DepartmentDao departmentDao, DataImporter importer,
                        JsonDataFile jsonFile) {
        this.employeeDao = employeeDao;
        this.departmentDao = departmentDao;
        this.importer = importer;
        this.jsonFile = jsonFile;
    }

    /**
     * Повертає валідатор (його використовують діалоги для розбору чисел).
     *
     * @return валідатор
     */
    public InputValidator getValidator() {
        return validator;
    }

    /**
     * Повертає всіх працівників.
     *
     * @return працівники
     * @throws DataAccessException якщо запит не виконався
     */
    public List<Employee> getEmployees() throws DataAccessException {
        return employeeDao.findAll();
    }

    /**
     * Шукає працівників за прізвищем або його фрагментом. Порожній текст повертає всіх.
     *
     * @param fragment прізвище або його частина
     * @return знайдені працівники
     * @throws ValidationException якщо текст пошуку неправильний
     * @throws DataAccessException якщо запит не виконався
     */
    public List<Employee> searchByLastName(String fragment) throws ValidationException, DataAccessException {
        if (fragment == null || fragment.trim().isEmpty()) {
            return employeeDao.findAll();
        }
        validator.validateSearchText(fragment);
        return employeeDao.findByLastName(fragment.trim());
    }

    /**
     * Повертає працівників підрозділу.
     *
     * @param department підрозділ
     * @return працівники
     * @throws DataAccessException якщо запит не виконався
     */
    public List<Employee> getEmployees(Department department) throws DataAccessException {
        return employeeDao.findByDepartment(department.getId());
    }

    /**
     * Перевіряє і додає працівника.
     *
     * @param employee новий працівник
     * @throws ValidationException якщо дані неправильні
     * @throws DataAccessException якщо запис не додано
     */
    public void addEmployee(Employee employee) throws ValidationException, DataAccessException {
        prepare(employee);
        employeeDao.add(employee);
    }

    /**
     * Перевіряє і зберігає змінені дані працівника.
     *
     * @param employee працівник
     * @throws ValidationException якщо дані неправильні
     * @throws DataAccessException якщо запис не оновлено
     */
    public void updateEmployee(Employee employee) throws ValidationException, DataAccessException {
        prepare(employee);
        employeeDao.update(employee);
    }

    /**
     * Видаляє працівника. Підлеглим цього працівника керівник очищується (ON DELETE SET NULL).
     *
     * @param employee працівник
     * @throws DataAccessException якщо запис не видалено
     */
    public void deleteEmployee(Employee employee) throws DataAccessException {
        employeeDao.delete(employee.getId());
    }

    /**
     * Повертає всі підрозділи.
     *
     * @return підрозділи
     * @throws DataAccessException якщо запит не виконався
     */
    public List<Department> getDepartments() throws DataAccessException {
        return departmentDao.findAll();
    }

    /**
     * Перевіряє і додає підрозділ.
     *
     * @param department новий підрозділ
     * @throws ValidationException якщо дані неправильні
     * @throws DataAccessException якщо запис не додано
     */
    public void addDepartment(Department department) throws ValidationException, DataAccessException {
        prepare(department);
        departmentDao.add(department);
    }

    /**
     * Перевіряє і зберігає змінений підрозділ.
     *
     * @param department підрозділ
     * @throws ValidationException якщо дані неправильні
     * @throws DataAccessException якщо запис не оновлено
     */
    public void updateDepartment(Department department) throws ValidationException, DataAccessException {
        prepare(department);
        departmentDao.update(department);
    }

    /**
     * Видаляє підрозділ, якщо в ньому немає працівників.
     *
     * @param department підрозділ
     * @throws ValidationException якщо в підрозділі є працівники
     * @throws DataAccessException якщо запис не видалено
     */
    public void deleteDepartment(Department department) throws ValidationException, DataAccessException {
        int count = employeeDao.findByDepartment(department.getId()).size();
        if (count > 0) {
            throw new ValidationException("У підрозділі \"" + department.getName() + "\" працює " + count
                    + " осіб. Спочатку переведіть їх до іншого підрозділу або видаліть.");
        }
        departmentDao.delete(department.getId());
    }

    /**
     * Обчислює премію одного працівника.
     *
     * @param salary заробітна плата
     * @param percent відсоток
     * @return премія, округлена до копійок
     */
    public static BigDecimal calculateBonus(BigDecimal salary, BigDecimal percent) {
        return salary.multiply(percent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /**
     * Обчислює загальну суму премії для списку працівників.
     *
     * @param employees працівники
     * @param percent відсоток
     * @return сума премій
     */
    public static BigDecimal calculateBonusTotal(List<Employee> employees, BigDecimal percent) {
        BigDecimal total = BigDecimal.ZERO;
        for (Employee e : employees) {
            total = total.add(calculateBonus(e.getSalary(), percent));
        }
        return total;
    }

    /**
     * Обчислює середню заробітну плату працівників.
     *
     * @param employees працівники
     * @return середня заробітна плата або 0, якщо список порожній
     */
    public static BigDecimal averageSalary(List<Employee> employees) {
        if (employees.isEmpty()) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (Employee e : employees) {
            sum = sum.add(e.getSalary());
        }
        return sum.divide(BigDecimal.valueOf(employees.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Нараховує премію працівникам вибраного підрозділу і зберігає її в базі.
     *
     * @param department підрозділ
     * @param percent відсоток від поточної заробітної плати
     * @return кількість працівників, яким нараховано премію
     * @throws ValidationException якщо підрозділ не вибрано, відсоток неправильний або в підрозділі нікого немає
     * @throws DataAccessException якщо транзакцію не виконано
     */
    public int accrueBonus(Department department, BigDecimal percent)
            throws ValidationException, DataAccessException {
        if (department == null) {
            throw new ValidationException("Виберіть підрозділ");
        }
        validator.checkPercent(percent);
        int count = employeeDao.accrueBonus(department.getId(), percent);
        if (count == 0) {
            throw new ValidationException("У підрозділі \"" + department.getName() + "\" немає працівників");
        }
        return count;
    }

    /**
     * Повертає історію премій підрозділу.
     *
     * @param department підрозділ
     * @return записи про премії
     * @throws DataAccessException якщо запит не виконався
     */
    public List<BonusRecord> getBonusHistory(Department department) throws DataAccessException {
        return employeeDao.findBonusHistory(department.getId());
    }

    /**
     * Повертає показники заробітної плати підрозділу (у тому числі середню).
     *
     * @param department підрозділ
     * @return показники
     * @throws DataAccessException якщо запит не виконався
     */
    public DepartmentStatistics getStatistics(Department department) throws DataAccessException {
        return departmentDao.getStatistics(department);
    }

    /**
     * Повертає показники всіх підрозділів.
     *
     * @return показники
     * @throws DataAccessException якщо запит не виконався
     */
    public List<DepartmentStatistics> getAllStatistics() throws DataAccessException {
        return departmentDao.getStatistics();
    }

    /**
     * Записує всі підрозділи і працівників у файл JSON.
     *
     * @param file файл
     * @return записані дані
     * @throws DataAccessException якщо дані не прочитано з бази
     * @throws IOException якщо файл не записано
     */
    public CompanyData exportToJson(Path file) throws DataAccessException, IOException {
        CompanyData data = CompanyData.from(departmentDao.findAll(), employeeDao.findAll());
        jsonFile.write(file, data);
        return data;
    }

    /**
     * Читає файл JSON, перевіряє всі записи і додає або оновлює їх у базі.
     *
     * @param file файл JSON
     * @return кількість доданих і оновлених записів
     * @throws IOException якщо файл не прочитано
     * @throws ValidationException якщо у файлі є неправильні записи (тоді база не змінюється)
     * @throws DataAccessException якщо запис у базу не вдався
     */
    public ImportResult importFromJson(Path file) throws IOException, ValidationException, DataAccessException {
        CompanyData data = jsonFile.read(file);
        List<String> errors = new ArrayList<>();
        Set<String> departmentNames = new HashSet<>();
        for (int i = 0; i < data.getDepartments().size(); i++) {
            CompanyData.DepartmentItem item = data.getDepartments().get(i);
            try {
                validator.validateDepartment(CompanyData.toDepartment(item));
                if (!departmentNames.add(item.name.trim())) {
                    errors.add("Підрозділ №" + (i + 1) + ": назва \"" + item.name + "\" повторюється");
                }
            } catch (ValidationException e) {
                addErrors(errors, "Підрозділ №" + (i + 1), e);
            }
        }
        Set<String> emails = new HashSet<>();
        for (int i = 0; i < data.getEmployees().size(); i++) {
            CompanyData.EmployeeItem item = data.getEmployees().get(i);
            try {
                validator.validateEmployee(CompanyData.toEmployee(item));
                if (item.email != null && !emails.add(item.email.trim().toLowerCase())) {
                    errors.add("Працівник №" + (i + 1) + ": email " + item.email + " повторюється");
                }
            } catch (ValidationException e) {
                addErrors(errors, "Працівник №" + (i + 1) + " (" + item.lastName + ")", e);
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return importer.importData(data);
    }

    private static void addErrors(List<String> errors, String prefix, ValidationException e) {
        for (String error : e.getErrors()) {
            errors.add(prefix + ": " + error);
        }
    }

    private void prepare(Employee e) throws ValidationException {
        e.setLastName(trim(e.getLastName()));
        e.setFirstName(trim(e.getFirstName()));
        e.setMiddleName(emptyToNull(e.getMiddleName()));
        e.setPosition(trim(e.getPosition()));
        e.setEmail(emptyToNull(e.getEmail()));
        e.setPhone(emptyToNull(InputValidator.normalizePhone(e.getPhone())));
        validator.validateEmployee(e);
    }

    private void prepare(Department d) throws ValidationException {
        d.setName(trim(d.getName()));
        d.setLocation(emptyToNull(d.getLocation()));
        d.setPhone(emptyToNull(InputValidator.normalizePhone(d.getPhone())));
        validator.validateDepartment(d);
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String emptyToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
