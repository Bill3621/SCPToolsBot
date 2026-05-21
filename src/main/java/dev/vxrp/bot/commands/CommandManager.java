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

package dev.vxrp.bot.commands;

import dev.vxrp.bot.commands.data.CustomCommand;
import dev.vxrp.bot.commands.data.Options;
import dev.vxrp.bot.commands.data.Subcommands;
import dev.vxrp.configuration.data.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private final Logger logger = LoggerFactory.getLogger(CommandManager.class);
    private final Config config;

    public CommandManager(Config config) {
        this.config = config;
    }

    public void registerSpecificCommands(List<CustomCommand> commands, JDA api) {
        List<CommandData> commandList = new ArrayList<>();
        for (CustomCommand command : commands) {
            if (!command.active()) continue;

            List<Permission> permissions = new ArrayList<>();
            if (command.defaultPermissions() != null) {
                for (String permission : command.defaultPermissions()) {
                    permissions.add(Permission.valueOf(permission));
                }
            }

            SlashCommandData currentCommand = Commands.slash(command.name(), command.description())
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions));

            if (command.options() != null) {
                currentCommand = currentCommand.addOptions(addOptions(command.options()));
            }
            if (command.subcommands() != null) {
                currentCommand = currentCommand.addSubcommands(addSubcommands(command.subcommands()));
            }

            commandList.add(currentCommand);
            logger.info("Registering command {} for bot: {} ({})", command.name(), api.getSelfUser().getName(), api.getSelfUser().getId());
        }

        api.updateCommands().addCommands(commandList).queue();
    }

    private List<OptionData> addOptions(List<Options> options) {
        List<OptionData> optionData = new ArrayList<>();
        for (Options option : options) {
            List<Command.Choice> choices = new ArrayList<>();

            if (option.choices() != null) {
                for (int i = 0; i < option.choices().size(); i++) {
                    choices.add(new Command.Choice(option.choices().get(i).name(), option.choices().get(i).id()));
                }
            }

            OptionData data = new OptionData(OptionType.valueOf(option.type()), option.name(), option.description(), option.isRequired());
            if (!choices.isEmpty()) {
                data.addChoices(choices);
            }
            optionData.add(data);
        }

        return optionData;
    }

    private List<SubcommandData> addSubcommands(List<Subcommands> subcommands) {
        List<SubcommandData> subcommandData = new ArrayList<>();
        for (Subcommands subcommand : subcommands) {
            SubcommandData currentSubCommand = new SubcommandData(subcommand.name(), subcommand.description());

            if (subcommand.options() != null) {
                currentSubCommand = currentSubCommand.addOptions(addOptions(subcommand.options()));
            }

            subcommandData.add(currentSubCommand);
        }
        return subcommandData;
    }
}
