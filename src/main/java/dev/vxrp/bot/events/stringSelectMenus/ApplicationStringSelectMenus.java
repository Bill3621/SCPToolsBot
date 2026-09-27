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
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for any
 * form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.events.stringSelectMenus;

import dev.vxrp.bot.application.handler.ApplicationMessageHandler;
import dev.vxrp.bot.modals.ApplicationTemplateModals;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

public class ApplicationStringSelectMenus {
    private final StringSelectInteractionEvent event;
    private final Config config;
    private final Translation translation;

    public ApplicationStringSelectMenus(StringSelectInteractionEvent event, Config config, Translation translation) {
        this.event = event;
        this.config = config;
        this.translation = translation;
    }

    public void init() {
        String menuId = event.getComponent().getCustomId();
        if (menuId == null) {
            return;
        }

        ApplicationMessageHandler handler = new ApplicationMessageHandler(config, translation);
        if (menuId.startsWith("application_activation_add")) {
            event.replyModal(new ApplicationTemplateModals(translation)
                    .chooseCountModal(event.getSelectedOptions().get(0).getValue())).queue();
        }

        if (menuId.startsWith("application_activation_remove")) {
            String roleId = event.getSelectedOptions().get(0).getValue();
            event.editMessage(MessageEditBuilder.fromCreateData(handler.updateActivationMenu(
                    event.getUser().getId(), roleId, false, 0)).build()).queue();
        }

        if (menuId.startsWith("application_position")) {
            var modal = handler.applicationModal(event.getSelectedOptions().get(0).getValue());
            if (modal == null) {
                event.reply(handler.positionNotActiveFeedback()).setEphemeral(true).queue();
            } else {
                event.replyModal(modal).queue();
            }
        }
    }
}
