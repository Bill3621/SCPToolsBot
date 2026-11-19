/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content is
 * prohibited. If you did not receive a copy of the license with this file, you may obtain a copy
 * of the license at
 *
 * https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software is
 * provided without any sort of WARRANTY, and the authors cannot be held liable for any form of
 * claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.application.review;

import dev.vxrp.bot.application.data.ApplicationRecord;
import dev.vxrp.bot.application.data.ApplicationTally;
import dev.vxrp.bot.application.enums.ApplicationLifecycle;
import dev.vxrp.bot.application.enums.ApplicationQueueFilter;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.database.tables.database.ApplicationTable;
import dev.vxrp.database.tables.database.ApplicationVoteTable;
import dev.vxrp.util.coroutines.ExecutorScopes;
import dev.vxrp.util.coroutines.Timer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

public final class ApplicationVoteNotifier {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationVoteNotifier.class);

    private final Config config;

    public ApplicationVoteNotifier(Config config) {
        this.config = config;
    }

    public void voteChanged(JDA api, ApplicationRecord application, ApplicationTally before, ApplicationTally after) {
        var closers = config.ticket().settings().applicationVoting().seniorCloserRoles();
        if (before.quorumReached() || !after.quorumReached() || closers.isEmpty()) return;
        post(api, application, closers, "reached quorum " + quorum(after) + " and is ready for a decision.");
    }

    public void spinUpChecker(JDA api) {
        if (config.ticket().settings().applicationVoting().reminderAfterDays() == 0) return;
        new Timer().runWithTimer(Duration.ofHours(1), ExecutorScopes.applicationScope, () -> {
            try {
                api.awaitReady();
                remindStale(api);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException e) {
                logger.error("Could not send application vote reminders", e);
            }
        });
    }

    private void remindStale(JDA api) {
        int days = config.ticket().settings().applicationVoting().reminderAfterDays();
        Instant cutoff = Instant.now().minus(Duration.ofDays(days));
        var applications = new ApplicationTable();
        var votes = new ApplicationVoteTable();
        for (ApplicationRecord application : applications.filter(ApplicationQueueFilter.OPEN)) {
            if (application.lifecycle() != ApplicationLifecycle.OPEN || application.createdAt() == null
                    || application.createdAt().isAfter(cutoff)) continue;
            var tally = votes.tally(application.id(), application.quorumRequired());
            if (tally.quorumReached() || !applications.markReminded(application.id())) continue;
            post(api, application, config.ticket().applicationVoterRoleIds(),
                    "is still below quorum " + quorum(tally) + " after " + days + " day(s). Please cast your vote.");
        }
    }

    private void post(JDA api, ApplicationRecord application, Collection<String> roleIds, String body) {
        String mentions = roleIds.stream().map(id -> "<@&" + id + ">").collect(Collectors.joining(" "));
        var message = new MessageCreateBuilder()
                .setContent((mentions + " Application `" + application.id() + "` for **" + roleName(application.roleId()) + "** " + body).strip())
                .setAllowedMentions(EnumSet.of(MentionType.ROLE))
                .build();

        var thread = application.reviewThreadId() == null ? null : api.getThreadChannelById(application.reviewThreadId());
        if (thread != null) {
            thread.sendMessage(message).queue(null, error -> logger.warn("Could not notify staff about application {}", application.id(), error));
            return;
        }
        var channel = application.reviewChannelId() == null ? null : api.getTextChannelById(application.reviewChannelId());
        if (channel == null) {
            logger.warn("Could not notify staff about application {} because its review channel is unavailable", application.id());
            return;
        }
        var action = channel.sendMessage(message);
        if (application.reviewMessageId() != null) action = action.setMessageReference(application.reviewMessageId());
        action.queue(null, error -> logger.warn("Could not notify staff about application {}", application.id(), error));
    }

    private String roleName(String roleId) {
        return config.ticket().applicationTypes().stream()
                .filter(type -> type.roleID().equals(roleId))
                .map(type -> type.name())
                .findFirst()
                .orElse(roleId);
    }

    private static String quorum(ApplicationTally tally) {
        return "(`" + tally.counted() + "/" + tally.quorumRequired() + "`)";
    }
}
