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

package dev.vxrp.bot.events.buttons;

import dev.vxrp.bot.regulars.RegularsManager;
import dev.vxrp.bot.regulars.handler.RegularsFileHandler;
import dev.vxrp.bot.regulars.handler.RegularsMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.UserTable;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class RegularsButtons {
    private final ButtonInteractionEvent event;
    private final Config config;
    private final Translation translation;

    public RegularsButtons(ButtonInteractionEvent event, Config config, Translation translation) {
        this.event = event;
        this.config = config;
        this.translation = translation;
    }

    public void init() {
        String buttonId = event.getButton().getCustomId();
        if (buttonId == null)
            return;

        if (buttonId.startsWith("regulars_open_settings")) {
            var regularsMessageHandler = new RegularsMessageHandler(event.getJDA(), config, translation);

            event.replyEmbeds(regularsMessageHandler.getSettings(event.getUser(), null, null))
                    .setComponents(ActionRow.of(
                            regularsMessageHandler.getSettingsActionRow(event.getUser().getId())))
                    .setEphemeral(true).queue();
        }

        if ("regulars_sync".equals(buttonId)) {
            if (notVerified())
                return;

            var embed = new EmbedBuilder()
                    .setTitle(new ColorTool().parse(translation.regulars().embedSyncGroupSelectTitle()))
                    .setDescription(new ColorTool().parse(translation.regulars().embedSyncGroupSelectBody()))
                    .build();

            event.getMessage().delete().queue();
            var menuBuilder = StringSelectMenu.create("regulars_group_select");
            for (var group : new RegularsFileHandler(config).query()) {
                menuBuilder.addOption(group.manifest().name(), group.manifest().name());
            }
            event.replyEmbeds(embed).setComponents(ActionRow.of(menuBuilder.build())).setEphemeral(true).queue();
        }

        if (buttonId.startsWith("regulars_sync_reactivate")) {
            var regularsMessageHandler = new RegularsMessageHandler(event.getJDA(), config, translation);
            if (notVerified())
                return;

            new RegularsManager(event.getJDA(), config, translation).reactivateSync(event.getUser().getId());
            var embed = new EmbedBuilder()
                    .setColor(0x2ECC70)
                    .setTitle(new ColorTool().parse(translation.regulars().embedSyncReactivatedTitle()))
                    .setDescription(new ColorTool().parse(translation.regulars().embedSyncReactivatedBody()))
                    .build();

            event.getMessage().delete().queue();
            event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
            event.replyEmbeds(new RegularsMessageHandler(event.getJDA(), config, translation)
                    .getSettings(event.getUser(), null, null)).setComponents(ActionRow.of(
                            regularsMessageHandler.getSettingsActionRow(event.getUser().getId())))
                    .setEphemeral(true).queue();
        }

        if ("regulars_sync_remove".equals(buttonId)) {
            if (notVerified())
                return;

            var embed = new EmbedBuilder()
                    .setTitle(new ColorTool().parse(translation.regulars().embedSyncRemovedConfirmTitle()))
                    .setDescription(new ColorTool().parse(translation.regulars().embedSyncRemovedConfirmBody()))
                    .build();

            event.getMessage().delete().queue();
            event.replyEmbeds(embed).setComponents(ActionRow.of(
                    Button.success("regulars_sync_remove_confirm", translation.buttons().textRegularSyncRemove())))
                    .setEphemeral(true).queue();
        }

        if (buttonId.startsWith("regulars_sync_remove_confirm")) {
            var regularsMessageHandler = new RegularsMessageHandler(event.getJDA(), config, translation);
            if (notVerified())
                return;

            new RegularsManager(event.getJDA(), config, translation).removeSync(event.getUser().getId());
            var embed = new EmbedBuilder()
                    .setColor(0x2ECC70)
                    .setTitle(new ColorTool().parse(translation.regulars().embedSyncRemovedTitle()))
                    .setDescription(new ColorTool().parse(translation.regulars().embedSyncRemovedBody()))
                    .build();

            event.getMessage().delete().queue();
            event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
            event.replyEmbeds(new RegularsMessageHandler(event.getJDA(), config, translation)
                    .getSettings(event.getUser(), null, null)).setComponents(ActionRow.of(
                            regularsMessageHandler.getSettingsActionRow(event.getUser().getId())))
                    .setEphemeral(true).queue();
        }
    }

    private boolean notVerified() {
        if (!new UserTable().exists(event.getUser().getId())) {
            var embed = new EmbedBuilder()
                    .setColor(0xE74D3C)
                    .setTitle(new ColorTool().parse(translation.regulars().embedNotVerifiedTitle()))
                    .setDescription(new ColorTool().parse(translation.regulars().embedNotVerifiedBody()))
                    .build();
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return true;
        }
        return false;
    }
}
