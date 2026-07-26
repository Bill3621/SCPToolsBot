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

import dev.vxrp.bot.events.modals.ApplicationModals;
import dev.vxrp.bot.events.modals.NoticeOfDepartureModals;
import dev.vxrp.bot.events.modals.TicketModals;
import dev.vxrp.bot.noticeofdeparture.handler.NoticeOfDepartureMessageHandler;
import dev.vxrp.bot.permissions.PermissionManager;
import dev.vxrp.bot.permissions.enums.StatusMessageType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModalListener extends ListenerAdapter {
    private final Logger logger = LoggerFactory.getLogger(ModalListener.class);
    private final JDA api;
    private final Config config;
    private final Translation translation;

    public ModalListener(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().startsWith("ticket")) {
            MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                    StatusMessageType.PANEL,
                    !config.ticket().settings().ticketLogChannel().isEmpty()
            );
            if (embed != null) {
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } else {
                new TicketModals(logger, event, config, translation).init();
            }
        }

        if (event.getModalId().startsWith("application")) {
            MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                    StatusMessageType.PANEL,
                    !config.ticket().settings().applicationMessageChannel().isEmpty()
            );
            if (embed != null) {
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } else {
                new ApplicationModals(event, config, translation);
            }
        }

        if (event.getModalId().startsWith("notice_of_departure")) {
            MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                    StatusMessageType.PANEL,
                    config.settings().noticeOfDeparture().active()
            );
            if (embed != null) {
                event.reply(NoticeOfDepartureMessageHandler.error(embed)).setEphemeral(true).queue();
            } else {
                new NoticeOfDepartureModals(event, config, translation).init();
            }
        }
    }
}
