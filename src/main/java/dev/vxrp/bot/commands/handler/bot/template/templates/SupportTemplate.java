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

package dev.vxrp.bot.commands.handler.bot.template.templates;

import dev.vxrp.bot.ticket.handler.TicketMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SupportTemplate {
    private final Config config;
    private final Translation translation;

    public SupportTemplate(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void pasteTemplate(SlashCommandInteractionEvent event) {
        new TicketMessageHandler(event.getJDA(), config, translation).sendTemplate(event.getChannel().asTextChannel(), event.getGuild());

        event.reply(new ColorTool().parse("%filler<1>%")).queue(hook -> hook.deleteOriginal().queue());
    }
}
