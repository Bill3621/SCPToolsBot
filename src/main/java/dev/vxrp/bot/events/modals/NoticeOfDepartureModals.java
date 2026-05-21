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

import dev.vxrp.bot.noticeofdeparture.NoticeOfDepartureManager;
import dev.vxrp.bot.noticeofdeparture.handler.NoticeOfDepartureMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class NoticeOfDepartureModals {
    private final ModalInteractionEvent event;
    private final Config config;
    private final Translation translation;

    public NoticeOfDepartureModals(ModalInteractionEvent event, Config config, Translation translation) {
        this.event = event;
        this.config = config;
        this.translation = translation;
    }

    public void init() {
        event.deferReply(true).queue();

        if (event.getModalId().startsWith("notice_of_departure_general")) {
            String date = event.getValues().get(0).getAsString();
            String reason = event.getValues().get(1).getAsString();
            var formatter = DateTimeFormatter.ofPattern(config.settings().noticeOfDeparture().dateFormatting());

            LocalDate parsedDate;
            try {
                parsedDate = LocalDate.parse(date, formatter);
            } catch (DateTimeParseException e) {
                var embed = new EmbedBuilder()
                        .setColor(0xE74D3C)
                        .setTitle(new ColorTool().parse(translation.noticeOfDeparture().embedEnterValidDateTitle()))
                        .setDescription(new ColorTool().parse(translation.noticeOfDeparture().embedEnterValidDateBody()
                                .replace("%formatter%", config.settings().noticeOfDeparture().dateFormatting())))
                        .build();
                event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
                return;
            }

            var currentDate = LocalDate.now();
            if (parsedDate.isBefore(currentDate) || parsedDate.isEqual(currentDate)) {
                var embed = new EmbedBuilder()
                        .setColor(0xE74D3C)
                        .setTitle(new ColorTool().parse(translation.noticeOfDeparture().embedEnterFutureDateTitle()))
                        .setDescription(new ColorTool().parse(translation.noticeOfDeparture().embedEnterFutureDateBody()))
                        .build();
                event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
                return;
            }

            new NoticeOfDepartureMessageHandler(event.getJDA(), config, translation).sendDecisionMessage(event.getUser().getId(), date, reason);

            var embed = new EmbedBuilder()
                    .setColor(0x2ECC70)
                    .setTitle(new ColorTool().parse(translation.noticeOfDeparture().embedDecisionSentTitle()))
                    .setDescription(new ColorTool().parse(translation.noticeOfDeparture().embedDecisionSentBody()))
                    .build();
            event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
        }

        if (event.getModalId().startsWith("notice_of_departure_reason_action_ACCEPTING")) {
            String[] splitId = event.getModalId().split(":");

            String reason = event.getValues().get(0).getAsString();
            String userId = splitId[1];
            String date = splitId[2];

            new NoticeOfDepartureMessageHandler(event.getJDA(), config, translation).sendAcceptedMessage(reason, userId, date);
            new NoticeOfDepartureManager(event.getJDA(), config, translation).createNotice(reason, event.getUser().getId(), userId, date);

            var embed = new EmbedBuilder()
                    .setColor(0x2ECC70)
                    .setTitle(new ColorTool().parse(translation.noticeOfDeparture().embedAcceptationSentTitle()))
                    .setDescription(new ColorTool().parse(translation.noticeOfDeparture().embedAcceptationSentBody()))
                    .build();
            event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
            if (event.getMessage() != null) event.getMessage().delete().queue();
        }

        if (event.getModalId().startsWith("notice_of_departure_reason_action_DISMISSING")) {
            String[] splitId = event.getModalId().split(":");

            String reason = event.getValues().get(0).getAsString();
            String userId = splitId[1];

            new NoticeOfDepartureMessageHandler(event.getJDA(), config, translation).sendDismissedMessage(reason, userId);

            var embed = new EmbedBuilder()
                    .setColor(0x2ECC70)
                    .setTitle(new ColorTool().parse(translation.noticeOfDeparture().embedDismissingSentTitle()))
                    .setDescription(new ColorTool().parse(translation.noticeOfDeparture().embedDismissingSentBody()))
                    .build();
            event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
            if (event.getMessage() != null) event.getMessage().delete().queue();
        }

        if (event.getModalId().startsWith("notice_of_departure_reason_action_REVOKING")) {
            String[] splitId = event.getModalId().split(":");

            String reason = event.getValues().get(0).getAsString();
            String userId = splitId[1];
            String date = splitId[2];

            new NoticeOfDepartureManager(event.getJDA(), config, translation).revokeNotice(reason, userId, date);

            var embed = new EmbedBuilder()
                    .setColor(0x2ECC70)
                    .setTitle(new ColorTool().parse(translation.noticeOfDeparture().embedRevokationSentTitle()))
                    .setDescription(new ColorTool().parse(translation.noticeOfDeparture().embedRevokationSentBody()))
                    .build();
            event.getHook().sendMessageEmbeds(embed).setEphemeral(true).queue();
        }
    }
}
