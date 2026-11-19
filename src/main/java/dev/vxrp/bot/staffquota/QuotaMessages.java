/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain a copy of the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.staffquota;

import dev.vxrp.bot.staffquota.QuotaStanding.LookupFailed;
import dev.vxrp.bot.staffquota.QuotaStanding.NotLinked;
import dev.vxrp.bot.staffquota.QuotaStanding.OnLeave;
import dev.vxrp.bot.staffquota.QuotaStanding.Tracked;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.configuration.data.TranslationStaffQuota;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.awt.Color;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class QuotaMessages {
    private static final String REPORT_FILE = "staff-quota.png";
    private static final String PROGRESS_FILE = "quota-progress.png";
    private static final Color NEUTRAL = new Color(0x5865F2);

    private final TranslationStaffQuota text;
    private final DateTimeFormatter dateFormat;
    private final QuotaCard card;
    private final ColorTool colorTool = new ColorTool();

    public QuotaMessages(Translation translation, String dateFormatting) {
        this.text = translation.staffQuota();
        this.dateFormat = DateTimeFormatter.ofPattern(dateFormatting);
        this.card = new QuotaCard(text, dateFormat);
    }

    MessageCreateData report(QuotaWeek week, List<QuotaRow> rows) {
        List<String> missed = rows.stream().map(QuotaRow::standing).filter(s -> s instanceof Tracked t && !t.met()).map(s -> "<@" + s.userId() + ">").toList();
        var embed = new EmbedBuilder().setTitle(parse(text.embedReportTitle(), week)).setColor(missed.isEmpty() ? QuotaCard.MET : QuotaCard.MISSED);

        if (rows.isEmpty()) return new MessageCreateBuilder().setEmbeds(embed.setDescription(colorTool.parse(text.textReportEmpty())).build()).build();
        if (!missed.isEmpty()) embed.setDescription(colorTool.parse(text.textReportMissed()) + " " + String.join(" ", missed));

        embed.setImage("attachment://" + REPORT_FILE);
        return new MessageCreateBuilder().setEmbeds(embed.build()).setFiles(FileUpload.fromData(card.report(week, rows, true), REPORT_FILE)).build();
    }

    public MessageEditData currentReport(QuotaWeek week, LocalDate today, List<QuotaRow> rows) {
        var embed = new EmbedBuilder().setTitle(parse(text.embedReportTitle(), week)).setColor(NEUTRAL);
        if (rows.isEmpty()) return edit(embed.setDescription(colorTool.parse(text.textReportEmpty())).build());

        embed.setImage("attachment://" + REPORT_FILE).setFooter(text.textCardDaysLeft().replace("%days%", String.valueOf(week.daysLeft(today))));
        return new MessageEditBuilder().setEmbeds(embed.build()).setFiles(FileUpload.fromData(card.report(week, rows, false), REPORT_FILE)).build();
    }

    MessageCreateData missedDm(QuotaWeek week, Tracked standing) {
        String shortBy = text.textCardShort().replace("%hours%", QuotaCard.hours(standing.required() - standing.hours()));
        byte[] png = card.progress(standing, QuotaCard.MISSED, shortBy, card.weekLabel(week));
        var embed = new EmbedBuilder().setColor(QuotaCard.MISSED).setTitle(colorTool.parse(text.embedMissedDmTitle())).setImage("attachment://" + PROGRESS_FILE).build();
        return new MessageCreateBuilder().setEmbeds(embed).setFiles(FileUpload.fromData(png, PROGRESS_FILE)).build();
    }

    public MessageEditData view(String userName, QuotaWeek week, LocalDate today, QuotaStanding standing) {
        var embed = new EmbedBuilder().setTitle(colorTool.parse(text.embedViewTitle().replace("%user%", userName)));
        return switch (standing) {
            case Tracked t -> {
                String note = t.met() ? text.textCardMet() : text.textCardToGo().replace("%hours%", QuotaCard.hours(t.required() - t.hours()));
                String daysLeft = text.textCardDaysLeft().replace("%days%", String.valueOf(week.daysLeft(today)));
                byte[] png = card.progress(t, t.met() ? QuotaCard.MET : QuotaCard.ON_TRACK, note, daysLeft);
                embed.setColor(t.met() ? QuotaCard.MET : NEUTRAL).setImage("attachment://" + PROGRESS_FILE).setFooter(card.weekLabel(week));
                yield new MessageEditBuilder().setEmbeds(embed.build()).setFiles(FileUpload.fromData(png, PROGRESS_FILE)).build();
            }
            case OnLeave _ -> edit(embed.setColor(NEUTRAL).setDescription(parse(text.embedViewOnLeaveBody(), week)).build());
            case NotLinked _ -> edit(error(text.embedNotLinkedBody().replace("%user%", userName)));
            case LookupFailed _ -> edit(error(text.embedLookupFailedBody()));
        };
    }

    public MessageEmbed notStaff(String userName) {
        return error(text.embedNotStaffBody().replace("%user%", userName));
    }

    public MessageEmbed noPermission() {
        return error(text.embedNoPermissionBody());
    }

    private MessageEmbed error(String body) {
        return new EmbedBuilder().setColor(QuotaCard.MISSED).setTitle(colorTool.parse(text.embedErrorTitle())).setDescription(colorTool.parse(body)).build();
    }

    private static MessageEditData edit(MessageEmbed embed) {
        return new MessageEditBuilder().setEmbeds(embed).build();
    }

    private String parse(String template, QuotaWeek week) {
        return colorTool.parse(template.replace("%week%", week.start().format(dateFormat)));
    }
}
