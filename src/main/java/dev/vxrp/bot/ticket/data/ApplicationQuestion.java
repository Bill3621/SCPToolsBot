package dev.vxrp.bot.ticket.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.modals.Modal;

import java.util.List;

/** A configurable question. Its label is saved with the answer for historical reviews. */
public record ApplicationQuestion(
        String label,
        String placeholder,
        TextInputStyle style,
        @JsonProperty("min_length") int minLength,
        @JsonProperty("max_length") int maxLength,
        boolean numeric
) {
    public ApplicationQuestion {
        if (label == null || label.isBlank() || label.length() > 45) {
            throw new IllegalArgumentException("Application question label must be 1 to 45 characters");
        }
        if (placeholder == null || placeholder.isBlank() || placeholder.length() > 100) {
            throw new IllegalArgumentException("Application question placeholder must be 1 to 100 characters");
        }
        if (style == null || (style != TextInputStyle.SHORT && style != TextInputStyle.PARAGRAPH)) {
            throw new IllegalArgumentException("Application question style must be SHORT or PARAGRAPH");
        }
        if (minLength < 1 || maxLength < minLength || maxLength > 4000) {
            throw new IllegalArgumentException("Application question length must be between 1 and 4000");
        }
    }

    public static void validate(List<ApplicationQuestion> questions, String name) {
        if (questions.isEmpty() || questions.size() > 2 * Modal.MAX_COMPONENTS) {
            throw new IllegalArgumentException(name + " must contain 1 to " + (2 * Modal.MAX_COMPONENTS) + " questions");
        }
        if (questions.stream().map(ApplicationQuestion::label).distinct().count() != questions.size()) {
            throw new IllegalArgumentException(name + " must have unique labels");
        }
    }
}
