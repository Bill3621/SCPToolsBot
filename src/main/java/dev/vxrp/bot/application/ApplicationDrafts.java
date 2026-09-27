package dev.vxrp.bot.application;

import dev.vxrp.bot.application.data.ApplicationAnswer;
import dev.vxrp.bot.ticket.data.ApplicationQuestion;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/** Short-lived first-page answers, bound to the applicant and guild until the final modal is submitted. */
final class ApplicationDrafts {
    private static final Duration LIFETIME = Duration.ofMinutes(15);
    private static final int MAX_DRAFTS = 512;

    private final Supplier<Instant> now;
    private final Map<String, Draft> drafts = new LinkedHashMap<>();

    ApplicationDrafts() {
        this(Instant::now);
    }

    ApplicationDrafts(Supplier<Instant> now) {
        this.now = now;
    }

    synchronized String save(String userId, String guildId, String roleId, List<ApplicationQuestion> questions,
                             List<ApplicationAnswer> answers) {
        prune();
        if (drafts.size() == MAX_DRAFTS) {
            drafts.remove(drafts.keySet().iterator().next());
        }
        String token = UUID.randomUUID().toString();
        drafts.put(token, new Draft(userId, guildId, roleId, List.copyOf(questions), List.copyOf(answers),
                now.get().plus(LIFETIME)));
        return token;
    }

    synchronized Draft find(String token, String userId, String guildId) {
        prune();
        Draft draft = drafts.get(token);
        return draft != null && draft.userId().equals(userId) && draft.guildId().equals(guildId) ? draft : null;
    }

    synchronized boolean consume(String token, Draft draft) {
        prune();
        return drafts.remove(token, draft);
    }

    private void prune() {
        Instant now = this.now.get();
        drafts.values().removeIf(draft -> !draft.expiresAt().isAfter(now));
    }

    record Draft(String userId, String guildId, String roleId, List<ApplicationQuestion> questions,
                 List<ApplicationAnswer> answers, Instant expiresAt) {}
}
