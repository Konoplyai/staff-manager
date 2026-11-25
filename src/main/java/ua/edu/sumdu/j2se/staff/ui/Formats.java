package ua.edu.sumdu.j2se.staff.ui;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Форматування грошових сум і дат для відображення в таблицях.
 */
public final class Formats {

    /** Формат дати дд.мм.рррр. */
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final DecimalFormat MONEY;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');
        MONEY = new DecimalFormat("#,##0.00", symbols);
    }

    private Formats() {
    }

    /**
     * Форматує суму: 85000 → "85 000,00".
     *
     * @param value сума
     * @return рядок або порожній рядок для null
     */
    public static String money(BigDecimal value) {
        return value == null ? "" : MONEY.format(value);
    }

    /**
     * Форматує суму з позначенням валюти: "85 000,00 грн".
     *
     * @param value сума
     * @return рядок
     */
    public static String hryvnia(BigDecimal value) {
        return money(value) + " грн";
    }

    /**
     * Форматує дату.
     *
     * @param date дата
     * @return рядок дд.мм.рррр або порожній рядок
     */
    public static String date(LocalDate date) {
        return date == null ? "" : date.format(DATE);
    }
}
