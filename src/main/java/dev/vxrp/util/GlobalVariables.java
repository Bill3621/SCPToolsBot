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

package dev.vxrp.util;

import dev.vxrp.bot.application.data.ApplicationType;
import dev.vxrp.bot.commands.CommandManager;
import dev.vxrp.bot.status.data.Instance;
import io.github.vxrpenter.secretlab.data.Server;
import net.dv8tion.jda.api.JDA;

import java.util.HashMap;
import java.util.HashSet;

public class GlobalVariables {
    public static volatile String upstreamVersion = "";

    public static volatile HashMap<String, String> statusMappedBots = new HashMap<>();

    public static volatile HashMap<String, Server> statusMappedServers = new HashMap<>();

    public static volatile HashMap<String, Instance> statusInstances = new HashMap<>();

    public static volatile HashMap<String, Boolean> statusApiSessionStatus = new HashMap<>();

    public static volatile HashMap<String, Boolean> statusServerSessionStatus = new HashMap<>();

    public static volatile HashSet<ApplicationType> applicationTypeSet = new HashSet<>();

    public static volatile JDA mainApi = null;

    public static volatile CommandManager mainCommandManager = null;
}
