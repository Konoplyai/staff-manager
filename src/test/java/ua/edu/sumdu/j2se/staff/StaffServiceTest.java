package ua.edu.sumdu.j2se.staff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.edu.sumdu.j2se.staff.db.DataAccessException;
import ua.edu.sumdu.j2se.staff.db.ImportResult;
import ua.edu.sumdu.j2se.staff.io.CompanyData;
import ua.edu.sumdu.j2se.staff.io.JsonDataFile;
import ua.edu.sumdu.j2se.staff.model.Department;
import ua.edu.sumdu.j2se.staff.model.Employee;
import ua.edu.sumdu.j2se.staff.service.StaffService;
import ua.edu.sumdu.j2se.staff.service.ValidationException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StaffServiceTest {

    private TestData.MemoryEmployeeDao employeeDao;
    private TestData.MemoryDepartmentDao departmentDao;
    private TestData.MemoryImporter importer;
    private StaffService service;
    private Department development;

    @BeforeEach
    void setUp() {
        employeeDao = new TestData.MemoryEmployeeDao();
        departmentDao = new TestData.MemoryDepartmentDao();
        importer = new TestData.MemoryImporter();
        service = new StaffService(employeeDao, departmentDao, importer, new JsonDataFile());
        development = new Department(1, "Відділ розробки ПЗ", "Корпус Б", null);
        departmentDao.departments.add(development);
        departmentDao.departments.add(new Department(2, "Бухгалтерія", "Корпус А", null));
        employeeDao.employees.add(TestData.employee(1, "Ткачук", "Максим", "54000", 1));
        employeeDao.employees.add(TestData.employee(2, "Кравчук", "Юлія", "52500", 1));
        employeeDao.employees.add(TestData.employee(3, "Поліщук", "Владислав", "32000", 1));
        employeeDao.employees.add(TestData.employee(4, "Кравченко", "Оксана", "29500", 2));
    }

    @Test
    void emptySearchReturnsAllEmployees() throws Exception {
        assertEquals(4, service.searchByLastName("  ").size());
    }

    @Test
    void searchByFragmentOfLastName() throws Exception {
        assertEquals(2, service.searchByLastName("кравч").size());
        assertEquals(3, service.searchByLastName("ук").size());
    }

    @Test
    void searchWithWrongSymbolsIsRejected() {
        assertThrows(ValidationException.class, () -> service.searchByLastName("12%"));
    }

    @Test
    void bonusIsRoundedToKopecks() {
        assertEquals(new BigDecimal("3333.33"), StaffService.calculateBonus(new BigDecimal("33333.33"), BigDecimal.TEN));
        assertEquals(new BigDecimal("4125.00"), StaffService.calculateBonus(new BigDecimal("55000"), new BigDecimal("7.5")));
    }

    @Test
    void bonusTotalForDepartment() throws DataAccessException {
        List<Employee> list = service.getEmployees(development);
        assertEquals(new BigDecimal("13850.00"), StaffService.calculateBonusTotal(list, BigDecimal.TEN));
    }

    @Test
    void accrueBonusUpdatesOnlySelectedDepartment() throws Exception {
        int count = service.accrueBonus(development, new BigDecimal("10"));
        assertEquals(3, count);
        assertEquals(new BigDecimal("5400.00"), employeeDao.employees.get(0).getBonus());
        assertEquals(BigDecimal.ZERO, employeeDao.employees.get(3).getBonus());
    }

    @Test
    void wrongPercentDoesNotReachDatabase() {
        assertThrows(ValidationException.class, () -> service.accrueBonus(development, new BigDecimal("0")));
        assertThrows(ValidationException.class, () -> service.accrueBonus(development, new BigDecimal("101")));
        assertThrows(ValidationException.class, () -> service.accrueBonus(null, BigDecimal.TEN));
        assertEquals(0, employeeDao.accrueCalls);
    }

    @Test
    void averageSalaryOfDepartment() throws DataAccessException {
        assertEquals(new BigDecimal("46166.67"), StaffService.averageSalary(service.getEmployees(development)));
        assertEquals(new BigDecimal("0.00"), StaffService.averageSalary(new ArrayList<>()));
    }

    @Test
    void departmentWithEmployeesIsNotDeleted() {
        assertThrows(ValidationException.class, () -> service.deleteDepartment(development));
        assertEquals(0, departmentDao.deleteCalls);
    }

    @Test
    void emptyDepartmentIsDeleted() throws Exception {
        Department empty = new Department(3, "Склад", null, null);
        departmentDao.departments.add(empty);
        service.deleteDepartment(empty);
        assertEquals(1, departmentDao.deleteCalls);
    }

    @Test
    void addEmployeeTrimsFieldsAndNormalizesPhone() throws Exception {
        Employee e = TestData.employee(0, "  Лисенко ", " Софія", "30500", 1);
        e.setMiddleName("  ");
        e.setPhone("+38 (050) 111-22-14");
        service.addEmployee(e);
        assertEquals("Лисенко", e.getLastName());
        assertNull(e.getMiddleName());
        assertEquals("+380501112214", e.getPhone());
        assertEquals(5, employeeDao.employees.size());
    }

    @Test
    void importWithErrorsDoesNotChangeDatabase(@TempDir Path dir) throws IOException {
        CompanyData data = CompanyData.from(departmentDao.findAll(), employeeDao.findAll());
        data.getEmployees().get(1).salary = new BigDecimal("-100");
        data.getEmployees().get(2).email = data.getEmployees().get(0).email;
        Path file = dir.resolve("bad.json");
        new JsonDataFile().write(file, data);
        ValidationException ex = assertThrows(ValidationException.class, () -> service.importFromJson(file));
        assertEquals(2, ex.getErrors().size());
        assertNull(importer.received);
    }

    @Test
    void correctFileIsPassedToImporter(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("company.json");
        service.exportToJson(file);
        ImportResult result = service.importFromJson(file);
        assertEquals(4, result.getEmployeesAdded());
        assertEquals(2, importer.received.getDepartments().size());
    }
}
