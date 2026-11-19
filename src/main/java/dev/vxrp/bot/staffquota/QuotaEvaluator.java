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
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

public final class QuotaEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(QuotaEvaluator.class);

    private final Config config;
    private final LeaveDays leaveDays;
    private final CedmodPlaytime playtime;

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

    public QuotaStanding evaluate(String userId, double weeklyHours, QuotaWeek week, int windowDays) {
        long daysAway = leaveDays.count(userId, week);
        if (daysAway >= QuotaWeek.DAYS) return new QuotaStanding.OnLeave(userId);

        double required = weeklyHours * (QuotaWeek.DAYS - daysAway) / QuotaWeek.DAYS;
        try {
            var hours = playtime.hoursPlayed(userId, windowDays);
            if (hours.isEmpty()) return new QuotaStanding.NotLinked(userId);
            return new QuotaStanding.Tracked(userId, hours.getAsDouble(), required);
        } catch (IOException e) {
            logger.error("CedMod playtime lookup failed for staff member {}", userId, e);
            return new QuotaStanding.LookupFailed(userId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new QuotaStanding.LookupFailed(userId);
        }
    }
}
