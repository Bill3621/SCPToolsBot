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

package dev.vxrp.bot.events.buttons;

import dev.vxrp.bot.application.ApplicationManager;
import dev.vxrp.bot.application.data.ApplicationType;
import dev.vxrp.bot.application.handler.ApplicationMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.GlobalVariables;
import dev.vxrp.util.color.ColorTool;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ApplicationButtons {
    private final Logger logger = LoggerFactory.getLogger(ApplicationButtons.class);
    private final ButtonInteractionEvent event;
    private final Config config;
    private final Translation translation;

    public ApplicationButtons(ButtonInteractionEvent event, Config config, Translation translation) {
        this.event = event;
        this.config = config;
        this.translation = translation;
    }

    public void init() {
        String buttonId = event.getButton().getId();
        if (buttonId == null) return;

        if (buttonId.startsWith("application_activation_add") && !nullCheck()) {
            var embed = new EmbedBuilder()
                    .setColor(0x2ECC70)
                    .setTitle(new ColorTool().parse(translation.application().embedChoosePositionTitle()))
                    .setDescription(new ColorTool().parse(translation.application().embedChoosePositionBody()))
                    .build();

            var menuBuilder = StringSelectMenu.create("application_activation_add:" + event.getUser().getId() + ":" + event.getMessage().getId());
            for (var application : GlobalVariables.applicationTypeSet) {
                menuBuilder.addOption(application.name(), application.roleId(), application.description(), Emoji.fromFormatted(application.emoji()));
            }

            event.replyEmbeds(embed).addActionRow(menuBuilder.build()).setEphemeral(true).queue();
        }

        if (buttonId.startsWith("application_activation_remove") && !nullCheck()) {
            var embed = new EmbedBuilder()
                    .setColor(0xE74D3C)
                    .setTitle(new ColorTool().parse(translation.application().embedChoosePositionTitle()))
                    .setDescription(new ColorTool().parse(translation.application().embedChoosePositionBody()))
                    .build();

            var menuBuilder = StringSelectMenu.create("application_activation_remove:" + event.getUser().getId() + ":" + event.getMessageId());
            for (var application : GlobalVariables.applicationTypeSet) {
                menuBuilder.addOption(application.name(), application.roleId(), application.description(), Emoji.fromFormatted(application.emoji()));
            }

            event.replyEmbeds(embed).addActionRow(menuBuilder.build()).setEphemeral(true).queue();
        }

        if (buttonId.startsWith("application_activation_complete_setup") && !nullCheck()) {
            if (!config.ticket().settings().applicationMessageChannel().isEmpty()) {
                event.getMessage().delete().queue();
                new ApplicationMessageHandler(config, translation).sendApplicationMessage(event.getJDA(), event.getJDA().getTextChannelById(config.ticket().settings().applicationMessageChannel()), true);
                var embed = new EmbedBuilder()
                        .setColor(0x2ECC70)
                        .setTitle(new ColorTool().parse(translation.application().embedApplicationActivatedTitle()))
                        .setDescription(new ColorTool().parse(translation.application().embedApplicationActivatedBody()))
                        .build();
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } else {
                logger.warn("Could not complete application setup, add channel id in the config to fix");
            }
        }

        if (buttonId.startsWith("application_deactivate")) {
            if (!config.ticket().settings().applicationMessageChannel().isEmpty()) {
                event.getMessage().delete().queue();

                List<ApplicationType> listOfTypes = new ArrayList<>();
                int position = -1;
                for (var type : config.ticket().applicationTypes()) {
                    position += 1;
                    listOfTypes.add(new ApplicationType(position, type.roleID(), "/", "/", "/", false, event.getUser().getId(), 0));
                }

                GlobalVariables.applicationTypeSet = new HashSet<>(listOfTypes);

                for (var type : config.ticket().applicationTypes()) {
                    new ApplicationManager(config, translation).changeApplicationType(type.roleID(), null, null, null, false, event.getUser().getId(), 0);
                }

                new ApplicationMessageHandler(config, translation).sendApplicationMessage(event.getJDA(), event.getJDA().getTextChannelById(config.ticket().settings().applicationMessageChannel()), false);
                var embed = new EmbedBuilder()
                        .setColor(0xE74D3C)
                        .setTitle(new ColorTool().parse(translation.application().embedApplicationDeactivatedTitle()))
                        .setDescription(new ColorTool().parse(translation.application().embedApplicationDeactivatedBody()))
                        .build();
                event.replyEmbeds(embed).setEphemeral(true).queue();
            } else {
                logger.warn("Could not deactivate application phase, add channel id in the config to fix");
            }
        }

        if (buttonId.startsWith("application_open")) {
            var embed = new EmbedBuilder()
                    .setTitle(new ColorTool().parse(translation.support().embedApplicationPositionTitle()))
                    .setDescription(new ColorTool().parse(translation.support().embedApplicationPositionBody()))
                    .build();

            var menuBuilder = StringSelectMenu.create("application_position");
            for (var type : config.ticket().applicationTypes()) {
                menuBuilder.addOption(type.name(), type.roleID(), type.description(), Emoji.fromFormatted(type.emoji()));
            }

            event.replyEmbeds(embed).addActionRow(menuBuilder.build()).setEphemeral(true).queue();
        }
    }

    private boolean nullCheck() {
        if (!GlobalVariables.applicationTypeSet.isEmpty()) return false;
        event.getMessage().delete().queue();
        event.reply("This message has expired, please execute the command again").setEphemeral(true).queue();
        return true;
    }
}
