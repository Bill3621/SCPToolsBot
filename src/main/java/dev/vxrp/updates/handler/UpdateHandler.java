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

package dev.vxrp.updates.handler;

import dev.vxrp.updates.data.Updates;
import dev.vxrp.updates.data.UpdatesConfigurationSegment;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UpdateHandler {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateHandler.class);
    private final String dir = System.getProperty("user.dir");

    public void checkUpdated(Updates old, Updates newInstance) {
        if (old.version().equals(newInstance.version())) return;

        String changedMessage = "Your {} {} is out of date, it's structure has been altered in the last update. Please look at the most current version of the {} here: {}";
        String regenerateMessage = "You have activated 'regenerate' for {} file, it will be regenerated";

        logger.warn("We have detected that you have installed an update for SCPToolsBot. The bot will now run an update check to see if your configurations are still up to date...");

        List<UpdatesConfigurationSegment> communalList = new ArrayList<>();
        communalList.addAll(newInstance.configurationUpdate());
        communalList.addAll(newInstance.translationUpdates());
        communalList.addAll(newInstance.regularsUpdate());

        boolean changed = false;
        for (UpdatesConfigurationSegment config : communalList) {
            if (!config.changed()) continue;
            changed = true;
            logger.warn(changedMessage, config.filename(), config.type(), config.filename(), config.upstream());
            if (!config.regenerate()) continue;
            logger.warn(regenerateMessage, config.filename());
            File file = Path.of(dir + config.location()).toFile();
            file.delete();
        }

        if (!changed) logger.info("No configuration files were changed in this update");
        if (newInstance.additionalInformation() != null && !newInstance.additionalInformation().isEmpty()) {
            logger.warn("Additional Information: {}", newInstance.additionalInformation());
        }
    }
}
