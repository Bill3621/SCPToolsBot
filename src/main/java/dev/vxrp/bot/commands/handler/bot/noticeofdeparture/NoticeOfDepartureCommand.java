/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content is
 * prohibited. If you did not receive a copy of the license at
 *
 * https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software is
 * provided without any sort of WARRANTY, and the authors cannot be held liable for any form of
 * claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.commands.handler.bot.noticeofdeparture;

import dev.vxrp.bot.modals.NoticeOfDepartureTemplateModals;
import dev.vxrp.bot.noticeofdeparture.enums.ActionId;
import dev.vxrp.bot.noticeofdeparture.handler.NoticeOfDepartureMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.NoticeOfDepartureTable;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class NoticeOfDepartureCommand {
    private final Config config;
    private final Translation translation;

    public NoticeOfDepartureCommand(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void view(SlashCommandInteractionEvent event) {
        User user = event.getOptions().get(0).getAsUser();
        if (!checkExistence(event, user))
            return;

        String handler = new NoticeOfDepartureTable().retrieveHandler(user.getId());
        String currentDate = new NoticeOfDepartureTable().retrieveBeginDate(user.getId());
        String endDate = new NoticeOfDepartureTable().retrieveEndDate(user.getId());

        String title = new ColorTool().parse(translation.noticeOfDeparture()
                .embedNoticeViewTitle().replace("%user%", user.getName()));
        String filedBy = new ColorTool().parse(translation.noticeOfDeparture().textNoticeViewFiledBy()
                .replace("%user%", "<@" + handler + ">"));
        event.reply(NoticeOfDepartureMessageHandler.noticeDetails(
                title,
                filedBy,
                currentDate != null ? currentDate : "Unknown",
                endDate != null ? endDate : "Unknown",
                new ColorTool().parse(translation.noticeOfDeparture().textNoticeViewReason())))
                .setEphemeral(true).queue();
    }

    public void revoke(SlashCommandInteractionEvent event) {
        User user = event.getOptions().get(0).getAsUser();
        if (!checkExistence(event, user))
            return;

        String endDate = new NoticeOfDepartureTable().retrieveEndDate(user.getId());
        event.replyModal(new NoticeOfDepartureTemplateModals(config, translation)
                .reasonActionModal(ActionId.REVOKING, user.getId(), endDate)).queue();
    }

    private boolean checkExistence(SlashCommandInteractionEvent event, User user) {
        if (user.isBot() || !new NoticeOfDepartureTable().exists(user.getId())) {
            event.reply(NoticeOfDepartureMessageHandler.feedback(
                    new ColorTool().parse(translation.permissions().embedNotFoundTitle()),
                    new ColorTool().parse(translation.permissions().embedNotFoundBody()), false))
                    .setEphemeral(true).queue();
            return false;
        }
        return true;
    }
}
