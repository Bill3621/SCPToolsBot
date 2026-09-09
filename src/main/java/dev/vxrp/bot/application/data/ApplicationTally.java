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
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.application.data;

public record ApplicationTally(int approve, int deny, int abstain, int quorumRequired) {
    public int counted() {
        return approve + deny;
    }

    public boolean quorumReached() {
        return counted() >= quorumRequired;
    }

    public Boolean recommendation() {
        if (!quorumReached() || approve == deny) {
            return null;
        }
        return approve > deny;
    }
}
