/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.regulars.handler;

import dev.vxrp.bot.regulars.data.RegularsConfigRole;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.RegularsTable;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.List;

public class RegularsMessageHandler {
    private final JDA api;
    private final Config config;
    private final Translation translation;

    public RegularsMessageHandler(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
    }

    public void sendRegulars(TextChannel channel) {
        List<MessageEmbed> embeds = new ArrayList<>();
        EmbedBuilder mainBuilder = new EmbedBuilder();
        mainBuilder.setThumbnail(api.getGuildById(config.settings().guildId()) != null
                ? api.getGuildById(config.settings().guildId()).getIconUrl()
                : null);
        mainBuilder.setTitle(new ColorTool().parse(translation.regulars().embedTemplateTitle()));
        mainBuilder.setDescription(new ColorTool().parse(translation.regulars().embedTemplateBody()));
        embeds.add(mainBuilder.build());

        var regulars = new RegularsFileHandler(config).query();
        for (var regular : regulars) {
            StringBuilder stringBuilder = new StringBuilder();

            for (var role : regular.config().roles()) {
                String timeframe = "";
                String playtime = "";
                String xp = "";
                String level = "";

                if (role.requirementType().equals("PLAYTIME") || role.requirementType().equals("BOTH")) {
                    timeframe = translation.regulars().textTemplateRoleTimeframe();
                    playtime = role.playtimeRequirements() + "h";
                }
                if (role.requirementType().equals("XP") || role.requirementType().equals("BOTH")) {
                    xp = translation.regulars().textTemplateRoleXp();
                    level = role.xpRequirements() + " Level";
                }

                stringBuilder.append(new ColorTool().parse(
                        translation.regulars().embedTemplateRoleBody()
                                .replace("%role%", "<@&" + role.id() + ">")
                                .replace("%description%", role.description())
                                .replace("%timeframe%", timeframe)
                                .replace("%playtime%", playtime)
                                .replace("%xp%", xp)
                                .replace("%level%", level)));
            }

            String groupRoleStr = "<@&" + regular.manifest().customRole().id() + ">";
            if (regular.manifest().customRole().id().isEmpty()) {
                groupRoleStr = "None";
            }

            EmbedBuilder groupBuilder = new EmbedBuilder();
            groupBuilder.setDescription(new ColorTool().parse(
                    translation.regulars().embedTemplateGroupBody()
                            .replace("%group%", regular.manifest().name())
                            .replace("%description%", regular.manifest().description())
                            .replace("%group_role%", groupRoleStr)
                            .replace("%roles%", stringBuilder.toString())));

            embeds.add(groupBuilder.build());
        }

        channel.sendMessageEmbeds(embeds)
                .setComponents(ActionRow
                        .of(Button.success("regulars_open_settings", translation.buttons().textRegularOpenSettings())))
                .queue();
    }

    public MessageEmbed getSettings(User user, String injectTitle, String injectDescription) {
        String groupRole = "None";
        String role = "None";
        String playtime = "0";
        String level = "0";
        String requirementType = "";
        String lastChecked = "None";

        RegularsTable table = new RegularsTable();
        if (table.exists(user.getId())) {
            RegularsConfigRole regularRole = new RegularsFileHandler(config)
                    .queryRoleFromConfig(table.getGroup(user.getId()), table.getRole(user.getId()));

            requirementType = regularRole != null ? regularRole.requirementType() : "";
            String groupRoleVal = table.getGroupRole(user.getId());
            groupRole = "<@&" + (groupRoleVal != null ? groupRoleVal : "None") + ">";
            String roleVal = table.getRole(user.getId());
            role = "<@&" + (roleVal != null ? roleVal : "None") + ">";
            playtime = String.valueOf(Math.round(table.getPlaytime(user.getId())));
            level = String.valueOf(table.getLevel(user.getId()));
            lastChecked = table.getLastChecked(user.getId()) != null ? table.getLastChecked(user.getId()) : "None";
        }

        String embedTitle = injectTitle != null ? injectTitle : translation.regulars().embedSettingsTitle();
        String embedDescription = injectDescription != null ? injectDescription
                : translation.regulars().embedSettingsBody();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(user.getAvatarUrl());
        builder.setTitle(new ColorTool().parse(embedTitle));
        builder.setDescription(new ColorTool().parse(embedDescription));

        String groupRoleDisplay = groupRole.replace("<@&>", "None");
        String roleDisplay = role.replace("<@&>", "None");

        builder.addField(translation.regulars().embedSettingsFieldGroupName(), groupRoleDisplay, true);
        builder.addField(translation.regulars().embedSettingsFieldRoleName(), roleDisplay, true);

        if (requirementType.equals("PLAYTIME") || requirementType.equals("BOTH")) {
            builder.addField(translation.regulars().embedSettingsFieldPlaytimeName(), playtime, true);
        }
        if (requirementType.equals("XP") || requirementType.equals("BOTH")) {
            builder.addField(translation.regulars().embedSettingsFieldXp(), level, true);
        }
        builder.addField(translation.regulars().embedSettingsFieldLastChecked(), lastChecked, true);
        if (!lastChecked.equals("None")) {
            builder.addField("", "", true);
        }

        return builder.build();
    }

    public List<Button> getSettingsActionRow(String userId) {
        RegularsTable table = new RegularsTable();
        Button syncButton = Button.success("regulars_sync", translation.buttons().textRegularSync());
        Button syncReactivateButton = Button.success("regulars_sync_reactivate",
                translation.buttons().textRegularSyncReactivate());
        Button syncDeactivateButton = Button.danger("regulars_sync_deactivate",
                translation.buttons().textRegularSyncDeactivate());
        Button syncRemoveButton = Button.danger("regulars_sync_remove", translation.buttons().textRegularSyncRemove());

        if (!table.exists(userId)) {
            syncReactivateButton = syncReactivateButton.asDisabled();
            syncDeactivateButton = syncDeactivateButton.asDisabled();
            syncRemoveButton = syncRemoveButton.asDisabled();

            return List.of(syncButton, syncReactivateButton, syncDeactivateButton, syncRemoveButton);
        }

        if (table.getActive(userId)) {
            syncButton = syncButton.asDisabled();
            syncReactivateButton = syncReactivateButton.asDisabled();
        } else {
            syncDeactivateButton = syncDeactivateButton.asDisabled();
        }

        return List.of(syncButton, syncReactivateButton, syncDeactivateButton, syncRemoveButton);
    }
}
