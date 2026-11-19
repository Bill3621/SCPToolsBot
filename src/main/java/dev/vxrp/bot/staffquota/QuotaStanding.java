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
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.staffquota;

public sealed interface QuotaStanding {
    String userId();

    record Tracked(String userId, double hours, double required) implements QuotaStanding {
        public boolean met() {
            return hours >= required;
        }

        public double ratio() {
            return required <= 0 ? 1 : hours / required;
        }
    }

    record OnLeave(String userId) implements QuotaStanding {}

    record NotLinked(String userId) implements QuotaStanding {}

    record LookupFailed(String userId) implements QuotaStanding {}
}
