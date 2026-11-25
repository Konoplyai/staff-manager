package ua.edu.sumdu.j2se.staff;

import org.junit.jupiter.api.Test;
import ua.edu.sumdu.j2se.staff.model.Employee;
import ua.edu.sumdu.j2se.staff.service.EmployeeComparators;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeComparatorsTest {

    private static List<String> names(List<Employee> list) {
        List<String> result = new ArrayList<>();
        for (Employee e : list) {
            result.add(e.getLastName());
        }
        return result;
    }

    @Test
    void sortsByUkrainianAlphabet() {
        List<Employee> list = new ArrayList<>();
        list.add(TestData.employee(1, "Яковенко", "Ігор", "1000", 1));
        list.add(TestData.employee(2, "Іваненко", "Ігор", "1000", 1));
        list.add(TestData.employee(3, "Єрмоленко", "Ігор", "1000", 1));
        list.add(TestData.employee(4, "Ґудзь", "Ігор", "1000", 1));
        list.add(TestData.employee(5, "Бойко", "Ігор", "1000", 1));
        list.add(TestData.employee(6, "Їжакевич", "Ігор", "1000", 1));
        list.add(TestData.employee(7, "Гончар", "Ігор", "1000", 1));
        List<Employee> sorted = EmployeeComparators.sortByFullName(list, true);
        assertEquals(List.of("Бойко", "Гончар", "Ґудзь", "Єрмоленко", "Іваненко", "Їжакевич", "Яковенко"),
                names(sorted));
    }

    @Test
    void descendingOrderIsReversed() {
        List<Employee> list = new ArrayList<>();
        list.add(TestData.employee(1, "Бойко", "Дмитро", "1000", 1));
        list.add(TestData.employee(2, "Шевчук", "Тетяна", "1000", 1));
        list.add(TestData.employee(3, "Мельник", "Ірина", "1000", 1));
        assertEquals(List.of("Шевчук", "Мельник", "Бойко"), names(EmployeeComparators.sortByFullName(list, false)));
    }

    @Test
    void sameLastNameIsSortedByFirstName() {
        List<Employee> list = new ArrayList<>();
        list.add(TestData.employee(1, "Коваль", "Юлія", "1000", 1));
        list.add(TestData.employee(2, "Коваль", "Артем", "1000", 1));
        List<Employee> sorted = EmployeeComparators.sortByFullName(list, true);
        assertEquals("Артем", sorted.get(0).getFirstName());
    }

    @Test
    void sortingDoesNotChangeSourceList() {
        List<Employee> list = new ArrayList<>();
        list.add(TestData.employee(1, "Шевчук", "Тетяна", "1000", 1));
        list.add(TestData.employee(2, "Бойко", "Дмитро", "1000", 1));
        EmployeeComparators.sortByFullName(list, true);
        assertEquals("Шевчук", list.get(0).getLastName());
    }
}
