/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content is
 * prohibited. If you did not receive a copy of the license with this file, you may obtain the
 * license at
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
import dev.vxrp.bot.application.data.ApplicationType;
import dev.vxrp.bot.application.enums.MessageType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.ApplicationTypeTable;
import dev.vxrp.database.tables.database.MessageTable;
import dev.vxrp.util.GlobalVariables;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ApplicationMessageHandler {
    private final Config config;
    private final Translation translation;
    private final Logger logger = LoggerFactory.getLogger(ApplicationManager.class);

    public ApplicationMessageHandler(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public Pair<MessageEmbed, ActionRow> getActivationMenu(String userId) {
        MessageEmbed embed = createMessage(true);
        return new Pair<>(embed, applicationActionRow(userId, null));
    }

    public Pair<MessageEmbed, ActionRow> getDeactivationMenu() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(0xE74D3C);
        builder.setTitle(
                new ColorTool().parse(translation.application().embedDeactivationMenuTitle()));
        builder.setDescription(
                new ColorTool().parse(translation.application().embedDeactivationMenuBody()));

        return new Pair<>(builder.build(),
                ActionRow.of(Button
                        .danger("application_deactivate",
                                translation.buttons().textApplicationDeactivate())
                        .withEmoji(Emoji.fromFormatted("\uD83E\uDEAB"))));
    }

    public void editActivationMessage(String userId, String roleId, TextChannel channel,
            String messageId, String name, String description, String emoji, Boolean state,
            String initializer, Integer member) {
        new ApplicationManager(config, translation).changeApplicationType(roleId, name, description,
                emoji, state, initializer, member);

        channel.editMessageById(messageId, "").setEmbeds(createMessage(false)).setComponents(
                applicationActionRow(userId, List.copyOf(GlobalVariables.applicationTypeSet)))
                .queue();
    }

    public void sendApplicationMessage(JDA api, TextChannel channel, boolean state) {
        StringBuilder roleStringPair = createRoleString(false);

        String status = state ? translation.application().textStatusActive()
                : translation.application().textStatusDeactivated();
        int embedColor = state ? 0x2ECC70 : 0xE74D3C;

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(embedColor);
        builder.setTitle(
                new ColorTool().parse(translation.application().embedApplicationMessageTitle()));
        builder.setDescription(new ColorTool().parse(
                translation.application().embedApplicationMessageBody().replace("%status%", status)
                        .replace("%active_roles%", roleStringPair.toString())));
        MessageEmbed embed = builder.build();

        MessageTable messageTable = new MessageTable();
        MessageTable.MessageTableData applicationMessage =
                messageTable.queryFromTable(MessageType.APPLICATION);

        Message message = null;

        if (applicationMessage != null) {
            try {
                TextChannel msgChannel = api.getTextChannelById(applicationMessage.channelId);
                if (msgChannel != null) {
                    message = msgChannel.editMessageById(applicationMessage.id, "").setEmbeds(embed)
                            .complete();
                }
            } catch (ErrorResponseException e) {
                message =
                        channel.sendMessageEmbeds(embed)
                                .setComponents(
                                        ActionRow.of(Button
                                                .success("application_open",
                                                        translation.buttons()
                                                                .textApplicationOpenTickets())
                                                .withEmoji(Emoji.fromFormatted("\uD83D\uDCD8"))))
                                .complete();
                messageTable.delete(applicationMessage.id);
                messageTable.insertIfNotExists(message.getId(), MessageType.APPLICATION,
                        message.getChannelId());
            }
        } else {
            message =
                    channel.sendMessageEmbeds(embed)
                            .setComponents(
                                    ActionRow.of(Button
                                            .success("application_open",
                                                    translation.buttons()
                                                            .textApplicationOpenTickets())
                                            .withEmoji(Emoji.fromFormatted("\uD83D\uDCD8"))))
                            .complete();
        }

        for (ApplicationType type : GlobalVariables.applicationTypeSet) {
            new ApplicationTypeTable().changeType(type.roleId(), type.state(), type.member(),
                    type.initializer());
        }
        if (message == null) {
            logger.error("Could not mirror applied application settings to database");
            return;
        }

        messageTable.insertIfNotExists(message.getId(), MessageType.APPLICATION,
                message.getChannelId());
    }

    private MessageEmbed createMessage(boolean useBaseValue) {
        StringBuilder roleStringPair = createRoleString(useBaseValue);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(0x2ECC70);
        builder.setTitle(
                new ColorTool().parse(translation.application().embedActivationMenuTitle()));
        builder.setDescription(
                new ColorTool().parse(translation.application().embedActivationMenuBody()
                        .replace("%status%", translation.application().textStatusDeactivated())
                        .replace("%active_roles%", roleStringPair.toString())));
        return builder.build();
    }

    private StringBuilder createRoleString(boolean useBaseValue) {
        List<ApplicationType> applicationTypeList = new ArrayList<>();

        if (useBaseValue && new ApplicationTypeTable().getAllEntries() != null) {
            List<ApplicationType> baseTypes = createStringBaseValue(applicationTypeList);
            GlobalVariables.applicationTypeSet = new HashSet<>(baseTypes);

            for (ApplicationTypeTable.ApplicationType type : new ApplicationTypeTable()
                    .getAllEntries()) {
                for (ApplicationType base : baseTypes) {
                    if (base.roleId().equals(type.roleId)) {
                        new ApplicationManager(config, translation).changeApplicationType(
                                type.roleId, base.name(), base.description(), base.emoji(),
                                type.active, type.initializer, type.members);
                    }
                }
            }
        }
        return createStringValue();
    }

    private List<ApplicationType> createStringBaseValue(List<ApplicationType> applicationTypeList) {
        StringBuilder stringBuilder = new StringBuilder();
        String deactivated =
                new ColorTool().parse(translation.application().textStatusDeactivated());
        int count = 0;
        for (var type : config.ticket().applicationTypes()) {
            count += 1;

            applicationTypeList.add(new ApplicationType(count, type.roleID(), type.name(),
                    type.description(), type.emoji(), false, null, 0));

            stringBuilder.append(new ColorTool().parse(translation.application()
                    .textRoleStatusTemplate().replace("%roleId%", type.roleID())
                    .replace("%status%", deactivated).replace("%max_candidates%", "0")));
        }

        return applicationTypeList;
    }

    private StringBuilder createStringValue() {
        StringBuilder stringBuilder = new StringBuilder();
        String activated = new ColorTool().parse(translation.application().textStatusActive());
        String deactivated =
                new ColorTool().parse(translation.application().textStatusDeactivated());

        for (ApplicationType type : GlobalVariables.applicationTypeSet) {
            String status;
            if (type.state()) {
                status = activated;
            } else {
                status = deactivated;
            }

            stringBuilder
                    .append(new ColorTool().parse(translation.application().textRoleStatusTemplate()
                            .replace("%roleId%", type.roleId()).replace("%status%", status)
                            .replace("%max_candidates%", String.valueOf(type.member()))));
        }

        return stringBuilder;
    }

    private ActionRow applicationActionRow(String userId, List<ApplicationType> types) {
        Collection<ActionRowChildComponent> rows = new ArrayList<>();

        Button add = Button
                .success("application_activation_add:" + userId,
                        translation.buttons().textApplicationActivationAdd())
                .withEmoji(Emoji.fromFormatted("\u2795"));
        Button remove = Button
                .danger("application_activation_remove:" + userId,
                        translation.buttons().textApplicationActivationRemove())
                .withEmoji(Emoji.fromFormatted("\u2796"));
        Button completeSetup = Button
                .primary("application_activation_complete_setup:" + userId,
                        translation.buttons().textApplicationActivationCompleteSetup())
                .withEmoji(Emoji.fromFormatted("\uD83D\uDCBD"));

        if (types == null) {
            remove = remove.asDisabled();
            completeSetup = completeSetup.asDisabled();
        }

        rows.add(add);
        rows.add(remove);
        rows.add(completeSetup);

        return ActionRow.of(rows);
    }

    public static class Pair<A, B> {
        public final A first;
        public final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }
    }
}
