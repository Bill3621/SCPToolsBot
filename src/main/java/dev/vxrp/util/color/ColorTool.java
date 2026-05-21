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

package dev.vxrp.util.color;

import dev.vxrp.util.color.enums.DCColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorTool {
    private final String singleFiller = "\u200E ";

    private final String darkGray = "\u001B[2;30m";
    private final String red = "\u001B[31m";
    private final String green = "\u001B[32m";
    private final String gold = "\u001B[33m";
    private final String lightBlue = "\u001B[34m";
    private final String pink = "\u001B[35m";
    private final String teal = "\u001B[36m";
    private final String white = "\u001B[37m";
    private final String bold = "\u001B[1;2m";
    private final String underline = "\u001B[4;2m";
    private final String reset = "\u001B[0m";

    public String apply(DCColor color, String text) {
        switch (color) {
            case DARK_GRAY:
                return darkGray + text + reset;
            case RED:
                return red + text + reset;
            case GREEN:
                return green + text + reset;
            case GOLD:
                return gold + text + reset;
            case LIGHT_BLUE:
                return lightBlue + text + reset;
            case PINK:
                return pink + text + reset;
            case TEAL:
                return teal + text + reset;
            case WHITE:
                return white + text + reset;
            case BOLD:
                return bold + text + reset;
            case UNDERLINE:
                return underline + text + reset;
            default:
                return text;
        }
    }

    public String parse(String text) {
        Matcher matcher = Pattern.compile("(?<=&filler<).+?(?=>&|$)").matcher(text);
        while (matcher.find()) {
            int count = Math.round((float) Integer.parseInt(matcher.group()) / 2);
            return text.replace("&filler<" + matcher.group() + ">&",
                    singleFiller.repeat(Math.max(0, count)));
        }

        return text
                .replace("&dark_gray&", darkGray)
                .replace("&red&", red)
                .replace("&green&", green)
                .replace("&gold&", gold)
                .replace("&light_blue&", lightBlue)
                .replace("&pink&", pink)
                .replace("&teal&", teal)
                .replace("&white&", white)
                .replace("&bold&", bold)
                .replace("&reset&", reset)
                .replace("&underline&", underline)
                .replace("&filler&", singleFiller.repeat(Math.max(0, 144)))
                .replace("&singleFiller&", singleFiller);
    }
}
