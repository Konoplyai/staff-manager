package ua.edu.sumdu.j2se.staff.service;

import ua.edu.sumdu.j2se.staff.model.Department;
import ua.edu.sumdu.j2se.staff.model.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Перевірка даних, які вводить користувач або які прочитані з файлу,
 * перед записом у базу даних.
 */
public class InputValidator {

    /** Максимальна заробітна плата, яку можна ввести. */
    public static final BigDecimal MAX_SALARY = new BigDecimal("1000000");
    /** Найраніша дата прийняття на роботу. */
    public static final LocalDate MIN_HIRE_DATE = LocalDate.of(1950, 1, 1);

    private static final Pattern NAME = Pattern.compile("[A-Za-zА-ЩЬЮЯҐЄІЇа-щьюяґєії'’ -]{1,50}");
    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+");
    private static final Pattern PHONE = Pattern.compile("\\+?\\d{10,12}");

    /**
     * Перевіряє дані працівника.
     *
     * @param e працівник
     * @throws ValidationException зі списком усіх помилок
     */
    public void validateEmployee(Employee e) throws ValidationException {
        List<String> errors = new ArrayList<>();
        checkName(e.getLastName(), "Прізвище", true, errors);
        checkName(e.getFirstName(), "Ім'я", true, errors);
        checkName(e.getMiddleName(), "По батькові", false, errors);
        if (isBlank(e.getPosition())) {
            errors.add("Посада: поле не може бути порожнім");
        } else if (e.getPosition().trim().length() > 100) {
            errors.add("Посада: не більше 100 символів");
        }
        if (e.getSalary() == null) {
            errors.add("Заробітна плата: не вказано");
        } else if (e.getSalary().signum() <= 0 || e.getSalary().compareTo(MAX_SALARY) > 0) {
            errors.add("Заробітна плата: має бути більше 0 і не більше " + MAX_SALARY.toPlainString());
        } else if (e.getSalary().stripTrailingZeros().scale() > 2) {
            errors.add("Заробітна плата: не більше двох знаків після коми");
        }
        if (e.getHireDate() == null) {
            errors.add("Дата прийняття: не вказано");
        } else if (e.getHireDate().isAfter(LocalDate.now())) {
            errors.add("Дата прийняття: не може бути в майбутньому");
        } else if (e.getHireDate().isBefore(MIN_HIRE_DATE)) {
            errors.add("Дата прийняття: не раніше " + MIN_HIRE_DATE);
        }
        if (!isBlank(e.getEmail()) && !EMAIL.matcher(e.getEmail().trim()).matches()) {
            errors.add("Email: неправильний формат (приклад: name@company.ua)");
        }
        if (!isBlank(e.getPhone()) && !PHONE.matcher(normalizePhone(e.getPhone())).matches()) {
            errors.add("Телефон: 10-12 цифр, можна з + на початку (приклад: +380501234567)");
        }
        if (e.getDepartmentId() == null && isBlank(e.getDepartmentName())) {
            errors.add("Підрозділ: не вибрано");
        }
        if (e.getManagerId() != null && e.getId() != 0 && e.getManagerId() == e.getId()) {
            errors.add("Керівник: працівник не може бути керівником самого себе");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    /**
     * Перевіряє дані підрозділу.
     *
     * @param d підрозділ
     * @throws ValidationException зі списком усіх помилок
     */
    public void validateDepartment(Department d) throws ValidationException {
        List<String> errors = new ArrayList<>();
        if (isBlank(d.getName())) {
            errors.add("Назва: поле не може бути порожнім");
        } else if (d.getName().trim().length() > 100) {
            errors.add("Назва: не більше 100 символів");
        }
        if (d.getLocation() != null && d.getLocation().trim().length() > 100) {
            errors.add("Розташування: не більше 100 символів");
        }
        if (!isBlank(d.getPhone()) && !PHONE.matcher(normalizePhone(d.getPhone())).matches()) {
            errors.add("Телефон: 10-12 цифр, можна з + на початку");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    /**
     * Перевіряє фрагмент прізвища для пошуку.
     *
     * @param fragment текст пошуку
     * @throws ValidationException якщо в тексті є недопустимі символи
     */
    public void validateSearchText(String fragment) throws ValidationException {
        if (fragment != null && !fragment.trim().isEmpty() && !NAME.matcher(fragment.trim()).matches()) {
            throw new ValidationException("Для пошуку введіть прізвище або його частину (лише літери, апостроф, дефіс)");
        }
    }

    /**
     * Перетворює текст на грошову суму. Допускаються пробіли і кома як роздільник.
     *
     * @param text введений текст
     * @param field назва поля для повідомлення
     * @return сума
     * @throws ValidationException якщо текст не є числом
     */
    public BigDecimal parseMoney(String text, String field) throws ValidationException {
        if (isBlank(text)) {
            throw new ValidationException(field + ": не вказано");
        }
        String s = text.trim().replace(" ", "").replace(" ", "").replace(',', '.');
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            throw new ValidationException(field + ": \"" + text.trim() + "\" не є числом");
        }
    }

    /**
     * Перетворює текст на відсоток премії і перевіряє діапазон (0; 100].
     *
     * @param text введений текст
     * @return відсоток
     * @throws ValidationException якщо текст не є числом або відсоток поза діапазоном
     */
    public BigDecimal parsePercent(String text) throws ValidationException {
        BigDecimal percent = parseMoney(text, "Відсоток премії");
        checkPercent(percent);
        return percent;
    }

    /**
     * Перевіряє, що відсоток у діапазоні (0; 100] і має не більше двох знаків після коми.
     *
     * @param percent відсоток
     * @throws ValidationException якщо відсоток неправильний
     */
    public void checkPercent(BigDecimal percent) throws ValidationException {
        if (percent == null || percent.signum() <= 0 || percent.compareTo(new BigDecimal("100")) > 0) {
            throw new ValidationException("Відсоток премії має бути більше 0 і не більше 100");
        }
        if (percent.stripTrailingZeros().scale() > 2) {
            throw new ValidationException("Відсоток премії: не більше двох знаків після коми");
        }
    }

    /**
     * Прибирає з номера телефону пробіли, дефіси і дужки.
     *
     * @param phone номер
     * @return номер лише з цифрами і, можливо, + на початку
     */
    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        return phone.trim().replace(" ", "").replace("-", "").replace("(", "").replace(")", "");
    }

    private static void checkName(String value, String field, boolean required, List<String> errors) {
        if (isBlank(value)) {
            if (required) {
                errors.add(field + ": поле не може бути порожнім");
            }
        } else if (!NAME.matcher(value.trim()).matches()) {
            errors.add(field + ": лише літери, апостроф і дефіс, до 50 символів");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
