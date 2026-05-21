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

package dev.vxrp.bot.commands.listeners;

import dev.vxrp.bot.commands.handler.status.playerlist.PlayerlistCommand;
import dev.vxrp.bot.commands.handler.status.status.StatusCommand;
import dev.vxrp.bot.commands.handler.status.template.TemplateCommandHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class StatusCommandListener extends ListenerAdapter {
    private final JDA api;
    private final Config config;
    private final Translation translation;

    public StatusCommandListener(JDA api, Config config, Translation translation) {
        this.api = api;
        this.config = config;
        this.translation = translation;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getChannel().getType() == ChannelType.PRIVATE) {
            var embed = new EmbedBuilder()
                    .setColor(0xE74D3C)
                    .setTitle(new ColorTool().parse(translation.permissions().embedCommandDeniedTitle()))
                    .setDescription(new ColorTool().parse(translation.permissions().embedCommandDeniedBody()))
                    .build();
            event.replyEmbeds(embed).setEphemeral(true).queue();
            return;
        }

        var commandList = config.extra().commands().statusCommands();

        for (var command : commandList) {
            if (!command.name().equals(event.getFullCommandName())) continue;
            checkInheritance(command.inherit(), event);
            break;
        }
    }

    private void checkInheritance(String inherit, SlashCommandInteractionEvent event) {
        switch (inherit) {
            case "status_commands.status.default":
                statusCommand(event);
                break;
            case "status_commands.playerlist.default":
                playerListCommand(event);
                break;
            case "status_commands.template.default":
                templateCommand(event);
                break;
        }
    }

    private void statusCommand(SlashCommandInteractionEvent event) {
        if (event.getOption("setting") != null && "maintenance".equals(event.getOption("setting").getAsString())) {
            new StatusCommand(config, translation).changeMaintenanceState(event);
        }
    }

    private void playerListCommand(SlashCommandInteractionEvent event) {
        new PlayerlistCommand(config, translation).pastePlayerList(event);
    }

    private void templateCommand(SlashCommandInteractionEvent event) {
        if (event.getOption("template") != null && "playerlist".equals(event.getOption("template").getAsString())) {
            new TemplateCommandHandler(config, translation).pastePlayerList(event);
        }
    }
}
