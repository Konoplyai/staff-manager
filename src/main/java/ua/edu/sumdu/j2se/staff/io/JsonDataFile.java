package ua.edu.sumdu.j2se.staff.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Запис і читання даних про підрозділи та працівників у форматі JSON (бібліотека Gson).
 */
public class JsonDataFile {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    /**
     * Записує дані у файл (UTF-8).
     *
     * @param file файл
     * @param data дані
     * @throws IOException якщо записати не вдалося
     */
    public void write(Path file, CompanyData data) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        }
    }

    /**
     * Читає дані з файлу.
     *
     * @param file файл JSON
     * @return прочитані дані
     * @throws IOException якщо файл не прочитано або він має неправильний формат
     */
    public CompanyData read(Path file) throws IOException {
        if (!Files.exists(file)) {
            throw new IOException("Файл " + file.getFileName() + " не знайдено");
        }
        CompanyData data;
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            data = gson.fromJson(reader, CompanyData.class);
        } catch (JsonParseException | DateTimeParseException e) {
            throw new IOException("Файл має неправильний формат JSON: " + e.getMessage(), e);
        }
        if (data == null || data.getDepartments() == null || data.getEmployees() == null) {
            throw new IOException("У файлі немає розділів \"departments\" та \"employees\"");
        }
        return data;
    }

    /**
     * Перетворює дані на рядок JSON (використовується в тестах).
     *
     * @param data дані
     * @return текст JSON
     */
    public String toJson(CompanyData data) {
        return gson.toJson(data);
    }

    /**
     * Адаптер Gson для дат у форматі yyyy-MM-dd.
     */
    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {

        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDate.parse(in.nextString());
        }
    }
}
