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
import dev.vxrp.configuration.data.ConfigStaffQuota;
import dev.vxrp.database.tables.database.StaffQuotaTable;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;

public final class QuotaEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(QuotaEvaluator.class);
    private static final double SECONDS_PER_HOUR = 3600;

    private final Config config;
    private final LeaveDays leaveDays;
    private final CedmodPlaytime playtime;
    private final StaffQuotaTable table = new StaffQuotaTable();

    public QuotaEvaluator(Config config) {
        this.config = config;
        this.leaveDays = new LeaveDays(config);
        this.playtime = new CedmodPlaytime(config.settings().cedmod());
    }

    public OptionalDouble weeklyHours(Member member) {
        Set<String> roleIds = member.getRoles().stream().map(ISnowflake::getId).collect(Collectors.toSet());
        return config.settings().staffQuota().roles().stream()
                .filter(role -> roleIds.contains(role.roleId()))
                .mapToDouble(ConfigStaffQuota.Role::hours)
                .max();
    }

    void recordBaseline(String userId, QuotaWeek week) throws IOException, InterruptedException {
        if (table.baseline(userId, week.start()).isPresent()) return;
        var total = playtime.secondsPlayed(userId, CedmodPlaytime.LIFETIME_DAYS);
        if (total.isPresent()) table.recordBaseline(userId, week.start(), total.getAsLong());
    }

    public QuotaStanding evaluate(String userId, double weeklyHours, QuotaWeek week, LocalDate today) {
        long daysAway = leaveDays.count(userId, week);
        if (daysAway >= QuotaWeek.DAYS) return new QuotaStanding.OnLeave(userId);

        double required = weeklyHours * (QuotaWeek.DAYS - daysAway) / QuotaWeek.DAYS;
        try {
            var seconds = secondsInWeek(userId, week, today);
            if (seconds.isEmpty()) return new QuotaStanding.NotLinked(userId);
            return new QuotaStanding.Tracked(userId, seconds.getAsLong() / SECONDS_PER_HOUR, required);
        } catch (IOException e) {
            logger.error("CedMod playtime lookup failed for staff member {}", userId, e);
            return new QuotaStanding.LookupFailed(userId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new QuotaStanding.LookupFailed(userId);
        }
    }

    private OptionalLong secondsInWeek(String userId, QuotaWeek week, LocalDate today) throws IOException, InterruptedException {
        var start = table.baseline(userId, week.start());
        if (start.isEmpty()) return playtime.secondsPlayed(userId, week.daysElapsed(today));

        var end = week.contains(today) ? OptionalLong.empty() : table.baseline(userId, week.end().plusDays(1));
        if (end.isEmpty()) end = playtime.secondsPlayed(userId, CedmodPlaytime.LIFETIME_DAYS);
        if (end.isEmpty()) return end;
        return OptionalLong.of(Math.max(0, end.getAsLong() - start.getAsLong()));
    }
}
