package ua.edu.sumdu.j2se.staff.service;

import ua.edu.sumdu.j2se.staff.model.Employee;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Порівняння працівників для сортування.
 * Для ПІБ використовується Collator з українською локаллю: звичайне compareTo
 * порівнює коди символів, і тоді літери І, Ї, Є, Ґ опиняються після Я.
 */
public final class EmployeeComparators {

    private static final Collator UKRAINIAN = Collator.getInstance(Locale.forLanguageTag("uk-UA"));

    static {
        UKRAINIAN.setStrength(Collator.SECONDARY);
    }

    private EmployeeComparators() {
    }

    /**
     * Порівняння за прізвищем, потім за ім'ям, потім за по батькові (за українським алфавітом).
     *
     * @return компаратор за ПІБ
     */
    public static Comparator<Employee> byFullName() {
        return (a, b) -> {
            int result = compareText(a.getLastName(), b.getLastName());
            if (result == 0) {
                result = compareText(a.getFirstName(), b.getFirstName());
            }
            if (result == 0) {
                result = compareText(a.getMiddleName(), b.getMiddleName());
            }
            return result;
        };
    }

    /**
     * Повертає новий список, відсортований за ПІБ.
     *
     * @param employees вихідний список (не змінюється)
     * @param ascending true - від А до Я, false - від Я до А
     * @return відсортований список
     */
    public static List<Employee> sortByFullName(List<Employee> employees, boolean ascending) {
        List<Employee> sorted = new ArrayList<>(employees);
        Comparator<Employee> comparator = byFullName();
        sorted.sort(ascending ? comparator : comparator.reversed());
        return sorted;
    }

    private static int compareText(String a, String b) {
        return UKRAINIAN.compare(a == null ? "" : a, b == null ? "" : b);
    }
}
