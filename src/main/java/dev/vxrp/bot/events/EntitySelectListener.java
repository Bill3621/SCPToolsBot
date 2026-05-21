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

import dev.vxrp.bot.events.entitySelectMenus.TicketEntitySelectMenus;
import dev.vxrp.bot.permissions.PermissionManager;
import dev.vxrp.bot.permissions.enums.StatusMessageType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class EntitySelectListener extends ListenerAdapter {
    private final JDA api;
    private final Config config;
    private final Translation translation;

    public EntitySelectListener(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
    }

    @Override
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
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
                new TicketEntitySelectMenus(event, config, translation);
            }
        }
    }
}
