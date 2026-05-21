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

package dev.vxrp.bot.events;

import dev.vxrp.bot.events.stringSelectMenus.ApplicationStringSelectMenus;
import dev.vxrp.bot.events.stringSelectMenus.RegularsStringSelectMenus;
import dev.vxrp.bot.events.stringSelectMenus.TicketStringSelectMenus;
import dev.vxrp.bot.permissions.PermissionManager;
import dev.vxrp.bot.permissions.enums.StatusMessageType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class StringSelectListener extends ListenerAdapter {
    private final JDA api;
    private final Config config;
    private final Translation translation;

    public StringSelectListener(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        String menuId = event.getComponent().getId();
        if (menuId == null) return;

        if (menuId.startsWith("ticket")) {
            MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                    StatusMessageType.PANEL,
                    !config.ticket().settings().ticketLogChannel().isEmpty()
            );
            if (embed != null) {
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } else {
                new TicketStringSelectMenus(event, config, translation).init();
            }
        }

        if (menuId.startsWith("application")) {
            MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                    StatusMessageType.PANEL,
                    !config.ticket().settings().applicationMessageChannel().isEmpty()
            );
            if (embed != null) {
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } else {
                new ApplicationStringSelectMenus(event, config, translation).init();
            }
        }

        if (menuId.startsWith("regulars")) {
            MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                    StatusMessageType.PANEL,
                    config.settings().regulars().active(),
                    config.settings().verify().active(),
                    config.settings().webserver().active()
            );
            if (embed != null) {
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } else {
                new RegularsStringSelectMenus(event, config, translation).init();
            }
        }
    }
}
