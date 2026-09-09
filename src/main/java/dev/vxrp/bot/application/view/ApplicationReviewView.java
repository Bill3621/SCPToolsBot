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
import dev.vxrp.bot.application.data.ApplicationRecord;
import dev.vxrp.bot.application.data.ApplicationTally;
import dev.vxrp.bot.application.data.ApplicationVote;
import dev.vxrp.bot.application.data.ApplicationVoteEvent;
import dev.vxrp.bot.application.enums.ApplicationDecision;
import dev.vxrp.bot.application.enums.ApplicationLifecycle;
import dev.vxrp.bot.application.enums.ApplicationQueueFilter;
import dev.vxrp.bot.application.enums.ApplicationVoteAction;
import dev.vxrp.bot.application.enums.ApplicationVoteChoice;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Components V2 renderers for the staff application review workflow. */
public final class ApplicationReviewView {
    public static final int DEFAULT_PAGE_SIZE = 8;
    // Seven entries leave room for the container, separators, filter, pagination, and refresh controls.
    private static final int MAX_QUEUE_ENTRIES = 7;
    private static final int HISTORY_PAGE_SIZE = 8;

    private ApplicationReviewView() {}

    public record QueueEntry(ApplicationRecord application, String roleName, String applicantName,
                             ApplicationTally tally) {
        public QueueEntry {
            if (application == null) {
                throw new IllegalArgumentException("application cannot be null");
            }
            roleName = ApplicationView.text(roleName, "Application");
            applicantName = ApplicationView.text(applicantName, application.issuer());
            tally = tally == null ? new ApplicationTally(0, 0, 0, application.quorumRequired()) : tally;
        }
    }

    public static MessageCreateData openReview(ApplicationRecord application, String roleName,
                                                List<ApplicationAnswer> answers, ApplicationTally tally,
                                                ApplicationVote currentVote, String ticketUrl) {
        requireApplication(application);
        ApplicationTally effectiveTally = effectiveTally(application, tally);
        List<ContainerChildComponent> content = reviewHeader(application, roleName, "Open review");
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        ApplicationView.appendAnswers(content, answers);
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(TextDisplay.of(tallyText(effectiveTally, currentVote)));
        content.add(ActionRow.of(voteMenu(application.id())));
        content.add(ActionRow.of(openActions(application.id(), ticketUrl)));
        return ApplicationView.message(content, null);
    }

    public static MessageCreateData renderOpenReview(ApplicationRecord application, String roleName,
                                                      List<ApplicationAnswer> answers, ApplicationTally tally,
                                                      ApplicationVote currentVote, String ticketUrl) {
        return openReview(application, roleName, answers, tally, currentVote, ticketUrl);
    }

    public static MessageEditData editOpenReview(ApplicationRecord application, String roleName,
                                                  List<ApplicationAnswer> answers, ApplicationTally tally,
                                                  ApplicationVote currentVote, String ticketUrl) {
        return MessageEditBuilder.fromCreateData(openReview(application, roleName, answers, tally, currentVote, ticketUrl)).build();
    }

    public static MessageCreateData lockedReview(ApplicationRecord application, String roleName,
                                                  List<ApplicationAnswer> answers, ApplicationTally tally,
                                                  List<ApplicationVoteEvent> history, String ticketUrl) {
        requireApplication(application);
        ApplicationTally effectiveTally = effectiveTally(application, tally);
        List<ContainerChildComponent> content = reviewHeader(application, roleName, "Locked review");
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        ApplicationView.appendAnswers(content, answers);
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(TextDisplay.of("### Decision\n" + decisionText(application.decision())));
        content.add(TextDisplay.of("**Vote tally**\n" + tallyText(effectiveTally, null)));
        content.add(TextDisplay.of("**Closed by**\n" + ApplicationView.displayActor(application.closedBy())
                + " · " + ApplicationView.timestamp(application.closedAt())));
        content.add(TextDisplay.of("**Close reason**\n" + ApplicationView.quote(application.closeReason())));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(TextDisplay.of(historySummary(history)));
        content.add(ActionRow.of(lockedActions(application.id(), ticketUrl)));
        return ApplicationView.message(content, application.decision() == ApplicationDecision.DENIED ? 0xE74D3C : null);
    }

    public static MessageCreateData renderLockedReview(ApplicationRecord application, String roleName,
                                                        List<ApplicationAnswer> answers, ApplicationTally tally,
                                                        List<ApplicationVoteEvent> history, String ticketUrl) {
        return lockedReview(application, roleName, answers, tally, history, ticketUrl);
    }

    public static MessageEditData editLockedReview(ApplicationRecord application, String roleName,
                                                    List<ApplicationAnswer> answers, ApplicationTally tally,
                                                    List<ApplicationVoteEvent> history, String ticketUrl) {
        return MessageEditBuilder.fromCreateData(lockedReview(application, roleName, answers, tally, history, ticketUrl)).build();
    }

    public static MessageCreateData queue(String requesterId, ApplicationQueueFilter filter, int page,
                                          int pageSize, List<QueueEntry> entries, long total) {
        ApplicationQueueFilter effectiveFilter = filter == null ? ApplicationQueueFilter.OPEN : filter;
        int effectivePageSize = Math.max(1, Math.min(DEFAULT_PAGE_SIZE, pageSize));
        int effectivePage = Math.max(0, page);
        long effectiveTotal = Math.max(0, total);
        int pageCount = Math.max(1, (int) Math.ceil((double) effectiveTotal / effectivePageSize));
        if (effectivePage >= pageCount) {
            effectivePage = pageCount - 1;
        }

        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of("## Application queue\n" + effectiveTotal + " matching applications · page "
                + (effectivePage + 1) + "/" + pageCount));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));

        List<QueueEntry> safeEntries = entries == null ? List.of() : entries;
        int rendered = 0;
        for (QueueEntry entry : safeEntries) {
            if (entry == null || rendered >= MAX_QUEUE_ENTRIES) {
                break;
            }
            content.add(queueEntry(entry));
            rendered++;
        }
        if (rendered == 0) {
            content.add(TextDisplay.of("No applications match this filter. Try another filter or refresh the queue."));
        } else if (safeEntries.size() > rendered || effectiveTotal > (long) effectivePage * effectivePageSize + rendered) {
            long omitted = Math.max(0, effectiveTotal - ((long) effectivePage * effectivePageSize + rendered));
            if (omitted > 0) {
                content.add(TextDisplay.of("-# " + omitted + " more application(s) are available on another page."));
            }
        }

        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(ActionRow.of(filterMenu(requesterId, effectiveFilter, effectivePage)));
        List<net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent> navigation = new ArrayList<>();
        navigation.addAll(List.of(pageButtons(requesterId, effectiveFilter, effectivePage, pageCount)));
        navigation.add(Button.secondary(refreshId(requesterId, effectiveFilter, effectivePage), "Refresh"));
        content.add(ActionRow.of(navigation));
        return ApplicationView.message(content, null);
    }

    public static MessageCreateData renderQueue(String requesterId, ApplicationQueueFilter filter, int page,
                                                 int pageSize, List<QueueEntry> entries, long total) {
        return queue(requesterId, filter, page, pageSize, entries, total);
    }

    public static MessageCreateData closeConfirmation(ApplicationRecord application, ApplicationTally tally,
                                                       ApplicationDecision suggestedDecision) {
        requireApplication(application);
        ApplicationTally effectiveTally = effectiveTally(application, tally);
        String decision = decisionText(suggestedDecision == null ? recommendedDecision(effectiveTally) : suggestedDecision);
        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of("## Close application `" + application.id() + "`?\n"
                + "This locks all votes and records the staff decision."));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        content.add(TextDisplay.of("**Suggested result**\n" + decision));
        content.add(TextDisplay.of(tallyText(effectiveTally, null)));
        content.add(TextDisplay.of("-# Confirm only after the application has been reviewed. A tie has no automatic result."));
        content.add(ActionRow.of(
                Button.success("application_review_close_decision:" + application.id() + ":APPROVED", "Approve"),
                Button.danger("application_review_close_decision:" + application.id() + ":DENIED", "Deny"),
                Button.secondary("application_review_close_cancel:" + application.id(), "Keep open")));
        return ApplicationView.message(content, null);
    }

    public static MessageCreateData renderCloseConfirmation(ApplicationRecord application, ApplicationTally tally,
                                                            ApplicationDecision suggestedDecision) {
        return closeConfirmation(application, tally, suggestedDecision);
    }

    public static MessageCreateData history(String applicationId, int page, int pageSize,
                                            List<ApplicationVoteEvent> events) {
        String id = ApplicationView.text(applicationId, "unknown");
        List<ApplicationVoteEvent> safeEvents = events == null ? List.of() : events.stream()
                .filter(event -> event != null)
                .sorted(Comparator.comparing(ApplicationVoteEvent::createdAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        int effectiveSize = Math.max(1, Math.min(HISTORY_PAGE_SIZE, pageSize));
        int pageCount = Math.max(1, (int) Math.ceil((double) safeEvents.size() / effectiveSize));
        int effectivePage = Math.max(0, Math.min(page, pageCount - 1));
        int start = effectivePage * effectiveSize;
        int end = Math.min(start + effectiveSize, safeEvents.size());

        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of("## Vote history\nApplication `" + id + "` · page "
                + (effectivePage + 1) + "/" + pageCount));
        content.add(Separator.createDivider(Separator.Spacing.SMALL));
        if (start == end) {
            content.add(TextDisplay.of("No vote history has been recorded."));
        } else {
            for (int index = start; index < end; index++) {
                ApplicationVoteEvent event = safeEvents.get(index);
                content.add(TextDisplay.of(historyLine(event)));
            }
        }
        List<net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent> navigation = new ArrayList<>();
        if (effectivePage > 0) {
            navigation.add(Button.secondary(historyId(id, effectivePage - 1), "Previous"));
        }
        if (effectivePage + 1 < pageCount) {
            navigation.add(Button.secondary(historyId(id, effectivePage + 1), "Next"));
        }
        if (!navigation.isEmpty()) {
            content.add(Separator.createDivider(Separator.Spacing.SMALL));
            content.add(ActionRow.of(navigation));
        }
        return ApplicationView.message(content, null);
    }

    public static MessageCreateData renderHistory(String applicationId, int page, int pageSize,
                                                   List<ApplicationVoteEvent> events) {
        return history(applicationId, page, pageSize, events);
    }

    public static MessageCreateData applicantResult(String title, String body, String ticketUrl,
                                                    ApplicationDecision decision) {
        return ApplicationView.applicantResult(ApplicationView.text(title, "Application result"),
                ApplicationView.text(body, decision == ApplicationDecision.APPROVED ? "Your application was approved." : "Your application was denied."),
                ticketUrl);
    }

    public static MessageCreateData feedback(String title, String body, boolean success) {
        return ApplicationView.feedback(title, body, success);
    }

    public static String filterId(String requesterId, ApplicationQueueFilter filter, int page) {
        return "applications_filter:" + ApplicationView.text(requesterId, "unknown") + ":"
                + effectiveFilter(filter) + ":" + Math.max(0, page);
    }

    public static String pageId(String requesterId, ApplicationQueueFilter filter, int page) {
        return "applications_page:" + ApplicationView.text(requesterId, "unknown") + ":"
                + effectiveFilter(filter) + ":" + Math.max(0, page);
    }

    public static String refreshId(String requesterId, ApplicationQueueFilter filter, int page) {
        return "applications_refresh:" + ApplicationView.text(requesterId, "unknown") + ":"
                + effectiveFilter(filter) + ":" + Math.max(0, page);
    }

    public static String voteId(String applicationId) {
        return "application_review_vote:" + ApplicationView.text(applicationId, "unknown");
    }

    public static String closeId(String applicationId) {
        return "application_review_close_prompt:" + ApplicationView.text(applicationId, "unknown");
    }

    public static String historyId(String applicationId, int page) {
        return "application_review_history:" + ApplicationView.text(applicationId, "unknown") + ":" + Math.max(0, page);
    }

    private static List<ContainerChildComponent> reviewHeader(ApplicationRecord application, String roleName,
                                                               String state) {
        List<ContainerChildComponent> content = new ArrayList<>();
        content.add(TextDisplay.of("## Application `" + application.id() + "` · " + ApplicationView.text(roleName, "Position")
                + "\n" + ApplicationView.displayActor(application.issuer()) + " · " + state));
        content.add(TextDisplay.of("Submitted " + ApplicationView.timestamp(application.createdAt())));
        return content;
    }

    private static String tallyText(ApplicationTally tally, ApplicationVote currentVote) {
        String vote = currentVote == null ? "not cast" : currentVote.choice().name();
        return "### Review tally\n"
                + "**Approve** `" + tally.approve() + "` · **Deny** `" + tally.deny() + "` · **Abstain** `" + tally.abstain() + "`\n"
                + "Quorum: `" + tally.counted() + "/" + tally.quorumRequired() + "` counted · your vote: **" + vote + "**";
    }

    private static String historySummary(List<ApplicationVoteEvent> events) {
        if (events == null || events.isEmpty()) {
            return "### Vote history\nNo vote changes have been recorded.";
        }
        StringBuilder result = new StringBuilder("### Vote history\n");
        int count = 0;
        for (ApplicationVoteEvent event : events) {
            if (event == null || count >= 5) {
                break;
            }
            if (count > 0) {
                result.append('\n');
            }
            result.append(historyLine(event));
            count++;
        }
        if (events.size() > count) {
            result.append("\n-# ").append(events.size() - count).append(" more event(s) in the full history.");
        }
        return result.toString();
    }

    private static String historyLine(ApplicationVoteEvent event) {
        String action = event.action() == null ? "UPDATED" : event.action().name();
        String choice = event.choice() == null ? "" : " → " + event.choice().name();
        return "**" + action + choice + "** · " + ApplicationView.displayActor(event.voterId())
                + " · " + ApplicationView.timestamp(event.createdAt());
    }

    private static String decisionText(ApplicationDecision decision) {
        if (decision == null) {
            return "No decision recorded";
        }
        return decision == ApplicationDecision.APPROVED ? "Approved" : "Denied";
    }

    private static ApplicationDecision recommendedDecision(ApplicationTally tally) {
        Boolean recommendation = tally.recommendation();
        return recommendation == null ? null : recommendation ? ApplicationDecision.APPROVED : ApplicationDecision.DENIED;
    }

    private static ApplicationTally effectiveTally(ApplicationRecord application, ApplicationTally tally) {
        return tally == null ? new ApplicationTally(0, 0, 0, application.quorumRequired()) : tally;
    }

    private static String effectiveFilter(ApplicationQueueFilter filter) {
        return (filter == null ? ApplicationQueueFilter.OPEN : filter).name();
    }

    private static ContainerChildComponent queueEntry(QueueEntry entry) {
        ApplicationRecord application = entry.application();
        ApplicationTally tally = entry.tally();
        String status = application.lifecycle() == null ? "Unknown" : application.lifecycle().name();
        String summary = "**" + ApplicationView.text(entry.roleName(), "Application") + "** · "
                + ApplicationView.displayActor(entry.applicantName()) + "\n"
                + status + " · " + tally.approve() + " approve · " + tally.deny() + " deny · "
                + tally.abstain() + " abstain · submitted " + ApplicationView.timestamp(application.createdAt());
        return Section.of(Button.primary("application_review:" + application.id(), "Review"), TextDisplay.of(summary));
    }

    private static StringSelectMenu filterMenu(String requesterId, ApplicationQueueFilter filter, int page) {
        StringSelectMenu.Builder builder = StringSelectMenu.create(filterId(requesterId, filter, page))
                .setPlaceholder("Choose an application filter");
        for (ApplicationQueueFilter value : ApplicationQueueFilter.values()) {
            String label = switch (value) {
                case OPEN -> "Open";
                case READY -> "Ready for decision";
                case CLOSED -> "Closed";
                case ALL -> "All applications";
            };
            String description = switch (value) {
                case OPEN -> "Applications still accepting votes";
                case READY -> "Open applications at quorum";
                case CLOSED -> "Locked application results";
                case ALL -> "Every application record";
            };
            builder.addOptions(SelectOption.of(label, value.name()).withDescription(description)
                    .withDefault(value == (filter == null ? ApplicationQueueFilter.OPEN : filter)));
        }
        return builder.build();
    }

    private static Button[] pageButtons(String requesterId, ApplicationQueueFilter filter, int page, int pageCount) {
        List<Button> buttons = new ArrayList<>();
        if (page > 0) {
            buttons.add(Button.secondary(pageId(requesterId, filter, page - 1), "Previous"));
        }
        if (page + 1 < pageCount) {
            buttons.add(Button.secondary(pageId(requesterId, filter, page + 1), "Next"));
        }
        return buttons.toArray(Button[]::new);
    }

    private static List<net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent> openActions(String id,
                                                                                                      String ticketUrl) {
        List<net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent> actions = new ArrayList<>();
        if (ticketUrl != null && ticketUrl.startsWith("http")) {
            actions.add(Button.link(ticketUrl, "Open ticket"));
        }
        actions.add(Button.danger(closeId(id), "Close review"));
        actions.add(Button.secondary(historyId(id, 0), "Vote history"));
        return actions;
    }

    private static List<net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent> lockedActions(String id,
                                                                                                        String ticketUrl) {
        List<net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent> actions = new ArrayList<>();
        if (ticketUrl != null && ticketUrl.startsWith("http")) {
            actions.add(Button.link(ticketUrl, "Open ticket"));
        }
        actions.add(Button.secondary(historyId(id, 0), "Vote history"));
        return actions;
    }

    private static StringSelectMenu voteMenu(String id) {
        return StringSelectMenu.create(voteId(id))
                .setPlaceholder("Cast or change your vote")
                .setRequiredRange(1, 1)
                .addOption("Approve", ApplicationVoteChoice.APPROVE.name(), "Count toward approval", Emoji.fromFormatted("✅"))
                .addOption("Deny", ApplicationVoteChoice.DENY.name(), "Count toward rejection", Emoji.fromFormatted("❌"))
                .addOption("Abstain", ApplicationVoteChoice.ABSTAIN.name(), "Record participation without deciding", Emoji.fromFormatted("➖"))
                .addOption("Remove my vote", "REMOVE", "Remove your current vote", Emoji.fromFormatted("↩️"))
                .build();
    }

    private static void requireApplication(ApplicationRecord application) {
        if (application == null || application.id() == null || application.id().isBlank()) {
            throw new IllegalArgumentException("application with an id is required");
        }
    }
}
