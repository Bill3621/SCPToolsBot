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

package dev.vxrp.bot.events.modals;

import dev.vxrp.bot.application.handler.ApplicationMessageHandler;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

public class ApplicationModals {
    private final ModalInteractionEvent event;
    private final Config config;
    private final Translation translation;

    public ApplicationModals(ModalInteractionEvent event, Config config, Translation translation) {
        this.event = event;
        this.config = config;
        this.translation = translation;

        if (event.getModalId().startsWith("application_choose_count")) {
            String roleId = event.getModalId().split(":")[1];
            String membersStr = event.getValues().get(0).getAsString();
            Integer members = null;
            try {
                members = Integer.parseInt(membersStr);
            } catch (NumberFormatException ignored) {}

            var payload = new ApplicationMessageHandler(config, translation).updateActivationMenu(
                    event.getUser().getId(), roleId, true, members);
            event.editMessage(MessageEditBuilder.fromCreateData(payload).build()).queue();
        }
    }
}
