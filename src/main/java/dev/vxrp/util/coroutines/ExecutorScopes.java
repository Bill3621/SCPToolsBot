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

package dev.vxrp.util.coroutines;

import dev.vxrp.bot.BotManager;
import dev.vxrp.bot.noticeofdeparture.NoticeOfDepartureManager;
import dev.vxrp.bot.regulars.RegularsManager;
import dev.vxrp.bot.status.StatusManager;
import dev.vxrp.web.WebServerManager;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class ExecutorScopes {

    private static ThreadFactory namedThreadFactory(String name, Class<?> loggerClass) {
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setUncaughtExceptionHandler((t, exception) ->
                    LoggerFactory.getLogger(loggerClass)
                            .error("An error occurred in the " + t.getName(), exception));
            return thread;
        };
    }

    public static final ScheduledExecutorService updatesScope =
            Executors.newSingleThreadScheduledExecutor(
                    namedThreadFactory("updates-scope", StatusManager.class));

    public static final ScheduledExecutorService statusbotScope =
            Executors.newSingleThreadScheduledExecutor(
                    namedThreadFactory("statusbot-scope", StatusManager.class));

    public static final ScheduledExecutorService noticeOfDepartureScope =
            Executors.newSingleThreadScheduledExecutor(
                    namedThreadFactory("notice-of-departure-scope", NoticeOfDepartureManager.class));

    public static final ScheduledExecutorService regularsScope =
            Executors.newSingleThreadScheduledExecutor(
                    namedThreadFactory("regulars-scope", RegularsManager.class));

    public static final ScheduledExecutorService webServerScope =
            Executors.newSingleThreadScheduledExecutor(
                    namedThreadFactory("webserver-scope", WebServerManager.class));

    public static final ScheduledExecutorService defaultStatusScope =
            Executors.newSingleThreadScheduledExecutor(
                    namedThreadFactory("default-status-scope", BotManager.class));
}
