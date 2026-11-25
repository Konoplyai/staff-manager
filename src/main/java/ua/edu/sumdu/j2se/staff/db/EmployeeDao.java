package ua.edu.sumdu.j2se.staff.db;

import ua.edu.sumdu.j2se.staff.model.BonusRecord;
import ua.edu.sumdu.j2se.staff.model.Employee;

import java.math.BigDecimal;
import java.util.List;

/**
 * Операції з таблицею працівників та історією премій.
 */
public interface EmployeeDao {

    /**
     * Повертає всіх працівників разом з назвою підрозділу і керівником.
     *
     * @return список працівників
     * @throws DataAccessException якщо запит не виконався
     */
    List<Employee> findAll() throws DataAccessException;

    /**
     * Шукає працівників, прізвище яких містить заданий фрагмент (без урахування регістру).
     *
     * @param fragment прізвище або його частина
     * @return знайдені працівники
     * @throws DataAccessException якщо запит не виконався
     */
    List<Employee> findByLastName(String fragment) throws DataAccessException;

    /**
     * Повертає працівників підрозділу.
     *
     * @param departmentId ідентифікатор підрозділу
     * @return працівники підрозділу
     * @throws DataAccessException якщо запит не виконався
     */
    List<Employee> findByDepartment(int departmentId) throws DataAccessException;

    /**
     * Додає працівника.
     *
     * @param employee новий працівник
     * @return ідентифікатор, який призначила база
     * @throws DataAccessException якщо запис не додано
     */
    int add(Employee employee) throws DataAccessException;

    /**
     * Оновлює дані працівника.
     *
     * @param employee працівник зі зміненими полями
     * @throws DataAccessException якщо запис не оновлено
     */
    void update(Employee employee) throws DataAccessException;

    /**
     * Видаляє працівника.
     *
     * @param id ідентифікатор
     * @throws DataAccessException якщо запит не виконався
     */
    void delete(int id) throws DataAccessException;

    /**
     * Нараховує премію всім працівникам підрозділу у відсотках від заробітної плати:
     * збільшує поле bonus і додає записи в історію премій (в одній транзакції).
     *
     * @param departmentId ідентифікатор підрозділу
     * @param percent відсоток
     * @return кількість працівників, яким нараховано премію
     * @throws DataAccessException якщо транзакцію не виконано
     */
    int accrueBonus(int departmentId, BigDecimal percent) throws DataAccessException;

    /**
     * Повертає історію премій працівників підрозділу (нові записи першими).
     *
     * @param departmentId ідентифікатор підрозділу
     * @return записи про премії
     * @throws DataAccessException якщо запит не виконався
     */
    List<BonusRecord> findBonusHistory(int departmentId) throws DataAccessException;
}
