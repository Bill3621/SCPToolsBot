/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content is
 * prohibited. If you did not receive a copy of the license with this file, you may obtain a copy
 * of the license at
 *
 * https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software is
 * provided without any sort of WARRANTY, and the authors cannot be held liable for any form of
 * claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.bot.application.view;

import dev.vxrp.bot.application.data.ApplicationAnswer;
import dev.vxrp.bot.application.data.ApplicationType;
import dev.vxrp.configuration.data.Translation;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Shared Components V2 builders for the application workflow. */
public final class ApplicationView {
    private ApplicationView() {}

    public static MessageCreateData activationMenu(String title, String body, String status,
                                                    Collection<ApplicationType> types, String userId,
                                                    Translation translation) {
        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of("## " + text(title, "Application setup") + "\n" + text(body, "Configure the application positions before publishing the panel.")));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(TextDisplay.of("**Current status**\n" + text(status, "Deactivated")));
        content.add(TextDisplay.of("### Positions\n" + roleStatusText(types)));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(TextDisplay.of("-# Add or remove positions, then complete setup to publish the application panel."));
        content.add(ActionRow.of(activationButtons(userId, types != null && !types.isEmpty(), translation)));
        return message(content, null);
    }

    public static MessageCreateData deactivationMenu(String title, String body, Translation translation) {
        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of("## " + text(title, "Deactivate applications?") + "\n" + text(body, "This will stop new applications from being created.")));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(TextDisplay.of("Existing application records remain available for review."));
        content.add(ActionRow.of(Button.danger("application_deactivate", text(translation.buttons().textApplicationDeactivate(), "Deactivate applications"))));
        return message(content, 0xE74D3C);
    }

    public static MessageCreateData publicPanel(String title, String body, Collection<ApplicationType> types,
                                                Map<String, Long> approvedCounts, boolean active, Translation translation) {
        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of("## " + text(title, "Applications") + "\n" + text(body, "Choose a position to apply.")));
        List<ApplicationType> positions = types == null ? List.of() : types.stream()
                .filter(type -> type != null && type.roleId() != null && !type.roleId().isBlank())
                .sorted(Comparator.comparingInt(ApplicationType::pos)).toList();
        Map<String, Long> accepted = approvedCounts == null ? Map.of() : approvedCounts;
        if (positions.isEmpty()) {
            content.add(TextDisplay.of("No positions configured."));
        } else if (positions.size() > 12) {
            StringBuilder summary = new StringBuilder("### Positions");
            for (ApplicationType type : positions) {
                long acceptedCount = count(accepted, type.roleId());
                summary.append("\n").append(isOpen(type, acceptedCount, active) ? "🟢 " : "⚫ ")
                        .append("**").append(text(type.name(), type.roleId())).append("** · ")
                        .append(availableText(type, acceptedCount, active, translation));
            }
            content.add(Separator.createDivider(Separator.Spacing.SMALL));
            content.add(TextDisplay.of(summary.toString()));
            if (active && positions.stream().anyMatch(type -> isOpen(type,
                    count(accepted, type.roleId()), active))) {
                content.add(ActionRow.of(Button.success("application_open",
                        text(translation.buttons().textApplicationOpenTickets(), "Open application"))));
            }
        } else {
            if (positions.size() > 9) {
                content.add(Separator.createDivider(Separator.Spacing.SMALL));
            }
            for (ApplicationType type : positions) {
                if (positions.size() <= 9) {
                    content.add(Separator.createDivider(Separator.Spacing.SMALL));
                }
                long acceptedCount = count(accepted, type.roleId());
                boolean open = isOpen(type, acceptedCount, active);
                String description = text(type.description(), "Application position").replace("\n", "\n> ");
                String summary = "**" + (open ? "🟢 " : "⚫ ") + text(type.name(), type.roleId())
                        + "**\n> " + description + "\n" + availableText(type, acceptedCount, active, translation);
                Button button = Button.success("application_apply:" + type.roleId(),
                        text(translation.application().textApply(), "Apply"));
                if (!open) {
                    button = Button.secondary("application_apply:" + type.roleId(),
                            text(translation.application().textClosed(), "Closed")).asDisabled();
                }
                content.add(Section.of(button, TextDisplay.of(summary)));
            }
        }
        return message(content, null);
    }

    public static MessageCreateData selectionPrompt(String title, String body, String menuId,
                                                    String placeholder, Collection<ApplicationType> types) {
        StringSelectMenu.Builder menu = StringSelectMenu.create(menuId)
                .setPlaceholder(text(placeholder, "Choose an application position"));
        if (types != null) {
            for (ApplicationType type : types) {
                if (type == null || type.roleId() == null || type.roleId().isBlank()) {
                    continue;
                }
                String label = text(type.name(), type.roleId());
                String description = text(type.description(), "Application position");
                if (type.emoji() == null || type.emoji().isBlank()) {
                    menu.addOption(label, type.roleId(), description);
                } else {
                    menu.addOption(label, type.roleId(), description, Emoji.fromFormatted(type.emoji()));
                }
            }
        }
        return selectionPrompt(title, body, menu.build());
    }

    public static MessageCreateData selectionPrompt(String title, String body, StringSelectMenu menu) {
        List<ContainerChildComponent> content = List.of(
                TextDisplay.of("## " + text(title, "Choose an option") + "\n" + text(body, "Select an application position to continue.")),
                Separator.createDivider(Separator.Spacing.SMALL),
                ActionRow.of(menu));
        return message(content, null);
    }

    public static MessageCreateData feedback(String title, String body, boolean success) {
        return message(List.of(TextDisplay.of("### " + text(title, success ? "Done" : "Unable to continue")
                + "\n" + text(body, "Please try again."))), success ? 0x2ECC70 : 0xE74D3C);
    }

    public static MessageCreateData applicantResult(String title, String body, String ticketUrl) {
        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of("## " + text(title, "Application result") + "\n" + text(body, "Your application has finished review.")));
        if (ticketUrl != null && ticketUrl.startsWith("http")) {
            content.add(Separator.createDivider(Separator.Spacing.SMALL));
            content.add(ActionRow.of(Button.link(ticketUrl, "Open application ticket")));
        }
        return message(content, null);
    }

    public static MessageCreateData applicationThreadRoot(String title, String applicant,
                                                           String roleName, List<ApplicationAnswer> answers) {
        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of("## " + text(title, "Application") + "\n"
                + displayActor(applicant) + " · " + text(roleName, "Position")));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        appendAnswers(content, answers);
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(TextDisplay.of("Staff voting and final decisions are handled from the canonical review message."));
        return message(content, null);
    }

    public static String ticketUrl(String guildId, String ticketChannelId) {
        if (guildId == null || guildId.isBlank() || ticketChannelId == null || ticketChannelId.isBlank()) {
            return null;
        }
        return "https://discord.com/channels/" + guildId + "/" + ticketChannelId;
    }

    static MessageCreateData message(Collection<? extends ContainerChildComponent> content, Integer accentColor) {
        Container container = Container.of(content);
        if (accentColor != null) {
            container = container.withAccentColor(accentColor);
        }
        return new MessageCreateBuilder()
                .useComponentsV2()
                .setAllowedMentions(List.of())
                .setComponents(List.of(container))
                .build();
    }

    static String timestamp(Instant instant) {
        return instant == null ? "unknown time" : "<t:" + instant.getEpochSecond() + ":R>";
    }

    static String displayActor(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown user";
        }
        return value.matches("\\d+") ? "<@" + value + ">" : value;
    }

    static String text(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.strip();
    }

    static String fieldLabel(String value) {
        String label = text(value, "Answer").replace('_', ' ').replace('-', ' ');
        return Character.toUpperCase(label.charAt(0)) + label.substring(1);
    }

    static String quote(String value) {
        String answer = text(value, "No answer provided.");
        if (answer.length() > 3900) {
            answer = answer.substring(0, 3897) + "...";
        }
        return "> " + answer.replace("\n", "\n> ");
    }

    static void appendAnswers(List<ContainerChildComponent> content, List<ApplicationAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            content.add(TextDisplay.of("No answers were recorded."));
            return;
        }
        for (ApplicationAnswer answer : answers) {
            if (answer == null) {
                continue;
            }
            content.add(TextDisplay.of("**" + fieldLabel(answer.fieldKey()) + "**\n" + quote(answer.value())));
        }
    }

    private static List<ActionRowChildComponent> activationButtons(String userId, boolean hasTypes,
                                                                    Translation translation) {
        String id = text(userId, "unknown");
        Button add = Button.success("application_activation_add:" + id,
                text(translation.buttons().textApplicationActivationAdd(), "Add position"));
        Button remove = Button.danger("application_activation_remove:" + id,
                text(translation.buttons().textApplicationActivationRemove(), "Remove position"));
        Button complete = Button.primary("application_activation_complete_setup:" + id,
                text(translation.buttons().textApplicationActivationCompleteSetup(), "Complete setup"));
        if (!hasTypes) {
            remove = remove.asDisabled();
            complete = complete.asDisabled();
        }
        return List.of(add, remove, complete);
    }

    private static long count(Map<String, Long> counts, String roleId) {
        return Math.max(0, counts.getOrDefault(roleId, 0L));
    }

    private static boolean isOpen(ApplicationType type, long acceptedCount, boolean active) {
        return active && type.state() && acceptedCount < type.member();
    }

    private static String availableText(ApplicationType type, long acceptedCount, boolean active, Translation translation) {
        if (active && type.state() && type.member() == Integer.MAX_VALUE) {
            return text(translation.application().textUnlimitedAvailability(), "No limit");
        }
        long available = active && type.state() ? Math.max(0, type.member() - acceptedCount) : 0;
        return available + " " + text(translation.application().textAvailable(), "available");
    }

    private static String roleStatusText(Collection<ApplicationType> types) {
        if (types == null || types.isEmpty()) {
            return "No positions configured.";
        }
        StringBuilder result = new StringBuilder();
        for (ApplicationType type : types) {
            if (type == null) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append('\n');
            }
            String name = text(type.name(), type.roleId());
            String status = type.state() ? "Active" : "Inactive";
            result.append("**").append(name).append("** · ").append(status)
                    .append(" · candidate limit: ").append(Math.max(0, type.member()));
        }
        return result.isEmpty() ? "No positions configured." : result.toString();
    }
}
