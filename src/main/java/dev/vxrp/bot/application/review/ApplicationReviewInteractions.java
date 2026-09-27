/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain a copy of the license at
 *
 * https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.application.review;

import dev.vxrp.bot.application.data.ApplicationRecord;
import dev.vxrp.bot.application.enums.ApplicationDecision;
import dev.vxrp.bot.application.enums.ApplicationLifecycle;
import dev.vxrp.bot.application.enums.ApplicationQueueFilter;
import dev.vxrp.bot.application.enums.ApplicationVoteChoice;
import dev.vxrp.bot.application.handler.ApplicationMessageHandler;
import dev.vxrp.bot.application.view.ApplicationReviewView;
import dev.vxrp.bot.application.view.ApplicationView;
import dev.vxrp.bot.commands.handler.bot.application.ApplicationCommand;
import dev.vxrp.bot.modals.ApplicationTemplateModals;
import dev.vxrp.bot.ticket.enums.TicketStatus;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.configuration.data.TranslationApplicationReview;
import dev.vxrp.database.DatabaseManager;
import dev.vxrp.database.tables.database.ApplicationAnswerTable;
import dev.vxrp.database.tables.database.ApplicationTable;
import dev.vxrp.database.tables.database.ApplicationVoteTable;
import dev.vxrp.database.tables.database.TicketTable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Routes staff application review interactions and revalidates authorization on every action. */
public final class ApplicationReviewInteractions {
    private static final int HISTORY_PAGE_SIZE = 8;

    private final Config config;
    private final Translation translation;
    private final ApplicationTable applications = new ApplicationTable();
    private final ApplicationAnswerTable answers = new ApplicationAnswerTable();
    private final ApplicationVoteTable votes = new ApplicationVoteTable();

    public ApplicationReviewInteractions(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public boolean handleButton(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (id.startsWith("applications_page:") || id.startsWith("applications_refresh:")) {
            updateQueue(event, id);
            return true;
        }
        if (id.startsWith("application_review_history:")) {
            showHistory(event, id);
            return true;
        }
        if (id.startsWith("application_review_close_prompt:")) {
            showClosePrompt(event, id.substring("application_review_close_prompt:".length()));
            return true;
        }
        if (id.startsWith("application_review_close_decision:")) {
            showReasonModal(event, id);
            return true;
        }
        if (id.startsWith("application_review_close_cancel:")) {
            event.editMessage(MessageEditBuilder.fromCreateData(ApplicationReviewView.feedback("Review remains open",
                    "No decision was recorded.", true)).build()).queue();
            return true;
        }
        if (id.startsWith("application_review:")) {
            showReview(event, id.substring("application_review:".length()));
            return true;
        }
        return false;
    }

    public boolean handleSelect(StringSelectInteractionEvent event) {
        String id = event.getComponentId();
        if (id.startsWith("applications_filter:")) {
            updateQueue(event, id);
            return true;
        }
        if (id.startsWith("application_review_vote:")) {
            updateVote(event, id.substring("application_review_vote:".length()));
            return true;
        }
        return false;
    }

    public boolean handleModal(ModalInteractionEvent event) {
        if (!event.getModalId().startsWith("application_review_close_reason:")) {
            return false;
        }

        String[] parts = event.getModalId().split(":", 3);
        if (parts.length != 3 || !isSenior(event.getMember())) {
            event.reply(feedback(false, translation.applicationReview().textClosePermission())).setEphemeral(true).queue();
            return true;
        }

        ApplicationDecision decision;
        try {
            decision = ApplicationDecision.valueOf(parts[2]);
        } catch (IllegalArgumentException exception) {
            event.reply(feedback(false, translation.applicationReview().textCloseError())).setEphemeral(true).queue();
            return true;
        }

        String applicationId = parts[1];
        String reason = event.getValue("application_review_close_reason") == null ? ""
                : event.getValue("application_review_close_reason").getAsString().strip();
        ApplicationRecord application = applications.retrieveById(applicationId).orElse(null);
        if (application == null || application.lifecycle() != ApplicationLifecycle.OPEN) {
            event.reply(feedback(false, translation.applicationReview().textCloseAlreadyClosed())).setEphemeral(true).queue();
            return true;
        }
        if (!votes.tally(applicationId, application.quorumRequired()).quorumReached()) {
            event.reply(feedback(false, translation.applicationReview().textCloseQuorum()
                    .replace("%quorum%", String.valueOf(application.quorumRequired())))).setEphemeral(true).queue();
            return true;
        }

        boolean closed = DatabaseManager.transaction(connection -> {
            boolean updated = applications.closeIfOpen(connection, applicationId, decision,
                    event.getUser().getId(), reason, java.time.Instant.now());
            if (updated) {
                new TicketTable().updateTicketStatus(connection, applicationId, TicketStatus.CLOSED);
            }
            return updated;
        });
        if (!closed) {
            event.reply(feedback(false, translation.applicationReview().textCloseAlreadyClosed())).setEphemeral(true).queue();
            return true;
        }

        ApplicationRecord locked = applications.retrieveById(applicationId).orElseThrow();
        refreshCanonical(event.getJDA(), locked);
        new ApplicationMessageHandler(config, translation).refreshPublicPanel(event.getJDA());
        var thread = event.getJDA().getThreadChannelById(applicationId);
        if (thread != null) {
            thread.getManager().setLocked(true).setArchived(true).queue();
        }
        notifyApplicant(event, locked);
        event.reply(feedback(true, closeSuccess(translation.applicationReview(), locked.decision())))
                .setEphemeral(true).queue();
        return true;
    }

    public boolean canReview(Member member) {
        if (member == null) {
            return false;
        }
        Set<String> allowed = new HashSet<>(config.ticket().settings().applicationVoting().seniorCloserRoles());
        config.ticket().types().stream()
                .filter(type -> "support::APPLICATION".equals(type.type()))
                .findFirst()
                .ifPresent(type -> allowed.addAll(type.logPermissionRoles()));
        return member.getRoles().stream().anyMatch(role -> allowed.contains(role.getId()));
    }

    private boolean isSenior(Member member) {
        if (member == null || config.ticket().settings().applicationVoting().seniorCloserRoles().isEmpty()) {
            return false;
        }
        return member.getRoles().stream().anyMatch(role ->
                config.ticket().settings().applicationVoting().seniorCloserRoles().contains(role.getId()));
    }

    private void showReview(ButtonInteractionEvent event, String applicationId) {
        if (!canReview(event.getMember())) {
            event.reply(feedback(false, translation.applicationReview().textVoteNotAllowed())).setEphemeral(true).queue();
            return;
        }
        ApplicationRecord application = applications.retrieveById(applicationId).orElse(null);
        if (application == null) {
            event.reply(feedback(false, translation.applicationReview().textQueueError())).setEphemeral(true).queue();
            return;
        }
        var handler = new ApplicationMessageHandler(config, translation);
        String ticketUrl = ApplicationView.ticketUrl(config.settings().guildId(), application.id());
        if (application.lifecycle() == ApplicationLifecycle.CLOSED) {
            event.reply(handler.lockedReview(application, roleName(application.roleId()), answers.list(applicationId),
                    votes.tally(applicationId, application.quorumRequired()), votes.history(applicationId), ticketUrl))
                    .setEphemeral(true).queue();
            return;
        }
        event.reply(handler.personalReview(application, roleName(application.roleId()), answers.list(applicationId),
                votes.tally(applicationId, application.quorumRequired()), votes.getCurrent(applicationId,
                        event.getUser().getId()), ticketUrl)).setEphemeral(true).queue();
    }

    private void updateVote(StringSelectInteractionEvent event, String applicationId) {
        if (!canReview(event.getMember())) {
            event.reply(feedback(false, translation.applicationReview().textVoteNotAllowed())).setEphemeral(true).queue();
            return;
        }
        ApplicationRecord application = applications.retrieveById(applicationId).orElse(null);
        if (application == null || application.lifecycle() != ApplicationLifecycle.OPEN) {
            event.reply(feedback(false, translation.applicationReview().textVoteClosed())).setEphemeral(true).queue();
            return;
        }

        String value = event.getValues().getFirst();
        String confirmation;
        if ("REMOVE".equals(value)) {
            votes.remove(applicationId, event.getUser().getId());
            confirmation = translation.applicationReview().textVoteRemoved();
        } else {
            ApplicationVoteChoice choice;
            try {
                choice = ApplicationVoteChoice.valueOf(value);
            } catch (IllegalArgumentException exception) {
                event.reply(feedback(false, translation.applicationReview().textQueueError())).setEphemeral(true).queue();
                return;
            }
            var previous = votes.getCurrent(applicationId, event.getUser().getId());
            votes.set(applicationId, event.getUser().getId(), choice);
            confirmation = voteFeedback(translation.applicationReview(), choice,
                    previous != null && previous.choice() != choice);
        }

        refreshCanonical(event.getJDA(), application);
        if (event.getMessage().isEphemeral()) {
            var payload = new ApplicationMessageHandler(config, translation).personalReview(application,
                    roleName(application.roleId()), answers.list(applicationId),
                    votes.tally(applicationId, application.quorumRequired()),
                    votes.getCurrent(applicationId, event.getUser().getId()),
                    ApplicationView.ticketUrl(config.settings().guildId(), application.id()));
            event.editMessage(MessageEditBuilder.fromCreateData(payload).build()).queue();
        } else {
            event.reply(feedback(true, confirmation)).setEphemeral(true).queue();
        }
    }

    static String voteFeedback(TranslationApplicationReview review, ApplicationVoteChoice choice, boolean changed) {
        String label = switch (choice) {
            case APPROVE -> review.textVoteApprove();
            case DENY -> review.textVoteDeny();
            case ABSTAIN -> review.textVoteAbstain();
        };
        return (changed ? review.textVoteChanged() : review.textVoteRecorded()).replace("%vote%", label);
    }

    static String closeSuccess(TranslationApplicationReview review, ApplicationDecision decision) {
        String label = decision == ApplicationDecision.APPROVED
                ? review.textDecisionApproved() : review.textDecisionDenied();
        if (label == null || label.isBlank()) {
            label = decision.name().toLowerCase(Locale.ROOT);
        }
        return review.textCloseSuccess().replace("%decision%", label);
    }

    private void showHistory(ButtonInteractionEvent event, String id) {
        if (!canReview(event.getMember())) {
            event.reply(feedback(false, translation.applicationReview().textVoteNotAllowed())).setEphemeral(true).queue();
            return;
        }
        String[] parts = id.split(":", 3);
        if (parts.length != 3) {
            event.reply(feedback(false, translation.applicationReview().textQueueError())).setEphemeral(true).queue();
            return;
        }
        int page;
        try {
            page = Integer.parseInt(parts[2]);
        } catch (NumberFormatException exception) {
            page = 0;
        }
        event.reply(new ApplicationMessageHandler(config, translation).voteHistory(parts[1], page,
                HISTORY_PAGE_SIZE, votes.history(parts[1]))).setEphemeral(true).queue();
    }

    private void showClosePrompt(ButtonInteractionEvent event, String applicationId) {
        if (!isSenior(event.getMember())) {
            event.reply(feedback(false, translation.applicationReview().textClosePermission())).setEphemeral(true).queue();
            return;
        }
        ApplicationRecord application = applications.retrieveById(applicationId).orElse(null);
        if (application == null || application.lifecycle() != ApplicationLifecycle.OPEN) {
            event.reply(feedback(false, translation.applicationReview().textCloseAlreadyClosed())).setEphemeral(true).queue();
            return;
        }
        var tally = votes.tally(applicationId, application.quorumRequired());
        if (!tally.quorumReached()) {
            event.reply(feedback(false, translation.applicationReview().textCloseQuorum()
                    .replace("%quorum%", String.valueOf(application.quorumRequired())))).setEphemeral(true).queue();
            return;
        }
        event.reply(new ApplicationMessageHandler(config, translation).closeConfirmation(application, tally, null))
                .setEphemeral(true).queue();
    }

    private void showReasonModal(ButtonInteractionEvent event, String id) {
        if (!isSenior(event.getMember())) {
            event.reply(feedback(false, translation.applicationReview().textClosePermission())).setEphemeral(true).queue();
            return;
        }
        String[] parts = id.split(":", 3);
        if (parts.length != 3) {
            event.reply(feedback(false, translation.applicationReview().textCloseError())).setEphemeral(true).queue();
            return;
        }
        event.replyModal(new ApplicationTemplateModals(translation).closeReasonModal(parts[1], parts[2])).queue();
    }

    private void updateQueue(ButtonInteractionEvent event, String id) {
        QueueState state = queueState(id, event.getUser().getId(), null);
        if (state == null || !canReview(event.getMember())) {
            event.reply(feedback(false, translation.applicationReview().textVoteNotAllowed())).setEphemeral(true).queue();
            return;
        }
        var command = new ApplicationCommand(config, translation);
        event.editMessage(MessageEditBuilder.fromCreateData(command.queuePayload(state.requesterId(), state.filter(),
                state.page(), command.queueEntries(state.filter(), state.page()))).build()).queue();
    }

    private void updateQueue(StringSelectInteractionEvent event, String id) {
        ApplicationQueueFilter selected;
        try {
            selected = ApplicationQueueFilter.valueOf(event.getValues().getFirst());
        } catch (IllegalArgumentException exception) {
            selected = ApplicationQueueFilter.OPEN;
        }
        QueueState state = queueState(id, event.getUser().getId(), selected);
        if (state == null || !canReview(event.getMember())) {
            event.reply(feedback(false, translation.applicationReview().textVoteNotAllowed())).setEphemeral(true).queue();
            return;
        }
        var command = new ApplicationCommand(config, translation);
        event.editMessage(MessageEditBuilder.fromCreateData(command.queuePayload(state.requesterId(), state.filter(),
                state.page(), command.queueEntries(state.filter(), state.page()))).build()).queue();
    }

    private QueueState queueState(String id, String actorId, ApplicationQueueFilter selected) {
        String[] parts = id.split(":", 4);
        if (parts.length != 4 || !actorId.equals(parts[1])) {
            return null;
        }
        try {
            ApplicationQueueFilter filter = selected == null ? ApplicationQueueFilter.valueOf(parts[2]) : selected;
            int page = selected == null ? Integer.parseInt(parts[3]) : 0;
            return new QueueState(parts[1], filter, Math.max(0, page));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private void refreshCanonical(net.dv8tion.jda.api.JDA api, ApplicationRecord application) {
        ApplicationRecord current = applications.retrieveById(application.id()).orElse(application);
        TextChannel channel = current.reviewChannelId() == null ? null : api.getTextChannelById(current.reviewChannelId());
        if (channel == null || current.reviewMessageId() == null) {
            return;
        }
        new ApplicationMessageHandler(config, translation).editReviewMessage(channel, current.reviewMessageId(), current,
                roleName(current.roleId()), answers.list(current.id()), votes.tally(current.id(), current.quorumRequired()),
                votes.history(current.id()), ApplicationView.ticketUrl(config.settings().guildId(), current.id()));
    }

    private void notifyApplicant(ModalInteractionEvent event, ApplicationRecord application) {
        String title = application.decision() == ApplicationDecision.APPROVED
                ? translation.applicationReview().textApplicantApprovedTitle()
                : translation.applicationReview().textApplicantDeniedTitle();
        String body = (application.decision() == ApplicationDecision.APPROVED
                ? translation.applicationReview().textApplicantApprovedBody()
                : translation.applicationReview().textApplicantDeniedBody())
                .replace("%position%", roleName(application.roleId()));
        var payload = ApplicationReviewView.applicantResult(title, body,
                ApplicationView.ticketUrl(config.settings().guildId(), application.id()), application.decision());
        event.getJDA().retrieveUserById(application.issuer())
                .flatMap(user -> user.openPrivateChannel())
                .flatMap(channel -> channel.sendMessage(payload))
                .queue(null, ignored -> {});
    }

    private String roleName(String roleId) {
        return config.ticket().applicationTypes().stream()
                .filter(type -> type.roleID().equals(roleId))
                .map(type -> type.name())
                .findFirst()
                .orElse(roleId);
    }

    private net.dv8tion.jda.api.utils.messages.MessageCreateData feedback(boolean success, String body) {
        return ApplicationReviewView.feedback(success ? "Done" : "Unable to continue", body, success);
    }

    private record QueueState(String requesterId, ApplicationQueueFilter filter, int page) {}
}
