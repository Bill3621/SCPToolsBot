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

import dev.vxrp.bot.commands.handler.bot.application.ApplicationCommand;
import dev.vxrp.bot.commands.handler.bot.help.HelpCommand;
import dev.vxrp.bot.commands.handler.bot.noticeofdeparture.NoticeOfDepartureCommand;
import dev.vxrp.bot.commands.handler.bot.regulars.RegularsCommand;
import dev.vxrp.bot.commands.handler.bot.settings.SettingsCommand;
import dev.vxrp.bot.commands.handler.bot.template.TemplateCommandHandler;
import dev.vxrp.bot.commands.handler.bot.verify.VerifyCommand;
import dev.vxrp.bot.permissions.PermissionManager;
import dev.vxrp.bot.permissions.enums.StatusMessageType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends ListenerAdapter {
    private final JDA api;
    private final Config config;
    private final Translation translation;

    public CommandListener(JDA api, Config config, Translation translation) {
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

        var commandList = config.extra().commands().commands();

        for (var command : commandList) {
            if (!event.getFullCommandName().contains(command.name())) continue;

            checkInheritance(command.inherit(), event);

            if (command.subcommands() == null) continue;
            for (var subCommand : command.subcommands()) {
                String[] parts = event.getFullCommandName().split(" ");
                if (parts.length < 2) continue;
                if (!subCommand.name().equals(parts[1])) continue;

                if (checkSubInheritance(subCommand.inherit(), event)) continue;
            }
        }
    }

    private void checkInheritance(String inherit, SlashCommandInteractionEvent event) {
        switch (inherit) {
            case "commands.help.default":
                helpCommand(event);
                return;
            case "commands.template.default":
                templateCommand(event);
                return;
            case "commands.verify.default": {
                MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                        StatusMessageType.COMMAND,
                        config.settings().verify().active(),
                        config.settings().webserver().active()
                );
                if (embed != null) {
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                } else {
                    verifyCommand(event);
                }
                return;
            }
            case "commands.settings.default":
                settingsCommand(event);
                return;
            case "commands.application.default": {
                MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                        StatusMessageType.COMMAND,
                        !config.ticket().settings().applicationMessageChannel().isEmpty()
                );
                if (embed != null) {
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                } else {
                    applicationCommand(event);
                }
                return;
            }
        }
    }

    private boolean checkSubInheritance(String inherit, SlashCommandInteractionEvent event) {
        switch (inherit) {
            case "notice_of_departure.view.sub": {
                MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                        StatusMessageType.COMMAND,
                        config.settings().noticeOfDeparture().active()
                );
                if (embed != null) {
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                } else {
                    new NoticeOfDepartureCommand(config, translation).view(event);
                }
                return true;
            }
            case "notice_of_departure.revoke.sub": {
                MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                        StatusMessageType.COMMAND,
                        config.settings().noticeOfDeparture().active()
                );
                if (embed != null) {
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                } else {
                    new NoticeOfDepartureCommand(config, translation).revoke(event);
                }
                return true;
            }
            case "regulars.view.sub": {
                MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                        StatusMessageType.COMMAND,
                        config.settings().regulars().active(),
                        config.settings().verify().active(),
                        config.settings().webserver().active()
                );
                if (embed != null) {
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                } else {
                    new RegularsCommand(config, translation).view(event);
                }
                return true;
            }
            case "regulars.remove.sub": {
                MessageEmbed embed = new PermissionManager(config, translation).checkStatus(
                        StatusMessageType.COMMAND,
                        config.settings().regulars().active(),
                        config.settings().verify().active(),
                        config.settings().webserver().active()
                );
                if (embed != null) {
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                } else {
                    new RegularsCommand(config, translation).remove(event);
                }
                return true;
            }
        }
        return false;
    }

    private void helpCommand(SlashCommandInteractionEvent event) {
        new HelpCommand(translation).pasteHelpMenu(event);
    }

    private void templateCommand(SlashCommandInteractionEvent event) {
        new TemplateCommandHandler(config, translation).findOption(event);
    }

    private void verifyCommand(SlashCommandInteractionEvent event) {
        new VerifyCommand(config, translation).pasteVerifyMenu(event);
    }

    private void settingsCommand(SlashCommandInteractionEvent event) {
        new SettingsCommand(config, translation).pasteSettingsMenu(event);
    }

    private void applicationCommand(SlashCommandInteractionEvent event) {
        String option = event.getOption("state") != null ? event.getOption("state").getAsString() : null;
        var applicationCommand = new ApplicationCommand(config, translation);

        if ("active".equals(option)) {
            applicationCommand.sendActivationMessage(event);
        } else if ("deactivated".equals(option)) {
            applicationCommand.sendDeactivationMessage(event);
        }
    }
}
