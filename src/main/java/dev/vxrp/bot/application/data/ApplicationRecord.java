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

import dev.vxrp.bot.application.enums.ApplicationDecision;
import dev.vxrp.bot.application.enums.ApplicationLifecycle;

import java.time.Instant;

public record ApplicationRecord(
        String id,
        String roleId,
        String issuer,
        String handler,
        ApplicationLifecycle lifecycle,
        ApplicationDecision decision,
        Instant createdAt,
        Instant closedAt,
        String closedBy,
        String closeReason,
        String reviewChannelId,
        String reviewMessageId,
        int quorumRequired) {}
