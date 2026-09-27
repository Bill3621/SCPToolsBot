package dev.vxrp.bot.application;

import dev.vxrp.bot.application.data.ApplicationAnswer;
import dev.vxrp.bot.ticket.data.ApplicationQuestion;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationDraftsTest {
    @Test
    void draftIsBoundToItsOwnerAndCanOnlyBeConsumedOnce() {
        var drafts = new ApplicationDrafts();
        var questions = List.of(new ApplicationQuestion("Name", "Your name", TextInputStyle.SHORT, 1, 100, false));
        String token = drafts.save("user", "guild", "role", questions, List.of(new ApplicationAnswer("Name", "Ada")));

        assertNull(drafts.find(token, "someone-else", "guild"));
        assertNull(drafts.find(token, "user", "other-guild"));
        var draft = drafts.find(token, "user", "guild");
        assertNotNull(draft);
        assertEquals("role", draft.roleId());
        assertEquals("Ada", draft.answers().getFirst().value());
        assertTrue(drafts.consume(token, draft));
        assertFalse(drafts.consume(token, draft));
        assertNull(drafts.find(token, "user", "guild"));
    }

    @Test
    void unfinishedApplicationExpiresAfterFifteenMinutes() {
        var now = new AtomicReference<>(Instant.parse("2026-09-27T12:00:00Z"));
        var drafts = new ApplicationDrafts(now::get);
        String token = drafts.save("user", "guild", "role", List.of(), List.of());

        now.set(now.get().plusSeconds(14 * 60));
        assertNotNull(drafts.find(token, "user", "guild"));
        now.set(now.get().plusSeconds(60));
        assertNull(drafts.find(token, "user", "guild"));
    }
}
