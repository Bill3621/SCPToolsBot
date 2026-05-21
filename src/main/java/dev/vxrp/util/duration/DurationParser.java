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

package dev.vxrp.util.duration;

import dev.vxrp.util.duration.enums.DurationType;

import java.time.Duration;

public class DurationParser {
    public Duration parse(int value, DurationType durationType) {
        switch (durationType) {
            case NANOSECONDS:
                return Duration.ofNanos(value);
            case MICROSECONDS:
                return Duration.ofNanos((long) value * 1000);
            case MILLISECONDS:
                return Duration.ofMillis(value);
            case SECONDS:
                return Duration.ofSeconds(value);
            case MINUTES:
                return Duration.ofMinutes(value);
            case HOURS:
                return Duration.ofHours(value);
            case DAYS:
                return Duration.ofDays(value);
            default:
                throw new IllegalArgumentException("Unknown duration type: " + durationType);
        }
    }
}
