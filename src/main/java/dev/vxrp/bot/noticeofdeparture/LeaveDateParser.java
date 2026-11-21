/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.noticeofdeparture;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Parses dates typed into the notice of departure modal. Accepts the configured format, looser numeric variants of it
 * (single digits, two-digit or missing year, any of {@code . / -} as separator) and durations such as {@code 2w} or
 * {@code 1w 3d}, which count from the given base date.
 */
public final class LeaveDateParser {
    private static final Pattern DURATION = Pattern.compile("(?:\\d{1,3}\\s*\\p{L}+\\s*)+");
    private static final Pattern DURATION_PART = Pattern.compile("(\\d{1,3})\\s*(\\p{L}+)");
    private static final Pattern DATE_SEPARATOR = Pattern.compile("[./\\-\\s]+");
    private static final Map<String, ChronoUnit> UNITS = Map.ofEntries(
            Map.entry("d", ChronoUnit.DAYS), Map.entry("day", ChronoUnit.DAYS), Map.entry("days", ChronoUnit.DAYS),
            Map.entry("t", ChronoUnit.DAYS), Map.entry("tag", ChronoUnit.DAYS), Map.entry("tage", ChronoUnit.DAYS),
            Map.entry("w", ChronoUnit.WEEKS), Map.entry("week", ChronoUnit.WEEKS), Map.entry("weeks", ChronoUnit.WEEKS),
            Map.entry("wo", ChronoUnit.WEEKS), Map.entry("woche", ChronoUnit.WEEKS), Map.entry("wochen", ChronoUnit.WEEKS),
            Map.entry("m", ChronoUnit.MONTHS), Map.entry("month", ChronoUnit.MONTHS), Map.entry("months", ChronoUnit.MONTHS),
            Map.entry("monat", ChronoUnit.MONTHS), Map.entry("monate", ChronoUnit.MONTHS));

    private final DateTimeFormatter formatter;
    private final List<ChronoField> fieldOrder;

    public LeaveDateParser(String pattern) {
        this.formatter = DateTimeFormatter.ofPattern(pattern);
        this.fieldOrder = Stream.of(ChronoField.DAY_OF_MONTH, ChronoField.MONTH_OF_YEAR, ChronoField.YEAR)
                .filter(field -> pattern.indexOf(letter(field)) >= 0)
                .sorted(Comparator.comparingInt(field -> pattern.indexOf(letter(field))))
                .toList();
    }

    public Optional<LocalDate> parse(String input, LocalDate base) {
        String value = input.strip().toLowerCase(Locale.ROOT);
        return parseDuration(value, base).or(() -> parseDate(value, base));
    }

    private Optional<LocalDate> parseDuration(String value, LocalDate base) {
        if (!DURATION.matcher(value).matches()) return Optional.empty();

        LocalDate result = base;
        Matcher part = DURATION_PART.matcher(value);
        while (part.find()) {
            ChronoUnit unit = UNITS.get(part.group(2));
            if (unit == null) return Optional.empty();
            result = result.plus(Integer.parseInt(part.group(1)), unit);
        }
        return Optional.of(result);
    }

    private Optional<LocalDate> parseDate(String value, LocalDate base) {
        try {
            return Optional.of(LocalDate.parse(value, formatter));
        } catch (DateTimeException ignored) {
        }

        String[] parts = DATE_SEPARATOR.split(value);
        boolean hasYear = parts.length == fieldOrder.size();
        if (!hasYear && parts.length != fieldOrder.size() - 1) return Optional.empty();

        List<ChronoField> order = hasYear ? fieldOrder : fieldOrder.stream().filter(f -> f != ChronoField.YEAR).toList();
        int day = 0, month = 0, year = base.getYear();
        try {
            for (int i = 0; i < parts.length; i++) {
                int number = Integer.parseInt(parts[i]);
                switch (order.get(i)) {
                    case DAY_OF_MONTH -> day = number;
                    case MONTH_OF_YEAR -> month = number;
                    default -> year = number < 100 ? 2000 + number : number;
                }
            }
            LocalDate date = LocalDate.of(year, month, day);
            return Optional.of(!hasYear && date.isBefore(base) ? date.plusYears(1) : date);
        } catch (NumberFormatException | DateTimeException e) {
            return Optional.empty();
        }
    }

    private static char letter(ChronoField field) {
        return switch (field) {
            case DAY_OF_MONTH -> 'd';
            case MONTH_OF_YEAR -> 'M';
            default -> 'y';
        };
    }
}
