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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

public record QuotaWeek(LocalDate start) {
    public static final int DAYS = 7;

    public static QuotaWeek containing(LocalDate day) {
        return new QuotaWeek(day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
    }

    public QuotaWeek previous() {
        return new QuotaWeek(start.minusWeeks(1));
    }

    public LocalDate end() {
        return start.plusDays(DAYS - 1);
    }

    public boolean contains(LocalDate day) {
        return !day.isBefore(start) && !day.isAfter(end());
    }

    public int daysElapsed(LocalDate today) {
        return (int) Math.clamp(ChronoUnit.DAYS.between(start, today) + 1, 1, DAYS);
    }

    public int daysLeft(LocalDate today) {
        return DAYS - daysElapsed(today);
    }
}
