package ua.edu.sumdu.j2se.staff;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.edu.sumdu.j2se.staff.db.DataAccessException;
import ua.edu.sumdu.j2se.staff.db.DatabaseConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConfigTest {

    @Test
    void urlIsBuiltFromParts() {
        DatabaseConfig c = new DatabaseConfig(" localhost ", 5432, "company_db", "company_user", "secret");
        assertEquals("jdbc:postgresql://localhost:5432/company_db", c.getUrl());
        assertEquals("company_user@localhost:5432/company_db", c.toString());
        assertFalse(c.toString().contains("secret"));
    }

    @Test
    void wrongParametersAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DatabaseConfig("", 5432, "db", "u", ""));
        assertThrows(IllegalArgumentException.class, () -> new DatabaseConfig("localhost", 70000, "db", "u", ""));
        assertThrows(IllegalArgumentException.class, () -> new DatabaseConfig("localhost", 5432, "db", " ", ""));
    }

    @Test
    void defaultsAreReadFromFile(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("app.properties");
        Files.writeString(file, "db.host=db.local\ndb.port=6543\ndb.name=hr\ndb.user=hr_user\n", StandardCharsets.UTF_8);
        DatabaseConfig c = DatabaseConfig.loadDefaults(file);
        assertEquals("jdbc:postgresql://db.local:6543/hr", c.getUrl());
        assertEquals("", c.getPassword());
    }

    @Test
    void missingFileGivesStandardValues(@TempDir Path dir) throws IOException {
        DatabaseConfig c = DatabaseConfig.loadDefaults(dir.resolve("none.properties"));
        assertEquals("jdbc:postgresql://localhost:5432/company_db", c.getUrl());
    }

    @Test
    void sqlStateIsTranslated() {
        DataAccessException e = DataAccessException.of("Не вдалося підключитися",
                new SQLException("password authentication failed", "28P01"));
        assertTrue(e.getMessage().contains("неправильне ім'я користувача або пароль"));
        DataAccessException fk = DataAccessException.of("Не вдалося видалити", new SQLException("fk", "23503"));
        assertTrue(fk.getMessage().contains("посилаються"));
    }
}
