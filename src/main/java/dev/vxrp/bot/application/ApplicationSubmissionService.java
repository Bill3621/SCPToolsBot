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
import dev.vxrp.bot.application.view.ApplicationView;
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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Creates application tickets and their durable canonical review records. */
public final class ApplicationSubmissionService {
    private static final Map<String, Object> ROLE_LOCKS = new ConcurrentHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(ApplicationSubmissionService.class);
    private final Config config;
    private final Translation translation;

    public ApplicationSubmissionService(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void submit(ModalInteractionEvent event, String roleId) {
        var applicationType = new ApplicationTypeTable().query(roleId);
        TicketTypes settings = applicationTicketSettings();
        TextChannel parent = settings == null ? null : event.getJDA().getTextChannelById(settings.parentChannel());
        TextChannel reviewChannel = event.getJDA().getTextChannelById(config.ticket().settings().ticketLogChannel());
        List<ApplicationAnswer> answers = answers(event.getValues());

        if (event.getGuild() == null || applicationType == null || !applicationType.active || settings == null
                || parent == null || reviewChannel == null || answers.size() != 5) {
            fail(event, "Application configuration is incomplete. Please contact a server administrator.");
            return;
        }
        if (!isNumeric(answer(answers, "application_age"))) {
            fail(event, translation.support().embedApplicationAgeNumericBody());
            return;
        }

        Object roleLock = ROLE_LOCKS.computeIfAbsent(roleId, ignored -> new Object());
        synchronized (roleLock) {
            try {
                if (applicationType.members != null
                        && new ApplicationTable().retrieveOpenCount(roleId) >= applicationType.members) {
                    fail(event, translation.support().embedNoMoreApplicationsBody()
                            .replace("%members%", String.valueOf(applicationType.members)));
                    return;
                }
                create(event, roleId, roleName(roleId), settings, parent, reviewChannel, answers);
            } finally {
                ROLE_LOCKS.remove(roleId, roleLock);
            }
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

        var preparation = settings.childRules().lockOnDefault()
                ? thread.getManager().setLocked(true)
                : thread.getManager().setLocked(false);
        preparation.queue(ignored -> sendThreadRoot(event, roleId, roleName, settings, reviewChannel, answers, thread),
                exception -> submissionFailed(event, roleId, applicationId, thread, null, exception));
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
                    null, application.quorumRequired());
            reviewChannel.sendMessage(messages.openReview(openApplication, roleName, answers,
                    new ApplicationVoteTable().tally(application.id(), application.quorumRequired()), null,
                    ApplicationView.ticketUrl(config.settings().guildId(), application.id())))
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

        event.getHook().sendMessage(ApplicationView.feedback("Application submitted",
                "Your application was created in " + thread.getAsMention() + ".", true))
                .setEphemeral(true).queue();
        logger.info("Created application {} for role {} by user {}", thread.getId(), roleId, event.getUser().getId());
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

    private static List<ApplicationAnswer> answers(List<ModalMapping> mappings) {
        Map<String, ModalMapping> byId = mappings.stream()
                .collect(Collectors.toMap(ModalMapping::getCustomId, Function.identity()));
        return List.of("application_name", "application_age", "application_playtime",
                        "application_reason_of_application", "application_skills").stream()
                .filter(byId::containsKey)
                .map(id -> new ApplicationAnswer(id, byId.get(id).getAsString()))
                .toList();
    }

    private static String answer(List<ApplicationAnswer> answers, String fieldKey) {
        return answers.stream().filter(answer -> fieldKey.equals(answer.fieldKey()))
                .map(ApplicationAnswer::value).findFirst().orElse("");
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
