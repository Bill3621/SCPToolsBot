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

package dev.vxrp.bot.application;

import dev.vxrp.bot.application.data.ApplicationType;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.configuration.data.Translation;
import dev.vxrp.util.GlobalVariables;

import java.util.HashSet;
import java.util.Set;

public class ApplicationManager {
    private final Config config;
    private final Translation translation;

    public ApplicationManager(Config config, Translation translation) {
        this.config = config;
        this.translation = translation;
    }

    public void changeApplicationType(String roleID, String name, String description, String emoji, Boolean state, String initializer, Integer member) {
        Set<ApplicationType> updated = new HashSet<>();
        for (ApplicationType it : GlobalVariables.applicationTypeSet) {
            if (it.roleId().equals(roleID)) {
                String typeName = name != null ? name : it.name();
                String typeDescription = description != null ? description : it.description();
                String typeEmoji = emoji != null ? emoji : it.emoji();
                boolean typeState = state != null ? state : it.state();
                String typeInitializer = initializer != null ? initializer : it.initializer();
                int typeMember = member != null ? member : it.member();

                updated.add(new ApplicationType(it.pos(), it.roleId(), typeName, typeDescription, typeEmoji, typeState, typeInitializer, typeMember));
            } else {
                updated.add(it);
            }
        }
        GlobalVariables.applicationTypeSet = new HashSet<>(updated);
    }
}
