/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for any
 * form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.events.buttons;

import dev.vxrp.bot.application.ApplicationManager;
import dev.vxrp.bot.application.ApplicationSubmissionService;
import dev.vxrp.bot.application.data.ApplicationType;
import dev.vxrp.bot.application.handler.ApplicationMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.GlobalVariables;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
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
        String buttonId = event.getButton().getCustomId();
        if (buttonId == null) {
            return;
        }

        if (buttonId.startsWith("application_continue:")) {
            new ApplicationSubmissionService(config, translation)
                    .continueApplication(event, buttonId.substring("application_continue:".length()));
            return;
        }

        ApplicationMessageHandler handler = new ApplicationMessageHandler(config, translation);
        if (buttonId.startsWith("application_apply:")) {
            var modal = handler.applicationModal(buttonId.substring("application_apply:".length()));
            if (modal == null) {
                event.reply(handler.positionNotActiveFeedback()).setEphemeral(true).queue();
            } else {
                event.replyModal(modal).queue();
            }
            return;
        }

        if (buttonId.startsWith("application_activation_add") && !nullCheck()) {
            event.editMessage(MessageEditBuilder.fromCreateData(handler.activationSelectionPrompt(
                    "application_activation_add:" + event.getUser().getId())).build()).queue();
        }

        if (buttonId.startsWith("application_activation_remove") && !nullCheck()) {
            event.editMessage(MessageEditBuilder.fromCreateData(handler.activationSelectionPrompt(
                    "application_activation_remove:" + event.getUser().getId())).build()).queue();
        }

        if (buttonId.startsWith("application_activation_complete_setup") && !nullCheck()) {
            if (!config.ticket().settings().applicationMessageChannel().isEmpty()) {
                event.getMessage().delete().queue();
                handler.sendApplicationMessage(event.getJDA(),
                        event.getJDA().getTextChannelById(config.ticket().settings().applicationMessageChannel()),
                        true);
                event.reply(handler.setupFeedback(true)).setEphemeral(true).queue();
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
                    listOfTypes.add(new ApplicationType(position, type.roleID(), type.name(), type.description(),
                            type.emoji(), false, event.getUser().getId(), 0));
                }

                GlobalVariables.applicationTypeSet = new HashSet<>(listOfTypes);
                for (var type : config.ticket().applicationTypes()) {
                    new ApplicationManager(config, translation).changeApplicationType(type.roleID(), null, null, null,
                            false, event.getUser().getId(), 0);
                }

                handler.sendApplicationMessage(event.getJDA(),
                        event.getJDA().getTextChannelById(config.ticket().settings().applicationMessageChannel()),
                        false);
                event.reply(handler.setupFeedback(false)).setEphemeral(true).queue();
            } else {
                logger.warn("Could not deactivate application phase, add channel id in the config to fix");
            }
        }

        if (buttonId.startsWith("application_open")) {
            event.reply(handler.applicationSelectionPrompt()).setEphemeral(true).queue();
        }
    }

    private boolean nullCheck() {
        if (!GlobalVariables.applicationTypeSet.isEmpty()) {
            return false;
        }
        event.getMessage().delete().queue();
        event.reply(new ApplicationMessageHandler(config, translation).expiredFeedback()).setEphemeral(true).queue();
        return true;
    }
}
