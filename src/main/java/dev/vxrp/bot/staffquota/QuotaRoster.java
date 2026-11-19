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

import dev.vxrp.configuration.data.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class QuotaRoster {
    private static final Logger logger = LoggerFactory.getLogger(QuotaRoster.class);

    private final QuotaEvaluator evaluator;

    public QuotaRoster(Config config) {
        this.evaluator = new QuotaEvaluator(config);
    }

    public List<QuotaRow> evaluate(Guild guild, QuotaWeek week, int windowDays) {
        List<Member> staff = guild.findMembers(member -> evaluator.weeklyHours(member).isPresent()).get();
        return staff.stream().map(member -> new QuotaRow(evaluator.evaluate(member.getId(), evaluator.weeklyHours(member).orElseThrow(), week, windowDays), member.getEffectiveName(), avatar(member))).toList();
    }

    private static BufferedImage avatar(Member member) {
        try (InputStream in = member.getEffectiveAvatar().download(64).get(10, TimeUnit.SECONDS)) {
            return ImageIO.read(in);
        } catch (IOException | ExecutionException | TimeoutException e) {
            logger.debug("Could not load avatar of staff member {} for the quota report", member.getId(), e);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
