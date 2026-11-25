package ua.edu.sumdu.j2se.staff.model;

import java.util.Objects;

/**
 * Підрозділ підприємства (рядок таблиці departments).
 */
public class Department {

    private int id;
    private String name;
    private String location;
    private String phone;

    /**
     * Створює порожній підрозділ (id = 0 означає, що він ще не збережений у базі).
     */
    public Department() {
    }

    /**
     * Створює підрозділ із заданими полями.
     *
     * @param id ідентифікатор
     * @param name назва
     * @param location розташування
     * @param phone телефон
     */
    public Department(int id, String name, String location, String phone) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.phone = phone;
    }

    /**
     * Повертає ідентифікатор.
     *
     * @return ідентифікатор
     */
    public int getId() {
        return id;
    }

    /**
     * Задає ідентифікатор.
     *
     * @param id ідентифікатор
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Повертає назву.
     *
     * @return назва
     */
    public String getName() {
        return name;
    }

    /**
     * Задає назву.
     *
     * @param name назва
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Повертає розташування.
     *
     * @return розташування
     */
    public String getLocation() {
        return location;
    }

    /**
     * Задає розташування.
     *
     * @param location розташування
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Повертає телефон.
     *
     * @return телефон
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Задає телефон.
     *
     * @param phone телефон
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Department)) {
            return false;
        }
        Department other = (Department) o;
        return id == other.id && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    /**
     * Назва підрозділу (так він показується у списках вибору).
     *
     * @return назва
     */
    @Override
    public String toString() {
        return name;
    }
}
