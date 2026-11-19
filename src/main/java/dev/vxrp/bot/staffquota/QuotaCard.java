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

import dev.vxrp.bot.staffquota.QuotaStanding.LookupFailed;
import dev.vxrp.bot.staffquota.QuotaStanding.NotLinked;
import dev.vxrp.bot.staffquota.QuotaStanding.OnLeave;
import dev.vxrp.bot.staffquota.QuotaStanding.Tracked;
import dev.vxrp.configuration.data.TranslationStaffQuota;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

final class QuotaCard {
    static final Color MET = new Color(0x2ECC71);
    static final Color MISSED = new Color(0xE74D3C);
    static final Color ON_TRACK = new Color(0xF1C40F);

    private static final Color BACKGROUND = new Color(0x111214);
    private static final Color TRACK = new Color(0x2A2C31);
    private static final Color TEXT = new Color(0xE6E6E6);
    private static final Color MUTED = new Color(0x8B8F98);

    private static final int SCALE = 2;
    private static final int WIDTH = 440;
    private static final int PAD = 16;
    private static final int ROW = 26;
    private static final int AVATAR = 22;
    private static final int NAME_WIDTH = 96;
    private static final int VALUE_WIDTH = 92;
    private static final int GAP = 8;

    private static final Font REGULAR = loadFont("/fonts/NotoSans-Regular.ttf");
    private static final Font SEMIBOLD = loadFont("/fonts/NotoSans-SemiBold.ttf");

    private final TranslationStaffQuota text;
    private final DateTimeFormatter dateFormat;

    QuotaCard(TranslationStaffQuota text, DateTimeFormatter dateFormat) {
        this.text = text;
        this.dateFormat = dateFormat;
    }

    byte[] report(QuotaWeek week, List<QuotaRow> rows, boolean closed) {
        List<QuotaRow> counted = rows.stream().filter(row -> row.standing() instanceof Tracked).sorted(Comparator.comparingDouble(QuotaCard::ratioOf)).toList();
        List<QuotaRow> notCounted = rows.stream().filter(row -> !(row.standing() instanceof Tracked)).toList();
        long met = counted.stream().filter(row -> row.standing() instanceof Tracked t && t.met()).count();
        String summary = text.textCardSummary().replace("%met%", String.valueOf(met)).replace("%total%", String.valueOf(counted.size()));

        int height = PAD + 30 + counted.size() * ROW + (notCounted.isEmpty() ? 0 : 26 + notCounted.size() * ROW) + PAD - 4;
        return draw(height, g -> {
            int y = PAD;
            drawText(g, SEMIBOLD, 14, TEXT, weekLabel(week), PAD, y + 14, false);
            drawText(g, REGULAR, 12, MUTED, summary, WIDTH - PAD, y + 14, true);
            y += 30;

            for (QuotaRow row : counted) {
                if (!(row.standing() instanceof Tracked t)) continue;
                drawRow(g, y, row, Math.min(t.ratio(), 1), t.met() ? MET : closed ? MISSED : ON_TRACK, progress(t), TEXT);
                y += ROW;
            }
            if (notCounted.isEmpty()) return;

            y += 10;
            drawText(g, SEMIBOLD, 10.5f, MUTED, text.textCardNotCounted().toUpperCase(Locale.ROOT), PAD, y + 11, false);
            y += 16;
            for (QuotaRow row : notCounted) {
                drawRow(g, y, row, 0, TRACK, reason(row.standing()), MUTED);
                y += ROW;
            }
        });
    }

    byte[] progress(Tracked standing, Color color, String noteLeft, String noteRight) {
        return draw(PAD + 24 + 8 + 14 + 10 + 16 + PAD, g -> {
            int y = PAD;
            drawText(g, SEMIBOLD, 16, TEXT, progress(standing), PAD, y + 17, false);
            drawText(g, REGULAR, 12, MUTED, Math.round(standing.ratio() * 100) + "%", WIDTH - PAD, y + 17, true);
            y += 24 + 8;
            drawBar(g, PAD, y, WIDTH - 2 * PAD, 14, Math.min(standing.ratio(), 1), color);
            y += 14 + 10;
            drawText(g, REGULAR, 12, MUTED, noteLeft, PAD, y + 12, false);
            drawText(g, REGULAR, 12, MUTED, noteRight, WIDTH - PAD, y + 12, true);
        });
    }

    String weekLabel(QuotaWeek week) {
        return text.textCardWeek().replace("%week%", week.start().format(dateFormat));
    }

    static String hours(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private String progress(Tracked t) {
        return text.textProgress().replace("%hours%", hours(t.hours())).replace("%required%", hours(t.required()));
    }

    private String reason(QuotaStanding standing) {
        return switch (standing) {
            case OnLeave _ -> text.textCardOnLeave();
            case NotLinked _ -> text.textCardNotLinked();
            case LookupFailed _ -> text.textCardLookupFailed();
            case Tracked t -> progress(t);
        };
    }

    private static double ratioOf(QuotaRow row) {
        return row.standing() instanceof Tracked t ? t.ratio() : 0;
    }

    private static void drawRow(Graphics2D g, int y, QuotaRow row, double fill, Color color, String value, Color valueColor) {
        int middle = y + ROW / 2;
        drawAvatar(g, row, PAD, middle - AVATAR / 2);

        int nameX = PAD + AVATAR + GAP;
        drawFittedText(g, REGULAR, 12.5f, TEXT, row.name(), nameX, middle, NAME_WIDTH);

        int barX = nameX + NAME_WIDTH + GAP;
        drawBar(g, barX, middle - 4, WIDTH - PAD - VALUE_WIDTH - GAP - barX, 8, fill, color);
        drawText(g, REGULAR, 12, valueColor, value, WIDTH - PAD, middle + 4, true);
    }

    private static void drawBar(Graphics2D g, double x, double y, double width, double height, double fill, Color color) {
        g.setColor(TRACK);
        g.fill(new RoundRectangle2D.Double(x, y, width, height, height, height));
        if (fill <= 0) return;
        g.setColor(color);
        g.fill(new RoundRectangle2D.Double(x, y, Math.max(height, width * fill), height, height, height));
    }

    private static void drawAvatar(Graphics2D g, QuotaRow row, int x, int y) {
        if (row.avatar() != null) {
            g.drawImage(circular(row.avatar(), AVATAR * SCALE), x, y, AVATAR, AVATAR, null);
            return;
        }
        g.setColor(Color.getHSBColor((row.name().hashCode() & 0xFFFF) / 65535f, 0.45f, 0.6f));
        g.fill(new Ellipse2D.Double(x, y, AVATAR, AVATAR));
        String initial = row.name().isEmpty() ? "?" : new String(Character.toChars(Character.toUpperCase(row.name().codePointAt(0))));
        Font font = fontFor(SEMIBOLD, 11, initial);
        FontMetrics metrics = g.getFontMetrics(font);
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.drawString(initial, x + (AVATAR - metrics.stringWidth(initial)) / 2f, y + (AVATAR + metrics.getAscent() - metrics.getDescent()) / 2f);
    }

    private static BufferedImage circular(BufferedImage source, int size) {
        var output = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = output.createGraphics();
        try {
            applyQuality(g);
            g.fill(new Ellipse2D.Double(0, 0, size, size));
            g.setComposite(AlphaComposite.SrcIn);
            g.drawImage(source, 0, 0, size, size, null);
        } finally {
            g.dispose();
        }
        return output;
    }

    private static void drawText(Graphics2D g, Font base, float size, Color color, String value, int x, int y, boolean alignRight) {
        Font font = fontFor(base, size, value);
        g.setFont(font);
        g.setColor(color);
        g.drawString(value, alignRight ? x - g.getFontMetrics(font).stringWidth(value) : x, y);
    }

    private static void drawFittedText(Graphics2D g, Font base, float size, Color color, String value, int x, int middle, int maxWidth) {
        Font font = fontFor(base, size, value);
        FontMetrics metrics = g.getFontMetrics(font);
        String fitted = value;
        if (metrics.stringWidth(fitted) > maxWidth) {
            while (!fitted.isEmpty() && metrics.stringWidth(fitted + "…") > maxWidth) fitted = fitted.substring(0, fitted.offsetByCodePoints(fitted.length(), -1));
            fitted += "…";
        }
        g.setFont(font);
        g.setColor(color);
        g.drawString(fitted, x, middle + (metrics.getAscent() - metrics.getDescent()) / 2f);
    }

    private static Font fontFor(Font base, float size, String value) {
        Font font = base.deriveFont(size);
        if (font.canDisplayUpTo(value) == -1) return font;
        return new Font(Font.SANS_SERIF, base == SEMIBOLD ? Font.BOLD : Font.PLAIN, 1).deriveFont(size);
    }

    private static byte[] draw(int height, Consumer<Graphics2D> painter) {
        var image = new BufferedImage(WIDTH * SCALE, height * SCALE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            applyQuality(g);
            g.scale(SCALE, SCALE);
            g.setColor(BACKGROUND);
            g.fillRect(0, 0, WIDTH, height);
            painter.accept(g);
        } finally {
            g.dispose();
        }

        var output = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", output);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return output.toByteArray();
    }

    private static void applyQuality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static Font loadFont(String path) {
        try (InputStream in = QuotaCard.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Missing bundled font " + path);
            return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (IOException | FontFormatException e) {
            throw new IllegalStateException("Could not load bundled font " + path, e);
        }
    }
}
