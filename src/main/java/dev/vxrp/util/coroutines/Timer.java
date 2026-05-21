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

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Timer {
    public void runWithTimer(Duration period, ScheduledExecutorService executor, Runnable task) {
        AtomicBoolean taskExecuted = new AtomicBoolean(false);
        executor.scheduleAtFixedRate(() -> {
            try {
                task.run();
                taskExecuted.set(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, 0, period.toMillis(), TimeUnit.MILLISECONDS);
        assert taskExecuted.get();
    }

    public void runLooped(ScheduledExecutorService executor, Runnable task) {
        AtomicBoolean taskExecuted = new AtomicBoolean(false);
        executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    task.run();
                    taskExecuted.set(true);
                } catch (Exception e) {
                    Thread.currentThread().getUncaughtExceptionHandler()
                            .uncaughtException(Thread.currentThread(), e);
                }
            }
        });
        assert taskExecuted.get();
    }
}
