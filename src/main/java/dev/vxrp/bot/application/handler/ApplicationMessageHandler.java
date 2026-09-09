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

package dev.vxrp.bot.application.handler;

import dev.vxrp.bot.application.ApplicationManager;
import dev.vxrp.bot.application.data.ApplicationAnswer;
import dev.vxrp.bot.application.data.ApplicationRecord;
import dev.vxrp.bot.application.data.ApplicationTally;
import dev.vxrp.bot.application.data.ApplicationType;
import dev.vxrp.bot.application.data.ApplicationVote;
import dev.vxrp.bot.application.data.ApplicationVoteEvent;
import dev.vxrp.bot.application.enums.ApplicationDecision;
import dev.vxrp.bot.application.enums.ApplicationQueueFilter;
import dev.vxrp.bot.application.view.ApplicationReviewView;
import dev.vxrp.bot.application.view.ApplicationView;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.ApplicationTypeTable;
import dev.vxrp.database.tables.database.MessageTable;
import dev.vxrp.bot.application.enums.MessageType;
import dev.vxrp.util.GlobalVariables;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ApplicationMessageHandler {
    private final Config config;
    private final Translation translation;
    private final Logger logger = LoggerFactory.getLogger(ApplicationMessageHandler.class);

    public ApplicationMessageHandler(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public MessageCreateData getActivationMenu(String userId) {
        createRoleString(true);
        return activationPayload(userId);
    }

    public MessageCreateData getCurrentActivationMenu(String userId) {
        return activationPayload(userId);
    }

    public MessageCreateData getDeactivationMenu() {
        return ApplicationView.deactivationMenu(
                parse(translation.application().embedDeactivationMenuTitle()),
                parse(translation.application().embedDeactivationMenuBody()),
                translation);
    }

    public void editActivationMessage(String userId, String roleId, TextChannel channel,
                                      String messageId, String name, String description, String emoji, Boolean state,
                                      String initializer, Integer member) {
        new ApplicationManager(config, translation).changeApplicationType(roleId, name, description,
                emoji, state, initializer, member);

        MessageCreateData payload = activationPayload(userId);
        channel.editMessageById(messageId, MessageEditBuilder.fromCreateData(payload).build()).queue();
    }

    public void sendApplicationMessage(JDA api, TextChannel channel, boolean state) {
        MessageCreateData payload = publicPanelPayload(state);
        MessageTable messageTable = new MessageTable();
        MessageTable.MessageTableData applicationMessage = messageTable.queryFromTable(MessageType.APPLICATION);
        Message message = null;

        if (applicationMessage != null) {
            try {
                TextChannel messageChannel = api.getTextChannelById(applicationMessage.channelId);
                if (messageChannel != null) {
                    message = messageChannel.editMessageById(applicationMessage.id,
                            MessageEditBuilder.fromCreateData(payload).build()).complete();
                }
            } catch (ErrorResponseException e) {
                message = channel.sendMessage(payload).complete();
                messageTable.delete(applicationMessage.id);
            }
        } else {
            message = channel.sendMessage(payload).complete();
        }

        for (ApplicationType type : GlobalVariables.applicationTypeSet) {
            new ApplicationTypeTable().changeType(type.roleId(), type.state(), type.member(), type.initializer());
        }
        if (message == null) {
            logger.error("Could not mirror application settings to the message database");
            return;
        }
        messageTable.insertIfNotExists(message.getId(), MessageType.APPLICATION, message.getChannelId());
    }

    public List<ApplicationType> configuredApplicationTypes() {
        List<ApplicationType> types = new ArrayList<>();
        int position = 0;
        for (var type : config.ticket().applicationTypes()) {
            types.add(new ApplicationType(position++, type.roleID(), type.name(), type.description(), type.emoji(),
                    true, null, 0));
        }
        return types;
    }

    public MessageCreateData applicationSelectionPrompt() {
        return ApplicationView.selectionPrompt(
                parse(translation.support().embedApplicationPositionTitle()),
                parse(translation.support().embedApplicationPositionBody()),
                "application_position",
                "Choose an application position",
                configuredApplicationTypes());
    }

    public MessageCreateData activationSelectionPrompt(String menuId) {
        return ApplicationView.selectionPrompt(
                parse(translation.application().embedChoosePositionTitle()),
                parse(translation.application().embedChoosePositionBody()),
                menuId,
                "Choose a position to configure",
                GlobalVariables.applicationTypeSet);
    }

    public MessageCreateData positionNotActiveFeedback() {
        return ApplicationView.feedback(
                parse(translation.application().embedPositionNotActiveTitle()),
                parse(translation.application().embedPositionNotActiveBody()),
                false);
    }

    public MessageCreateData setupFeedback(boolean active) {
        return ApplicationView.feedback(
                parse(active ? translation.application().embedApplicationActivatedTitle()
                        : translation.application().embedApplicationDeactivatedTitle()),
                parse(active ? translation.application().embedApplicationActivatedBody()
                        : translation.application().embedApplicationDeactivatedBody()),
                active);
    }

    public MessageCreateData expiredFeedback() {
        return ApplicationView.feedback("Message expired",
                "This message has expired. Execute the command again to start over.", false);
    }

    public MessageCreateData applicationThreadRoot(String title, String applicant, String roleName,
                                                   List<ApplicationAnswer> answers) {
        return ApplicationView.applicationThreadRoot(title, applicant, roleName, answers);
    }

    public MessageCreateData openReview(ApplicationRecord application, String roleName, List<ApplicationAnswer> answers,
                                        ApplicationTally tally, ApplicationVote currentVote, String ticketUrl) {
        return ApplicationReviewView.openReview(application, roleName, answers, tally, currentVote, ticketUrl);
    }

    public MessageCreateData lockedReview(ApplicationRecord application, String roleName, List<ApplicationAnswer> answers,
                                           ApplicationTally tally, List<ApplicationVoteEvent> history, String ticketUrl) {
        return ApplicationReviewView.lockedReview(application, roleName, answers, tally, history, ticketUrl);
    }

    public void editReviewMessage(TextChannel channel, String messageId, ApplicationRecord application,
                                  String roleName, List<ApplicationAnswer> answers, ApplicationTally tally,
                                  ApplicationVote currentVote, List<ApplicationVoteEvent> history, String ticketUrl) {
        if (application.lifecycle() == dev.vxrp.bot.application.enums.ApplicationLifecycle.CLOSED) {
            channel.editMessageById(messageId, ApplicationReviewView.editLockedReview(application, roleName, answers,
                    tally, history, ticketUrl)).queue();
            return;
        }
        channel.editMessageById(messageId, ApplicationReviewView.editOpenReview(application, roleName, answers,
                tally, currentVote, ticketUrl)).queue();
    }

    public MessageCreateData applicationQueue(String requesterId, ApplicationQueueFilter filter, int page, int pageSize,
                                              List<ApplicationReviewView.QueueEntry> entries, long total) {
        return ApplicationReviewView.queue(requesterId, filter, page, pageSize, entries, total);
    }

    public MessageCreateData closeConfirmation(ApplicationRecord application, ApplicationTally tally,
                                               ApplicationDecision suggestedDecision) {
        return ApplicationReviewView.closeConfirmation(application, tally, suggestedDecision);
    }

    public MessageCreateData voteHistory(String applicationId, int page, int pageSize,
                                         List<ApplicationVoteEvent> events) {
        return ApplicationReviewView.history(applicationId, page, pageSize, events);
    }

    private MessageCreateData activationPayload(String userId) {
        return ApplicationView.activationMenu(
                parse(translation.application().embedActivationMenuTitle()),
                parse(translation.application().embedActivationMenuBody()),
                parse(translation.application().textStatusDeactivated()),
                GlobalVariables.applicationTypeSet,
                userId,
                translation);
    }

    private MessageCreateData publicPanelPayload(boolean active) {
        String status = active ? translation.application().textStatusActive()
                : translation.application().textStatusDeactivated();
        return ApplicationView.publicPanel(
                parse(translation.application().embedApplicationMessageTitle()),
                parse(translation.application().embedApplicationMessageBody()),
                parse(status),
                GlobalVariables.applicationTypeSet,
                active,
                translation);
    }

    private void createRoleString(boolean useBaseValue) {
        if (useBaseValue) {
            List<ApplicationType> baseTypes = createStringBaseValue();
            GlobalVariables.applicationTypeSet = new HashSet<>(baseTypes);
            List<ApplicationTypeTable.ApplicationType> entries = new ApplicationTypeTable().getAllEntries();
            for (ApplicationTypeTable.ApplicationType type : entries) {
                for (ApplicationType base : baseTypes) {
                    if (base.roleId().equals(type.roleId)) {
                        new ApplicationManager(config, translation).changeApplicationType(type.roleId,
                                base.name(), base.description(), base.emoji(), type.active, type.initializer,
                                type.members);
                    }
                }
            }
        }
    }

    private List<ApplicationType> createStringBaseValue() {
        List<ApplicationType> applicationTypeList = new ArrayList<>();
        for (var type : config.ticket().applicationTypes()) {
            applicationTypeList.add(new ApplicationType(applicationTypeList.size(), type.roleID(), type.name(),
                    type.description(), type.emoji(), false, null, 0));
        }
        return applicationTypeList;
    }

    private String parse(String value) {
        return new ColorTool().parse(value == null ? "" : value).strip();
    }
}
