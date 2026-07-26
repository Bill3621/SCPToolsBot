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
import dev.vxrp.database.tables.database.NoticeOfDepartureTable;
import dev.vxrp.util.color.ColorTool;
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
            String startDateStr = event.getValues().get(1).getAsString();
            String reason = event.getValues().get(2).getAsString();
            var formatter = DateTimeFormatter.ofPattern(config.settings().noticeOfDeparture().dateFormatting());

            LocalDate parsedDate;
            try {
                parsedDate = LocalDate.parse(date, formatter);
            } catch (DateTimeParseException e) {
                reply(translation.noticeOfDeparture().embedEnterValidDateTitle(),
                        translation.noticeOfDeparture().embedEnterValidDateBody()
                                .replace("%formatter%", config.settings().noticeOfDeparture().dateFormatting()),
                        0xE74D3C);
                return;
            }

            var today = LocalDate.now();
            if (parsedDate.isBefore(today) || parsedDate.isEqual(today)) {
                reply(translation.noticeOfDeparture().embedEnterFutureDateTitle(),
                        translation.noticeOfDeparture().embedEnterFutureDateBody()
                                .replace("%formatter%", config.settings().noticeOfDeparture().dateFormatting()),
                        0xE74D3C);
                return;
            }

            LocalDate parsedStartDate;
            if (startDateStr != null && !startDateStr.isBlank()) {
                try {
                    parsedStartDate = LocalDate.parse(startDateStr, formatter);
                } catch (DateTimeParseException e) {
                    reply(translation.noticeOfDeparture().embedStartDateInvalidTitle(),
                            translation.noticeOfDeparture().embedStartDateInvalidBody()
                                    .replace("%formatter%", config.settings().noticeOfDeparture().dateFormatting()),
                            0xE74D3C);
                    return;
                }

                if (parsedStartDate.isBefore(today)) {
                    reply(translation.noticeOfDeparture().embedStartDatePastTitle(),
                            translation.noticeOfDeparture().embedStartDatePastBody()
                                    .replace("%formatter%", config.settings().noticeOfDeparture().dateFormatting()),
                            0xE74D3C);
                    return;
                }

                if (!parsedStartDate.isBefore(parsedDate)) {
                    reply(translation.noticeOfDeparture().embedStartDateAfterEndTitle(),
                            translation.noticeOfDeparture().embedStartDateAfterEndBody(), 0xE74D3C);
                    return;
                }
            } else {
                parsedStartDate = today;
            }

            String startDateFormatted = parsedStartDate.format(formatter);
            if (new NoticeOfDepartureTable().exists(event.getUser().getId())) {
                reply("Notice of Departure Already Filed",
                        "You already have an active notice of departure.", 0xE74D3C);
                return;
            }

            new NoticeOfDepartureManager(event.getJDA(), config, translation)
                    .createNotice(reason, event.getUser().getId(), event.getUser().getId(), date, startDateFormatted);

            reply(translation.noticeOfDeparture().embedDecisionSentTitle(),
                    translation.noticeOfDeparture().embedDecisionSentBody(), 0x2ECC70);
        }

        if (event.getModalId().startsWith("notice_of_departure_reason_action_REVOKING")) {
            String[] splitId = event.getModalId().split(":");

            String reason = event.getValues().get(0).getAsString();
            String userId = splitId[1];
            String date = splitId[2];

            new NoticeOfDepartureManager(event.getJDA(), config, translation).revokeNotice(reason, userId, date);

            reply(translation.noticeOfDeparture().embedRevokationSentTitle(),
                    translation.noticeOfDeparture().embedRevokationSentBody(), 0x2ECC70);
        }
    }

    private void reply(String title, String body, int color) {
        event.getHook().sendMessage(NoticeOfDepartureMessageHandler.feedback(
                new ColorTool().parse(title), new ColorTool().parse(body), color == 0x2ECC70)).setEphemeral(true).queue();
    }
}
