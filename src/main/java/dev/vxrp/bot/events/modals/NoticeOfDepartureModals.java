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

import dev.vxrp.bot.modals.NoticeOfDepartureTemplateModals;
import dev.vxrp.bot.noticeofdeparture.LeaveDateParser;
import dev.vxrp.bot.noticeofdeparture.NoticeOfDepartureDrafts;
import dev.vxrp.bot.noticeofdeparture.NoticeOfDepartureManager;
import dev.vxrp.bot.noticeofdeparture.handler.NoticeOfDepartureMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.NoticeOfDepartureTable;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
            fileNotice();
            return;
        }

        if (event.getModalId().startsWith("notice_of_departure_reason_action_REVOKING")) {
            String[] splitId = event.getModalId().split(":");

            String reason = event.getValues().get(0).getAsString();
            String userId = splitId[1];
            String date = splitId[2];

            new NoticeOfDepartureManager(event.getJDA(), config, translation).revokeNotice(reason, userId, date);

            reply(translation.noticeOfDeparture().embedRevokationSentTitle(),
                    translation.noticeOfDeparture().embedRevokationSentBody(), true);
        }
    }

    private void fileNotice() {
        var text = translation.noticeOfDeparture();
        String userId = event.getUser().getId();
        String dateFormat = config.settings().noticeOfDeparture().dateFormatting();
        var draft = new NoticeOfDepartureDrafts.Draft(value(NoticeOfDepartureTemplateModals.START_DATE_ID),
                value(NoticeOfDepartureTemplateModals.RETURN_DATE_ID), value(NoticeOfDepartureTemplateModals.EXPLANATION_ID));

        if (new NoticeOfDepartureTable().exists(userId)) {
            reply(text.embedAlreadyFiledTitle(), text.embedAlreadyFiledBody(), false);
            return;
        }

        var parser = new LeaveDateParser(dateFormat);
        var today = LocalDate.now();
        Optional<LocalDate> start = draft.startDate().isBlank() ? Optional.of(today) : parser.parse(draft.startDate(), today);
        if (start.isEmpty()) {
            reject(draft, text.embedStartDateInvalidTitle(), text.embedStartDateInvalidBody().replace("%formatter%", dateFormat));
            return;
        }
        if (start.get().isBefore(today)) {
            reject(draft, text.embedStartDatePastTitle(), text.embedStartDatePastBody().replace("%formatter%", dateFormat));
            return;
        }

        Optional<LocalDate> end = parser.parse(draft.returnDate(), start.get());
        if (end.isEmpty()) {
            reject(draft, text.embedEnterValidDateTitle(), text.embedEnterValidDateBody().replace("%formatter%", dateFormat));
            return;
        }
        if (!end.get().isAfter(today)) {
            reject(draft, text.embedEnterFutureDateTitle(), text.embedEnterFutureDateBody().replace("%formatter%", dateFormat));
            return;
        }
        if (!start.get().isBefore(end.get())) {
            reject(draft, text.embedStartDateAfterEndTitle(), text.embedStartDateAfterEndBody());
            return;
        }

        var formatter = DateTimeFormatter.ofPattern(dateFormat);
        new NoticeOfDepartureManager(event.getJDA(), config, translation)
                .createNotice(draft.explanation(), userId, userId, end.get().format(formatter), start.get().format(formatter));

        String range = date(TimeFormat.DATE_LONG, start.get()) + " → " + date(TimeFormat.DATE_LONG, end.get())
                + " · " + date(TimeFormat.RELATIVE, end.get());
        reply(text.embedDecisionSentTitle(), text.embedDecisionSentBody() + "\n" + range, true);
    }

    private String value(String id) {
        ModalMapping mapping = event.getValue(id);
        return mapping == null ? "" : mapping.getAsString().strip();
    }

    private void reject(NoticeOfDepartureDrafts.Draft draft, String title, String body) {
        NoticeOfDepartureDrafts.save(event.getUser().getId(), draft);
        Button retry = Button.primary("notice_of_departure_retry", translation.buttons().textNoticeOfDepartureRetry());
        event.getHook().sendMessage(NoticeOfDepartureMessageHandler.feedback(
                new ColorTool().parse(title), new ColorTool().parse(body), false, retry)).setEphemeral(true).queue();
    }

    private void reply(String title, String body, boolean success) {
        event.getHook().sendMessage(NoticeOfDepartureMessageHandler.feedback(
                new ColorTool().parse(title), new ColorTool().parse(body), success)).setEphemeral(true).queue();
    }

    private static String date(TimeFormat format, LocalDate date) {
        return format.atInstant(date.atStartOfDay(ZoneId.systemDefault()).toInstant()).toString();
    }
}
