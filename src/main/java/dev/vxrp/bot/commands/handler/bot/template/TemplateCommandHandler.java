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

package dev.vxrp.bot.commands.handler.bot.template;

import dev.vxrp.bot.commands.handler.bot.template.templates.NoticeOfDepartureTemplate;
import dev.vxrp.bot.commands.handler.bot.template.templates.RegularsTemplate;
import dev.vxrp.bot.commands.handler.bot.template.templates.SupportTemplate;
import dev.vxrp.bot.commands.handler.bot.template.templates.VerifyTemplate;
import dev.vxrp.bot.noticeofdeparture.handler.NoticeOfDepartureMessageHandler;
import dev.vxrp.bot.permissions.PermissionManager;
import dev.vxrp.bot.permissions.enums.StatusMessageType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class TemplateCommandHandler {
    private final Config config;
    private final Translation translation;

    public TemplateCommandHandler(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void findOption(SlashCommandInteractionEvent event) {
        String option = event.getOption("template") != null ? event.getOption("template").getAsString() : null;

        if ("support".equals(option)) {
            MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                    StatusMessageType.TEMPLATE,
                    !config.ticket().settings().ticketLogChannel().isEmpty()
            );
            if (embed != null) {
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } else {
                new SupportTemplate(config, translation).pasteTemplate(event);
            }
        } else if ("verify".equals(option)) {
            MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                    StatusMessageType.TEMPLATE,
                    config.settings().verify().active(),
                    config.settings().webserver().active()
            );
            if (embed != null) {
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } else {
                new VerifyTemplate(config, translation).pasteTemplate(event);
            }
        } else if ("notice_of_departure".equals(option)) {
            MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                    StatusMessageType.TEMPLATE,
                    config.settings().noticeOfDeparture().active()
            );
            if (embed != null) {
                event.reply(NoticeOfDepartureMessageHandler.error(embed)).setEphemeral(true).queue();
            } else {
                new NoticeOfDepartureTemplate(config, translation).pasteTemplate(event);
            }
        } else if ("regulars".equals(option)) {
            MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                    StatusMessageType.TEMPLATE,
                    config.settings().regulars().active(),
                    config.settings().verify().active(),
                    config.settings().webserver().active()
            );
            if (embed != null) {
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } else {
                new RegularsTemplate(config, translation).pasteTemplate(event);
            }
        }
    }
}
