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

package dev.vxrp.bot.events.modals;

import dev.vxrp.bot.ticket.TicketManager;
import dev.vxrp.bot.ticket.enums.TicketStatus;
import dev.vxrp.bot.ticket.enums.TicketType;
import dev.vxrp.bot.ticket.handler.TicketSettingsHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.ApplicationTable;
import dev.vxrp.database.tables.database.ApplicationTypeTable;
import dev.vxrp.database.tables.database.TicketTable;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.slf4j.Logger;

public class TicketModals {
    private final Logger logger;
    private final ModalInteractionEvent event;
    private final Config config;
    private final Translation translation;
    private final net.dv8tion.jda.api.entities.MessageEmbed noHandlerEmbed;

    public TicketModals(Logger logger, ModalInteractionEvent event, Config config, Translation translation) {
        this.logger = logger;
        this.event = event;
        this.config = config;
        this.translation = translation;
        this.noHandlerEmbed = new EmbedBuilder()
                .setTitle(new ColorTool().parse(translation.support().embedTicketNoHandlerTitle()))
                .setDescription(new ColorTool().parse(translation.support().embedTicketNoHandlerBody()))
                .build();
    }

    public void init() {
        event.deferReply(true).queue();

        var api = event.getJDA();

        if (event.getModalId().startsWith("ticket_general")) {
            var handler = new TicketManager(api, config, translation);
            var child = handler.createTicket(TicketType.GENERAL, TicketStatus.OPEN, event.getUser().getId(), null, event.getModalId(), event.getValues());
            respond(child, event);
        }

        if (event.getModalId().startsWith("ticket_report")) {
            var handler = new TicketManager(api, config, translation);
            var child = handler.createTicket(TicketType.REPORT, TicketStatus.OPEN, event.getUser().getId(), null, event.getModalId(), event.getValues());
            respond(child, event);
        }

        if (event.getModalId().startsWith("ticket_error")) {
            var handler = new TicketManager(api, config, translation);
            var child = handler.createTicket(TicketType.ERROR, TicketStatus.OPEN, event.getUser().getId(), null, event.getModalId(), event.getValues());
            respond(child, event);
        }

        if (event.getModalId().startsWith("ticket_unban")) {
            var handler = new TicketManager(api, config, translation);
            var child = handler.createTicket(TicketType.UNBAN, TicketStatus.OPEN, event.getUser().getId(), null, event.getModalId(), event.getValues());
            respond(child, event);
        }

        if (event.getModalId().startsWith("ticket_complaint")) {
            String creator = "anonymous";
            if ("false".equals(event.getModalId().split(":")[2])) creator = event.getUser().getId();

            var handler = new TicketManager(api, config, translation);
            var child = handler.createTicket(TicketType.COMPLAINT, TicketStatus.OPEN, creator, null, event.getModalId(), event.getValues());
            respond(child, event);
        }

        if (event.getModalId().startsWith("ticket_application")) {
            String roleId = event.getModalId().split(":")[1];

            if (new ApplicationTable().retrieveSerial(roleId) >= new ApplicationTypeTable().query(roleId).members) {
                var embed = new EmbedBuilder()
                        .setColor(0xE74D3C)
                        .setTitle(new ColorTool().parse(translation.support().embedNoMoreApplicationsTitle()))
                        .setDescription(new ColorTool().parse(translation.support().embedNoMoreApplicationsBody()
                                .replace("%members%", String.valueOf(new ApplicationTypeTable().query(roleId).members))))
                        .build();
                event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
                return;
            }

            String ageStr = event.getValues().get(1).getAsString();
            try {
                Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                var embed = new EmbedBuilder()
                        .setColor(0xE74D3C)
                        .setTitle(new ColorTool().parse(translation.support().embedApplicationAgeNumericTitle()))
                        .setDescription(new ColorTool().parse(translation.support().embedApplicationAgeNumericBody()))
                        .build();
                event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
                return;
            }

            var handler = new TicketManager(api, config, translation);
            var child = handler.createTicket(TicketType.APPLICATION, TicketStatus.OPEN, event.getUser().getId(), null, event.getModalId(), event.getValues());
            new ApplicationTable().addToDatabase(child != null ? child.getId() : "", roleId, false, false, event.getUser().getId(), null);
            respond(child, event);
        }

        if (event.getModalId().startsWith("ticket_close")) {
            String channelId = event.getModalId().split(":")[1];
            String reason = event.getValues().get(0).getAsString();
            if (new TicketTable().determineHandler(channelId)) {
                event.getHook().sendMessageEmbeds(noHandlerEmbed).setEphemeral(true).queue();
                return;
            }

            var channel = event.getJDA().getThreadChannelById(channelId);
            var embed = new EmbedBuilder()
                    .setColor(0xE74D3C)
                    .setTitle(new ColorTool().parse(translation.support().embedLogClosedTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedLogClosedBody()
                            .replace("reason", reason)))
                    .build();
            event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
            new TicketSettingsHandler(api, config, translation).archiveTicket(event.getUser(), channel, channelId, reason);
        }
    }

    private void respond(ThreadChannel child, ModalInteractionEvent event) {
        if (child == null) {
            var embed = new EmbedBuilder()
                    .setColor(0xE74D3C)
                    .setTitle(new ColorTool().parse(translation.support().embedInteractionChainErrorTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedInteractionChainErrorBody()))
                    .build();
            logger.error("Modal Interaction Suspended, error suspected. Child channel could not correctly be returned");
            event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        var embed = new EmbedBuilder()
                .setColor(0x2ECC70)
                .setTitle(new ColorTool().parse(translation.support().embedTicketCreatedTitle()))
                .setDescription(new ColorTool().parse(translation.support().embedTicketCreatedBody().replace("%channel%", child.getAsMention())))
                .build();
        event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
    }
}
