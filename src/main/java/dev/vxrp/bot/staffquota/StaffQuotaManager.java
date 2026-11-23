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
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.StaffQuotaTable;
import dev.vxrp.util.coroutines.ExecutorScopes;
import dev.vxrp.util.coroutines.Timer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public final class StaffQuotaManager {
    private static final Logger logger = LoggerFactory.getLogger(StaffQuotaManager.class);

    private final JDA api;
    private final Config config;
    private final Translation translation;
    private final StaffQuotaTable table = new StaffQuotaTable();

    public StaffQuotaManager(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
    }

    public void spinUpChecker() {
        if (!config.settings().staffQuota().active()) return;
        if (!config.settings().cedmod().active()) {
            logger.error("Staff quota needs the cedmod integration, activate it to track staff playtime");
            return;
        }

        new Timer().runWithTimer(Duration.ofHours(1), ExecutorScopes.staffQuotaScope, this::tick);
    }

    private void tick() {
        try {
            api.awaitReady();
            runTick();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            logger.error("Staff quota tick failed, retrying next hour", e);
        }
    }

    private void runTick() throws InterruptedException {
        LocalDate today = LocalDate.now();
        if (config.settings().noticeOfDeparture().active()) new LeaveDays(config).recordToday(today);

        var guild = api.getGuildById(config.settings().guildId());
        if (guild == null) {
            logger.error("Could not track staff quota, check guild_id");
            return;
        }

        QuotaWeek current = QuotaWeek.containing(today);
        QuotaWeek week = current.previous();
        boolean needsBaselines = !table.hasBaselines(current.start());
        boolean needsReport = !table.isReported(week.start());
        if (!needsBaselines && !needsReport) return;

        var roster = new QuotaRoster(config);
        List<Member> staff = roster.staff(guild);
        if (needsBaselines) {
            roster.recordBaselines(staff, current);
            table.pruneBaselinesBefore(week.start());
        }
        if (needsReport) report(roster, staff, week, today);
    }

    private void report(QuotaRoster roster, List<Member> staff, QuotaWeek week, LocalDate today) {
        var channel = api.getTextChannelById(config.settings().staffQuota().reportChannel());
        if (channel == null) {
            logger.error("Could not post the staff quota report, check staff_quota.report_channel_id");
            return;
        }

        if (!today.equals(week.end().plusDays(1))) logger.warn("Staff quota report for week of {} is late, playtime after the week ended may be counted", week.start());

        List<QuotaRow> rows = roster.evaluate(staff, week, today);
        table.markReported(week.start());
        logger.info("Staff quota report for week of {} evaluated {} staff members", week.start(), rows.size());

        var messages = new QuotaMessages(translation, config.settings().noticeOfDeparture().dateFormatting());
        channel.sendMessage(messages.report(week, rows)).queue();

        if (!config.settings().staffQuota().dmMissed()) return;
        for (var row : rows) {
            if (!(row.standing() instanceof QuotaStanding.Tracked tracked) || tracked.met()) continue;
            api.openPrivateChannelById(tracked.userId())
                    .flatMap(dm -> dm.sendMessage(messages.missedDm(week, tracked)))
                    .queue(null, error -> logger.debug("Could not DM staff member {} about their missed quota", tracked.userId(), error));
        }
    }
}
