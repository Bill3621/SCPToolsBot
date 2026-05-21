/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.commands.handler.status.template;

import dev.vxrp.bot.commands.handler.status.playerlist.PlayerlistMessageHandler;
import dev.vxrp.bot.status.enums.PlayerlistType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.database.tables.database.StatusTable;
import dev.vxrp.util.GlobalVariables;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.LocalDate;

public class TemplateCommandHandler {
    private final Config config;
    private final Translation translation;

    public TemplateCommandHandler(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void pastePlayerList(SlashCommandInteractionEvent event) {
        var embed = new PlayerlistMessageHandler().getEmbed(event.getJDA().getSelfUser().getId(), translation);

        var message = event.getChannel().sendMessageEmbeds(embed).complete();
        event.reply(new ColorTool().parse("%filler<1>%")).queue(hook -> hook.deleteOriginal().queue());
        String id = message.getId();

        Integer currentPort = GlobalVariables.statusMappedBots.get(event.getJDA().getSelfUser().getId());
        var server = GlobalVariables.statusMappedServers.get(currentPort);

        new StatusTable().addToDatabase(
                PlayerlistType.PRINTED,
                event.getChannel().getId(),
                id,
                server != null ? String.valueOf(server.getPort()) : "0",
                LocalDate.now().toString(),
                String.valueOf(System.currentTimeMillis())
        );
    }
}
