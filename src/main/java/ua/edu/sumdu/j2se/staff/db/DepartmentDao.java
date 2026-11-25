package ua.edu.sumdu.j2se.staff.db;

import ua.edu.sumdu.j2se.staff.model.Department;
import ua.edu.sumdu.j2se.staff.model.DepartmentStatistics;

import java.util.List;

/**
 * Операції з таблицею підрозділів.
 */
public interface DepartmentDao {

    /**
     * Повертає всі підрозділи, впорядковані за назвою.
     *
     * @return список підрозділів
     * @throws DataAccessException якщо запит не виконався
     */
    List<Department> findAll() throws DataAccessException;

    /**
     * Додає підрозділ.
     *
     * @param department новий підрозділ
     * @return ідентифікатор, який призначила база
     * @throws DataAccessException якщо запис не додано
     */
    int add(Department department) throws DataAccessException;

    /**
     * Оновлює підрозділ.
     *
     * @param department підрозділ зі зміненими полями
     * @throws DataAccessException якщо запис не оновлено
     */
    void update(Department department) throws DataAccessException;

    /**
     * Видаляє підрозділ.
     *
     * @param id ідентифікатор
     * @throws DataAccessException якщо в підрозділі є працівники або запит не виконався
     */
    void delete(int id) throws DataAccessException;

    /**
     * Обчислює показники заробітної плати для кожного підрозділу.
     *
     * @return показники всіх підрозділів
     * @throws DataAccessException якщо запит не виконався
     */
    List<DepartmentStatistics> getStatistics() throws DataAccessException;

    /**
     * Обчислює показники заробітної плати вибраного підрозділу.
     *
     * @param department підрозділ
     * @return показники підрозділу
     * @throws DataAccessException якщо запит не виконався
     */
    DepartmentStatistics getStatistics(Department department) throws DataAccessException;
}
