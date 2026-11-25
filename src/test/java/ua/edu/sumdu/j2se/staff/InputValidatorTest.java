package ua.edu.sumdu.j2se.staff;

import org.junit.jupiter.api.Test;
import ua.edu.sumdu.j2se.staff.model.Department;
import ua.edu.sumdu.j2se.staff.model.Employee;
import ua.edu.sumdu.j2se.staff.service.InputValidator;
import ua.edu.sumdu.j2se.staff.service.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputValidatorTest {

    private final InputValidator validator = new InputValidator();

    @Test
    void correctEmployeePasses() {
        Employee e = TestData.employee(1, "Ковальчук", "Олександр", "45000.50", 1);
        e.setPhone("+380501112233");
        assertDoesNotThrow(() -> validator.validateEmployee(e));
    }

    @Test
    void apostropheAndHyphenAreAllowedInNames() {
        Employee e = TestData.employee(1, "Сем'янчук-Бойко", "Дар'я", "30000", 1);
        assertDoesNotThrow(() -> validator.validateEmployee(e));
    }

    @Test
    void emptyEmployeeHasAllErrors() {
        Employee e = new Employee();
        e.setSalary(null);
        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateEmployee(e));
        assertEquals(6, ex.getErrors().size());
    }

    @Test
    void digitsInLastNameAreRejected() {
        Employee e = TestData.employee(1, "Бойко2", "Дмитро", "30000", 1);
        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateEmployee(e));
        assertTrue(ex.getMessage().startsWith("Прізвище"));
    }

    @Test
    void salaryMustBePositiveAndNotTooBig() {
        Employee e = TestData.employee(1, "Бойко", "Дмитро", "0", 1);
        assertThrows(ValidationException.class, () -> validator.validateEmployee(e));
        e.setSalary(new BigDecimal("1000000.01"));
        assertThrows(ValidationException.class, () -> validator.validateEmployee(e));
        e.setSalary(new BigDecimal("100.123"));
        assertThrows(ValidationException.class, () -> validator.validateEmployee(e));
    }

    @Test
    void hireDateCannotBeInFuture() {
        Employee e = TestData.employee(1, "Бойко", "Дмитро", "30000", 1);
        e.setHireDate(LocalDate.now().plusDays(1));
        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateEmployee(e));
        assertTrue(ex.getMessage().contains("майбутньому"));
    }

    @Test
    void wrongEmailAndPhoneAreRejected() {
        Employee e = TestData.employee(1, "Бойко", "Дмитро", "30000", 1);
        e.setEmail("boiko.company.ua");
        e.setPhone("12-34");
        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateEmployee(e));
        assertEquals(2, ex.getErrors().size());
    }

    @Test
    void employeeCannotBeHisOwnManager() {
        Employee e = TestData.employee(5, "Бойко", "Дмитро", "30000", 1);
        e.setManagerId(5);
        assertThrows(ValidationException.class, () -> validator.validateEmployee(e));
    }

    @Test
    void departmentNameIsRequired() {
        assertThrows(ValidationException.class, () -> validator.validateDepartment(new Department(0, " ", null, null)));
        assertDoesNotThrow(() -> validator.validateDepartment(new Department(0, "Відділ логістики", "Корпус В",
                "+380542123499")));
    }

    @Test
    void parseMoneyAcceptsCommaAndSpaces() throws ValidationException {
        assertEquals(new BigDecimal("32500.50"), validator.parseMoney("32 500,50", "Сума"));
        assertThrows(ValidationException.class, () -> validator.parseMoney("десять", "Сума"));
        assertThrows(ValidationException.class, () -> validator.parseMoney("", "Сума"));
    }

    @Test
    void percentMustBeInRange() throws ValidationException {
        assertEquals(new BigDecimal("12.5"), validator.parsePercent("12,5"));
        assertThrows(ValidationException.class, () -> validator.parsePercent("0"));
        assertThrows(ValidationException.class, () -> validator.parsePercent("-5"));
        assertThrows(ValidationException.class, () -> validator.parsePercent("150"));
    }

    @Test
    void searchTextAllowsOnlyLetters() {
        assertDoesNotThrow(() -> validator.validateSearchText("ко"));
        assertThrows(ValidationException.class, () -> validator.validateSearchText("%' OR 1=1 --"));
    }

    @Test
    void phoneIsNormalized() {
        assertEquals("+380501234567", InputValidator.normalizePhone(" +38 (050) 123-45-67 "));
    }
}
