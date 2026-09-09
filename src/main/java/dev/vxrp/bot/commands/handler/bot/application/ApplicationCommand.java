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

package dev.vxrp.bot.commands.handler.bot.application;

import dev.vxrp.bot.application.enums.ApplicationQueueFilter;
import dev.vxrp.bot.application.review.ApplicationReviewInteractions;
import dev.vxrp.bot.application.handler.ApplicationMessageHandler;
import dev.vxrp.bot.application.view.ApplicationReviewView;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.ApplicationTable;
import dev.vxrp.database.tables.database.ApplicationVoteTable;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class ApplicationCommand {
    private final Config config;
    private final Translation translation;

    public ApplicationCommand(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void sendActivationMessage(SlashCommandInteractionEvent event) {
        var payload = new ApplicationMessageHandler(config, translation)
                .getActivationMenu(event.getUser().getId());
        event.reply(payload).setEphemeral(true).queue();
    }

    public void sendDeactivationMessage(SlashCommandInteractionEvent event) {
        var payload = new ApplicationMessageHandler(config, translation).getDeactivationMenu();
        event.reply(payload).setEphemeral(true).queue();
    }

    public void sendQueue(SlashCommandInteractionEvent event) {
        if (!new ApplicationReviewInteractions(config, translation).canReview(event.getMember())) {
            event.reply(ApplicationReviewView.feedback("Unable to continue",
                    translation.applicationReview().textVoteNotAllowed(), false)).setEphemeral(true).queue();
            return;
        }
        event.reply(queuePayload(event.getUser().getId(), ApplicationQueueFilter.OPEN, 0,
                queueEntries(ApplicationQueueFilter.OPEN, 0))).setEphemeral(true).queue();
    }

    public List<ApplicationReviewView.QueueEntry> queueEntries(ApplicationQueueFilter filter, int page) {
        ApplicationTable applications = new ApplicationTable();
        ApplicationVoteTable votes = new ApplicationVoteTable();
        return applications.list(filter, page, ApplicationReviewView.DEFAULT_PAGE_SIZE).stream()
                .map(application -> queueEntry(application, roleName(application.roleId()), application.issuer(),
                        votes.tally(application.id(), application.quorumRequired())))
                .toList();
    }

    private String roleName(String roleId) {
        return config.ticket().applicationTypes().stream()
                .filter(type -> type.roleID().equals(roleId))
                .map(type -> type.name())
                .findFirst()
                .orElse(roleId);
    }

    /**
     * Builds the staff queue payload for wiring to the application queue command.
     */
    public ApplicationReviewView.QueueEntry queueEntry(dev.vxrp.bot.application.data.ApplicationRecord application,
                                                       String roleName, String applicantName,
                                                       dev.vxrp.bot.application.data.ApplicationTally tally) {
        return new ApplicationReviewView.QueueEntry(application, roleName, applicantName, tally);
    }

    /**
     * Builds an ephemeral queue payload. Routing and authorization remain in the command listener.
     */
    public net.dv8tion.jda.api.utils.messages.MessageCreateData queuePayload(String requesterId,
                                                                              ApplicationQueueFilter filter,
                                                                              int page,
                                                                              List<ApplicationReviewView.QueueEntry> entries) {
        ApplicationTable table = new ApplicationTable();
        ApplicationQueueFilter effectiveFilter = filter == null ? ApplicationQueueFilter.OPEN : filter;
        return new ApplicationMessageHandler(config, translation).applicationQueue(requesterId, effectiveFilter,
                page, ApplicationReviewView.DEFAULT_PAGE_SIZE, entries, table.count(effectiveFilter));
    }
}
