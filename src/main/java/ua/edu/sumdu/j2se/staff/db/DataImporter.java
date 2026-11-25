package ua.edu.sumdu.j2se.staff.db;

import ua.edu.sumdu.j2se.staff.io.CompanyData;

/**
 * Запис імпортованих даних у базу.
 */
public interface DataImporter {

    /**
     * Додає або оновлює підрозділи та працівників з файлу.
     * Підрозділ шукається за назвою, працівник - за email.
     *
     * @param data дані з файлу
     * @return кількість доданих і оновлених записів
     * @throws DataAccessException якщо імпорт не виконано (зміни скасовуються)
     */
    ImportResult importData(CompanyData data) throws DataAccessException;
}
