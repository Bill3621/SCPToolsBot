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

package dev.vxrp.configuration.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TranslationStaffQuota(
    @JsonProperty("EMBED_REPORT_TITLE") String embedReportTitle,
    @JsonProperty("TEXT_REPORT_MISSED") String textReportMissed,
    @JsonProperty("TEXT_REPORT_EMPTY") String textReportEmpty,
    @JsonProperty("TEXT_PROGRESS") String textProgress,
    @JsonProperty("TEXT_CARD_WEEK") String textCardWeek,
    @JsonProperty("TEXT_CARD_SUMMARY") String textCardSummary,
    @JsonProperty("TEXT_CARD_NOT_COUNTED") String textCardNotCounted,
    @JsonProperty("TEXT_CARD_ON_LEAVE") String textCardOnLeave,
    @JsonProperty("TEXT_CARD_NOT_LINKED") String textCardNotLinked,
    @JsonProperty("TEXT_CARD_LOOKUP_FAILED") String textCardLookupFailed,
    @JsonProperty("TEXT_CARD_TO_GO") String textCardToGo,
    @JsonProperty("TEXT_CARD_SHORT") String textCardShort,
    @JsonProperty("TEXT_CARD_MET") String textCardMet,
    @JsonProperty("TEXT_CARD_DAYS_LEFT") String textCardDaysLeft,
    @JsonProperty("EMBED_MISSED_DM_TITLE") String embedMissedDmTitle,
    @JsonProperty("EMBED_VIEW_TITLE") String embedViewTitle,
    @JsonProperty("EMBED_VIEW_ON_LEAVE_BODY") String embedViewOnLeaveBody,
    @JsonProperty("EMBED_ERROR_TITLE") String embedErrorTitle,
    @JsonProperty("EMBED_NOT_STAFF_BODY") String embedNotStaffBody,
    @JsonProperty("EMBED_NOT_LINKED_BODY") String embedNotLinkedBody,
    @JsonProperty("EMBED_LOOKUP_FAILED_BODY") String embedLookupFailedBody,
    @JsonProperty("EMBED_NO_PERMISSION_BODY") String embedNoPermissionBody
) {
    public TranslationStaffQuota {
        if (embedReportTitle == null) embedReportTitle = DEFAULT.embedReportTitle;
        if (textReportMissed == null) textReportMissed = DEFAULT.textReportMissed;
        if (textReportEmpty == null) textReportEmpty = DEFAULT.textReportEmpty;
        if (textProgress == null) textProgress = DEFAULT.textProgress;
        if (textCardWeek == null) textCardWeek = DEFAULT.textCardWeek;
        if (textCardSummary == null) textCardSummary = DEFAULT.textCardSummary;
        if (textCardNotCounted == null) textCardNotCounted = DEFAULT.textCardNotCounted;
        if (textCardOnLeave == null) textCardOnLeave = DEFAULT.textCardOnLeave;
        if (textCardNotLinked == null) textCardNotLinked = DEFAULT.textCardNotLinked;
        if (textCardLookupFailed == null) textCardLookupFailed = DEFAULT.textCardLookupFailed;
        if (textCardToGo == null) textCardToGo = DEFAULT.textCardToGo;
        if (textCardShort == null) textCardShort = DEFAULT.textCardShort;
        if (textCardMet == null) textCardMet = DEFAULT.textCardMet;
        if (textCardDaysLeft == null) textCardDaysLeft = DEFAULT.textCardDaysLeft;
        if (embedMissedDmTitle == null) embedMissedDmTitle = DEFAULT.embedMissedDmTitle;
        if (embedViewTitle == null) embedViewTitle = DEFAULT.embedViewTitle;
        if (embedViewOnLeaveBody == null) embedViewOnLeaveBody = DEFAULT.embedViewOnLeaveBody;
        if (embedErrorTitle == null) embedErrorTitle = DEFAULT.embedErrorTitle;
        if (embedNotStaffBody == null) embedNotStaffBody = DEFAULT.embedNotStaffBody;
        if (embedNotLinkedBody == null) embedNotLinkedBody = DEFAULT.embedNotLinkedBody;
        if (embedLookupFailedBody == null) embedLookupFailedBody = DEFAULT.embedLookupFailedBody;
        if (embedNoPermissionBody == null) embedNoPermissionBody = DEFAULT.embedNoPermissionBody;
    }

    public static final TranslationStaffQuota DEFAULT = new TranslationStaffQuota(
            "Staff Quota · Week of %week%",
            "**Missed**",
            "No members hold a staff role with a quota.",
            "%hours% / %required%h",
            "Week of %week%",
            "%met% of %total% met",
            "Not counted",
            "on leave",
            "not linked",
            "lookup failed",
            "%hours%h to go",
            "%hours%h short",
            "Quota met",
            "%days% days left",
            "Staff Quota Missed",
            "Staff Quota · %user%",
            "Week of %week%\nOn leave for the whole week.",
            "Staff Quota",
            "%user% has no staff role with a quota.",
            "%user% has no Discord account linked in CedMod.",
            "Could not reach CedMod. Try again later.",
            "You can only view your own quota."
    );
}
