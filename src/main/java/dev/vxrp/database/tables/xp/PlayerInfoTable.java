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
 * any form of claim, damages or other other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.database.tables.xp;

public class PlayerInfoTable {

    public static final String CREATE_PLAYER_INFO_STEAM =
            "CREATE TABLE IF NOT EXISTS playerinfo_Steam (" +
                    "id BIGINT PRIMARY KEY, " +
                    "xp INTEGER NOT NULL DEFAULT 0, " +
                    "nickname VARCHAR(64) NOT NULL)";

    public static final String CREATE_PLAYER_INFO_DISCORD =
            "CREATE TABLE IF NOT EXISTS playerinfo_Discord (" +
                    "id BIGINT PRIMARY KEY, " +
                    "xp INTEGER NOT NULL DEFAULT 0, " +
                    "nickname VARCHAR(64) NOT NULL)";
}
