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

package dev.vxrp.configuration.handler;

import dev.vxrp.bot.ticket.data.ApplicationTypes;
import dev.vxrp.configuration.data.Config;
import dev.vxrp.database.tables.database.ApplicationTypeTable;

import java.util.ArrayList;
import java.util.List;

public class ConfigFileHandler {
    public void databaseManagement(Config config) {
        List<String> idList = new ArrayList<>();

        for (ApplicationTypes type : config.ticket().applicationTypes()) {
            if (!new ApplicationTypeTable().exists(type.roleID())) {
                new ApplicationTypeTable().addToDatabase(type.roleID(), false, null, null);
            }

            idList.add(type.roleID());
        }

        new ApplicationTypeTable().deleteRedundantValues(idList);
    }
}
