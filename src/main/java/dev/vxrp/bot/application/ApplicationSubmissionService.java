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

package dev.vxrp.bot.application;

import dev.vxrp.bot.application.data.ApplicationAnswer;
import dev.vxrp.bot.application.data.ApplicationRecord;
import dev.vxrp.bot.application.enums.ApplicationLifecycle;
import dev.vxrp.bot.application.handler.ApplicationMessageHandler;
import dev.vxrp.bot.application.review.ApplicationReviewInteractions;
import dev.vxrp.bot.application.view.ApplicationView;
import dev.vxrp.bot.modals.TicketTemplateModals;
import dev.vxrp.bot.ticket.data.ApplicationQuestion;
import dev.vxrp.bot.ticket.data.TicketTypes;
import dev.vxrp.bot.ticket.enums.TicketStatus;
import dev.vxrp.bot.ticket.enums.TicketType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.DatabaseManager;
import dev.vxrp.database.tables.database.ApplicationTable;
import dev.vxrp.database.tables.database.ApplicationTypeTable;
import dev.vxrp.database.tables.database.ApplicationVoteTable;
import dev.vxrp.database.tables.database.TicketTable;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Creates application tickets and their durable canonical review records. */
public final class ApplicationSubmissionService {
    private static final ApplicationDrafts DRAFTS = new ApplicationDrafts();

    private final Logger logger = LoggerFactory.getLogger(ApplicationSubmissionService.class);
    private final Config config;
    private final Translation translation;

    public ApplicationSubmissionService(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void submit(ModalInteractionEvent event, String roleId) {
        List<ApplicationQuestion> questions = config.ticket().questionsForRole(roleId);
        if (questions == null) {
            fail(event, "Application configuration is incomplete. Please contact a server administrator.");
            return;
        }
        int firstPageSize = Math.min(questions.size(), Modal.MAX_COMPONENTS);
        List<ApplicationAnswer> firstAnswers = answers(questions.subList(0, firstPageSize), event.getValues());
        if (firstAnswers == null) {
            fail(event, "Your application form is no longer valid. Please reopen it and try again.");
            return;
        }
        String error = numericError(questions.subList(0, firstPageSize), firstAnswers);
        if (error != null) {
            fail(event, error);
            return;
        }
        if (questions.size() <= Modal.MAX_COMPONENTS) {
            submit(event, roleId, firstAnswers);
            return;
        }
        var type = new ApplicationTypeTable().query(roleId);
        if (event.getGuild() == null || type == null || !type.active) {
            fail(event, "This application position is no longer available.");
            return;
        }
        String token = DRAFTS.save(event.getUser().getId(), event.getGuild().getId(), roleId, questions, firstAnswers);
        event.getHook().sendMessage("First part saved. Continue within 15 minutes to complete your application.")
                .addComponents(ActionRow.of(Button.primary("application_continue:" + token, "Continue application")))
                .setEphemeral(true).queue();
    }

    public void continueApplication(ButtonInteractionEvent event, String token) {
        String guildId = event.getGuild() == null ? "" : event.getGuild().getId();
        var draft = DRAFTS.find(token, event.getUser().getId(), guildId);
        if (draft == null) {
            event.reply("This application has expired or has already been submitted. Please start again.")
                    .setEphemeral(true).queue();
            return;
        }
        event.replyModal(new TicketTemplateModals(translation)
                .applicationContinuationModal(token, draft.questions())).queue();
    }

    public void finishDraft(ModalInteractionEvent event, String token) {
        String guildId = event.getGuild() == null ? "" : event.getGuild().getId();
        var draft = DRAFTS.find(token, event.getUser().getId(), guildId);
        if (draft == null) {
            fail(event, "This application has expired or has already been submitted. Please start again.");
            return;
        }
        List<ApplicationQuestion> remaining = draft.questions().subList(Modal.MAX_COMPONENTS, draft.questions().size());
        List<ApplicationAnswer> finalAnswers = answers(remaining, event.getValues(), Modal.MAX_COMPONENTS);
        if (finalAnswers == null) {
            fail(event, "Your application form is no longer valid. Please try the Continue button again.");
            return;
        }
        String error = numericError(remaining, finalAnswers);
        if (error != null) {
            fail(event, error);
            return;
        }
        if (!DRAFTS.consume(token, draft)) {
            fail(event, "This application has expired or has already been submitted. Please start again.");
            return;
        }
        var answers = new ArrayList<>(draft.answers());
        answers.addAll(finalAnswers);
        submit(event, draft.roleId(), answers);
    }

    private void submit(ModalInteractionEvent event, String roleId, List<ApplicationAnswer> answers) {
        var applicationType = new ApplicationTypeTable().query(roleId);
        TicketTypes settings = applicationTicketSettings();
        TextChannel parent = settings == null ? null : event.getJDA().getTextChannelById(settings.parentChannel());
        TextChannel reviewChannel = event.getJDA().getTextChannelById(config.ticket().settings().ticketLogChannel());

        if (event.getGuild() == null || applicationType == null || !applicationType.active || settings == null
                || parent == null || reviewChannel == null) {
            fail(event, "Application configuration is incomplete. Please contact a server administrator.");
            return;
        }

        if (applicationType.members != null
                && new ApplicationTable().retrieveApprovedCount(roleId) >= applicationType.members) {
            fail(event, translation.support().embedNoMoreApplicationsBody()
                    .replace("%members%", String.valueOf(applicationType.members)));
            return;
        }
        try {
            create(event, roleId, roleName(roleId), settings, parent, reviewChannel, answers);
        } catch (RuntimeException exception) {
            logger.error("Could not start application submission for role {}", roleId, exception);
            fail(event, "Your application could not be created. Please try again later.");
        }
    }

    private void create(ModalInteractionEvent event, String roleId, String roleName, TicketTypes settings,
                        TextChannel parent, TextChannel reviewChannel, List<ApplicationAnswer> answers) {
        String threadName = settings.childRules().parentName()
                .replace("%r%", String.valueOf(new TicketTable().retrieveSerial()));
        parent.createThreadChannel(threadName, true).queue(thread -> provision(event, roleId, roleName, settings,
                        reviewChannel, answers, thread),
                exception -> {
                    logger.error("Could not create application thread for role {}", roleId, exception);
                    fail(event, "Your application could not be created. Please try again later.");
                });
    }

    private void provision(ModalInteractionEvent event, String roleId, String roleName, TicketTypes settings,
                           TextChannel reviewChannel, List<ApplicationAnswer> answers, ThreadChannel thread) {
        String applicationId = thread.getId();
        try {
            new ApplicationTable().createProvisioning(applicationId, roleId, event.getUser().getId(), null,
                    config.ticket().settings().applicationVoting().quorum(), answers);
        } catch (RuntimeException exception) {
            logger.error("Could not provision application {}", applicationId, exception);
            cleanup(applicationId, thread, null);
            fail(event, "Your application could not be created. Please try again later.");
            return;
        }

        inviteParticipants(event, roleId, roleName, settings, reviewChannel, answers, thread);
    }

    private void inviteParticipants(ModalInteractionEvent event, String roleId, String roleName, TicketTypes settings,
                                    TextChannel reviewChannel, List<ApplicationAnswer> answers, ThreadChannel thread) {
        Set<String> roleIds = Set.copyOf(settings.roles());
        if (roleIds.contains(event.getGuild().getPublicRole().getId())) {
            submissionFailed(event, roleId, thread.getId(), thread, null,
                    new IllegalArgumentException("The @everyone role cannot be invited to a private application thread"));
            return;
        }
        if (roleIds.isEmpty()) {
            addParticipants(event, roleId, roleName, settings, reviewChannel, answers, thread, List.of());
            return;
        }
        event.getGuild().findMembers(member -> member.getRoles().stream()
                        .anyMatch(role -> roleIds.contains(role.getId())))
                .onSuccess(members -> addParticipants(event, roleId, roleName, settings, reviewChannel, answers,
                        thread, members))
                .onError(exception -> submissionFailed(event, roleId, thread.getId(), thread, null, exception));
    }

    private void addParticipants(ModalInteractionEvent event, String roleId, String roleName, TicketTypes settings,
                                 TextChannel reviewChannel, List<ApplicationAnswer> answers, ThreadChannel thread,
                                 List<Member> staff) {
        var ids = new LinkedHashSet<String>();
        ids.add(event.getUser().getId());
        for (Member member : staff) {
            ids.add(member.getId());
        }
        try {
            RestAction.allOf(ids.stream().map(thread::addThreadMemberById).toList())
                    .queue(ignored -> prepareThread(event, roleId, roleName, settings, reviewChannel, answers, thread),
                            exception -> submissionFailed(event, roleId, thread.getId(), thread, null, exception));
        } catch (RuntimeException exception) {
            submissionFailed(event, roleId, thread.getId(), thread, null, exception);
        }
    }

    private void prepareThread(ModalInteractionEvent event, String roleId, String roleName, TicketTypes settings,
                               TextChannel reviewChannel, List<ApplicationAnswer> answers, ThreadChannel thread) {
        var preparation = settings.childRules().lockOnDefault()
                ? thread.getManager().setLocked(true)
                : thread.getManager().setLocked(false);
        preparation.queue(ignored -> sendThreadRoot(event, roleId, roleName, settings, reviewChannel, answers, thread),
                exception -> submissionFailed(event, roleId, thread.getId(), thread, null, exception));
    }

    private void sendThreadRoot(ModalInteractionEvent event, String roleId, String roleName, TicketTypes settings,
                                TextChannel reviewChannel, List<ApplicationAnswer> answers, ThreadChannel thread) {
        notifyParticipants(event, settings, thread);
        var messages = new ApplicationMessageHandler(config, translation);
        thread.sendMessage(messages.applicationThreadRoot(
                translation.support().embedTicketApplicationTitle().replace("%name%", thread.getName()),
                event.getUser().getId(), roleName, answers)).queue(rootMessage -> {
            var application = new ApplicationTable().retrieveById(thread.getId()).orElseThrow();
            var openApplication = new ApplicationRecord(application.id(), application.roleId(), application.issuer(),
                    application.handler(), ApplicationLifecycle.OPEN, application.decision(), application.createdAt(),
                    application.closedAt(), application.closedBy(), application.closeReason(), reviewChannel.getId(),
                    null, null, application.quorumRequired());
            reviewChannel.sendMessage(messages.openReview(openApplication, roleName, answers,
                    new ApplicationVoteTable().tally(application.id(), application.quorumRequired()),
                    ApplicationView.ticketUrl(config.settings().guildId(), application.id()), null))
                    .queue(reviewMessage -> finish(event, roleId, thread, rootMessage, reviewMessage),
                            exception -> submissionFailed(event, roleId, thread.getId(), thread, null, exception));
        }, exception -> submissionFailed(event, roleId, thread.getId(), thread, null, exception));
    }

    private void finish(ModalInteractionEvent event, String roleId, ThreadChannel thread, Message rootMessage,
                        Message reviewMessage) {
        try {
            DatabaseManager.transaction(connection -> {
                new TicketTable().addToDatabase(connection, thread.getId(), TicketType.APPLICATION, TicketStatus.OPEN,
                        LocalDate.now().toString(), event.getUser().getId(), null, reviewMessage.getId(),
                        rootMessage.getId(), "CURRENTLY NOT IMPLEMENTED");
                new ApplicationTable().markOpen(connection, thread.getId(), reviewMessage.getChannelId(),
                        reviewMessage.getId());
                return null;
            });
        } catch (RuntimeException exception) {
            submissionFailed(event, roleId, thread.getId(), thread, reviewMessage, exception);
            return;
        }

        new ApplicationMessageHandler(config, translation).refreshPublicPanel(event.getJDA());
        event.getHook().sendMessage(ApplicationView.feedback("Application submitted",
                "Your application was created in " + thread.getAsMention() + ".", true))
                .setEphemeral(true).queue();
        logger.info("Created application {} for role {} by user {}", thread.getId(), roleId, event.getUser().getId());
        try {
            reviewMessage.createThreadChannel("Application " + thread.getId() + " staff discussion")
                    .queue(discussion -> {
                        try {
                            if (!new ApplicationTable().setReviewThreadId(thread.getId(), reviewMessage.getId(), discussion.getId())) {
                                logger.warn("Could not associate discussion thread with application {}", thread.getId());
                                return;
                            }
                            new ApplicationReviewInteractions(config, translation).refreshCanonical(event.getJDA(), thread.getId());
                        } catch (RuntimeException exception) {
                            logger.error("Could not save discussion thread for application {}", thread.getId(), exception);
                        }
                    }, exception -> {
                        logger.error("Could not create discussion thread for application {}", thread.getId(), exception);
                        new ApplicationReviewInteractions(config, translation).refreshCanonical(event.getJDA(), thread.getId());
                    });
        } catch (RuntimeException exception) {
            logger.error("Could not start discussion thread for application {}", thread.getId(), exception);
            new ApplicationReviewInteractions(config, translation).refreshCanonical(event.getJDA(), thread.getId());
        }
    }

    private void notifyParticipants(ModalInteractionEvent event, TicketTypes settings, ThreadChannel thread) {
        thread.sendMessage(event.getUser().getAsMention()).queue(message -> message.delete().queue());
        for (String staffRoleId : settings.roles()) {
            var role = event.getJDA().getRoleById(staffRoleId);
            if (role != null) {
                thread.sendMessage(role.getAsMention()).queue(message -> message.delete().queue());
            }
        }
    }

    private void submissionFailed(ModalInteractionEvent event, String roleId, String applicationId,
                                  ThreadChannel thread, Message reviewMessage, Throwable exception) {
        logger.error("Could not create application {} for role {}", applicationId, roleId, exception);
        cleanup(applicationId, thread, reviewMessage);
        fail(event, "Your application could not be created. Please try again later.");
    }

    private void cleanup(String applicationId, ThreadChannel thread, Message reviewMessage) {
        if (applicationId != null) {
            try {
                new ApplicationTable().markFailed(applicationId);
            } catch (RuntimeException exception) {
                logger.error("Could not mark application {} as failed", applicationId, exception);
            }
        }
        if (reviewMessage != null) {
            reviewMessage.delete().queue(null, ignored -> {});
        }
        if (thread != null) {
            thread.getManager().setLocked(true).setArchived(true).queue(null, ignored -> {});
        }
    }

    private void fail(ModalInteractionEvent event, String body) {
        event.getHook().sendMessage(ApplicationView.feedback("Unable to submit application", body, false))
                .setEphemeral(true).queue();
    }

    private TicketTypes applicationTicketSettings() {
        return config.ticket().types().stream()
                .filter(type -> "support::APPLICATION".equals(type.type()))
                .findFirst()
                .orElse(null);
    }

    private String roleName(String roleId) {
        return config.ticket().applicationTypes().stream()
                .filter(type -> type.roleID().equals(roleId))
                .map(type -> type.name())
                .findFirst()
                .orElse(roleId);
    }

    static List<ApplicationAnswer> answers(List<ApplicationQuestion> questions, List<ModalMapping> mappings) {
        return answers(questions, mappings, 0);
    }

    static List<ApplicationAnswer> answers(List<ApplicationQuestion> questions, List<ModalMapping> mappings, int offset) {
        if (mappings.size() != questions.size()) {
            return null;
        }
        var seen = new HashSet<String>();
        for (ModalMapping mapping : mappings) {
            if (!seen.add(mapping.getCustomId())) {
                return null;
            }
        }
        var answers = new ArrayList<ApplicationAnswer>();
        for (int index = 0; index < questions.size(); index++) {
            String id = "application_question_" + (offset + index);
            var mapping = mappings.stream().filter(value -> id.equals(value.getCustomId())).findFirst().orElse(null);
            ApplicationQuestion question = questions.get(index);
            if (mapping == null || mapping.getAsString() == null
                    || mapping.getAsString().length() < question.minLength()
                    || mapping.getAsString().length() > question.maxLength()) {
                return null;
            }
            answers.add(new ApplicationAnswer(question.label(), mapping.getAsString()));
        }
        return answers;
    }

    private static String numericError(List<ApplicationQuestion> questions, List<ApplicationAnswer> answers) {
        for (int index = 0; index < questions.size(); index++) {
            if (questions.get(index).numeric() && !isNumeric(answers.get(index).value())) {
                return "Please enter a number for " + questions.get(index).label() + ".";
            }
        }
        return null;
    }

    private static boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
