package ua.edu.sumdu.j2se.staff;

import javafx.application.Application;
import ua.edu.sumdu.j2se.staff.ui.StaffApp;

/**
 * Точка входу. Окремий клас потрібен, щоб застосунок запускався з JavaFX у classpath
 * (головний клас не повинен успадковувати Application).
 */
public final class Launcher {

    private Launcher() {
    }

    /**
     * Запускає застосунок.
     *
     * @param args аргументи командного рядка (не використовуються)
     */
    public static void main(String[] args) {
        Application.launch(StaffApp.class, args);
    }
}
