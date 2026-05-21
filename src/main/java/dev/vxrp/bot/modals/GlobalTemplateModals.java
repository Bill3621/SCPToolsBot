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

package dev.vxrp.bot.modals;

import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class GlobalTemplateModals {
    private final Translation translation;

    public GlobalTemplateModals(Translation translation) {
        this.translation = translation;
    }

    public Modal reasonModal(String id) {
        TextInput reason = TextInput.create("reason",
                        translation.permissions().modalReasonEnterReasonTitle(),
                        TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(1, 1000)
                .setPlaceholder(translation.permissions().modalReasonEnterReasonPlaceholder())
                .build();

        return Modal.create(id, translation.permissions().modalReasonTitle())
                .addComponents(ActionRow.of(reason))
                .build();
    }
}
