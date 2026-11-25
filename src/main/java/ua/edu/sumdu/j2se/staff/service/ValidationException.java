package ua.edu.sumdu.j2se.staff.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Помилка перевірки введених даних. Містить список усіх знайдених помилок,
 * щоб користувач міг виправити їх за один раз.
 */
public class ValidationException extends Exception {

    private final List<String> errors;

    /**
     * Створює виняток з однією помилкою.
     *
     * @param error текст помилки
     */
    public ValidationException(String error) {
        super(error);
        this.errors = new ArrayList<>();
        this.errors.add(error);
    }

    /**
     * Створює виняток зі списком помилок.
     *
     * @param errors тексти помилок
     */
    public ValidationException(List<String> errors) {
        super(String.join("\n", errors));
        this.errors = new ArrayList<>(errors);
    }

    /**
     * Повертає список помилок.
     *
     * @return помилки
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
}
