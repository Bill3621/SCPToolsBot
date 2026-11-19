/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain a copy of the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.staffquota;

import dev.vxrp.configuration.data.Config;
import dev.vxrp.database.tables.database.NoticeOfDepartureTable;
import dev.vxrp.database.tables.database.StaffQuotaTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Set;

final class LeaveDays {
    private static final Logger logger = LoggerFactory.getLogger(LeaveDays.class);

    private final NoticeOfDepartureTable notices = new NoticeOfDepartureTable();
    private final StaffQuotaTable table = new StaffQuotaTable();
    private final DateTimeFormatter formatter;

    LeaveDays(Config config) {
        this.formatter = DateTimeFormatter.ofPattern(config.settings().noticeOfDeparture().dateFormatting());
    }

    void recordToday(LocalDate today) {
        for (String userId : notices.retrieveAllIds()) {
            if (notice(userId).filter(n -> n.covers(today)).isPresent()) table.recordLeaveDay(userId, today);
        }
        table.pruneLeaveDaysBefore(QuotaWeek.containing(today).previous().start());
    }

    long count(String userId, QuotaWeek week) {
        Set<LocalDate> days = table.leaveDays(userId, week.start(), week.end());
        notice(userId).ifPresent(n -> week.start().datesUntil(week.end().plusDays(1)).filter(n::covers).forEach(days::add));
        return days.size();
    }

    private Optional<Notice> notice(String userId) {
        String begin = notices.retrieveBeginDate(userId);
        String end = notices.retrieveEndDate(userId);
        if (begin == null || end == null) return Optional.empty();
        try {
            return Optional.of(new Notice(LocalDate.parse(begin, formatter), LocalDate.parse(end, formatter)));
        } catch (DateTimeParseException e) {
            logger.warn("Could not parse notice of departure dates for {}: {} to {}", userId, begin, end);
            return Optional.empty();
        }
    }

    private record Notice(LocalDate begin, LocalDate end) {
        boolean covers(LocalDate day) {
            return !day.isBefore(begin) && day.isBefore(end);
        }
    }
}
