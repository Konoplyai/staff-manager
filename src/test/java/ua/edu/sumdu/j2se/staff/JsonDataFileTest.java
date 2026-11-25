package ua.edu.sumdu.j2se.staff;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.edu.sumdu.j2se.staff.io.CompanyData;
import ua.edu.sumdu.j2se.staff.io.JsonDataFile;
import ua.edu.sumdu.j2se.staff.model.Department;
import ua.edu.sumdu.j2se.staff.model.Employee;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonDataFileTest {

    private final JsonDataFile jsonFile = new JsonDataFile();

    private CompanyData sample() {
        List<Department> departments = new ArrayList<>();
        departments.add(new Department(1, "Відділ розробки ПЗ", "Корпус Б, к. 310", "+380542123403"));
        List<Employee> employees = new ArrayList<>();
        Employee boss = TestData.employee(8, "Коваленко", "Андрій", "78000", 1);
        Employee dev = TestData.employee(9, "Бойко", "Дмитро", "72000.50", 1);
        dev.setManagerId(8);
        dev.setDepartmentName("Відділ розробки ПЗ");
        dev.setBonus(new BigDecimal("7200.00"));
        employees.add(boss);
        employees.add(dev);
        return CompanyData.from(departments, employees);
    }

    @Test
    void writeAndReadGiveSameData(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("company.json");
        jsonFile.write(file, sample());
        CompanyData read = jsonFile.read(file);
        assertEquals(1, read.getDepartments().size());
        assertEquals(2, read.getEmployees().size());
        CompanyData.EmployeeItem dev = read.getEmployees().get(1);
        assertEquals("Бойко", dev.lastName);
        assertEquals(new BigDecimal("72000.50"), dev.salary);
        assertEquals(LocalDate.of(2020, 1, 15), dev.hireDate);
        assertEquals("Відділ розробки ПЗ", dev.department);
    }

    @Test
    void managerIsSavedByEmail() {
        CompanyData data = sample();
        assertNull(data.getEmployees().get(0).managerEmail);
        assertEquals("user8@company.ua", data.getEmployees().get(1).managerEmail);
    }

    @Test
    void dateIsWrittenAsText() {
        String json = jsonFile.toJson(sample());
        assertTrue(json.contains("\"hireDate\": \"2020-01-15\""));
        assertTrue(json.contains("\"exportedAt\""));
    }

    @Test
    void brokenFileGivesIoException(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("broken.json");
        Files.writeString(file, "{ \"departments\": [ {\"name\": ", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> jsonFile.read(file));
    }

    @Test
    void wrongDateGivesIoException(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("date.json");
        Files.writeString(file, "{\"departments\": [], \"employees\": [{\"lastName\": \"Бойко\", "
                + "\"hireDate\": \"15.01.2020\"}]}", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> jsonFile.read(file));
    }

    @Test
    void fileWithoutSectionsIsRejected(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("empty.json");
        Files.writeString(file, "{\"exportedAt\": \"2026-11-25\"}", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> jsonFile.read(file));
    }

    @Test
    void missingFileIsReported(@TempDir Path dir) {
        assertThrows(IOException.class, () -> jsonFile.read(dir.resolve("nope.json")));
    }
}
