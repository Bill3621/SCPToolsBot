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

public record TranslationNoticeOfDeparture(
    @JsonProperty("EMBED_TEMPLATE_TITLE") String embedTemplateTitle,
    @JsonProperty("EMBED_TEMPLATE_BODY") String embedTemplateBody,
    @JsonProperty("TEXT_TEMPLATE_RETURN_DATE") String textTemplateReturnDate,
    @JsonProperty("TEXT_TEMPLATE_START_DATE") String textTemplateStartDate,
    @JsonProperty("TEXT_TEMPLATE_REASON") String textTemplateReason,
    @JsonProperty("TEXT_TEMPLATE_FOOTER") String textTemplateFooter,
    @JsonProperty("MODAL_TITLE") String modalTitle,
    @JsonProperty("MODAL_TIME_TITLE") String modalTimeTitle,
    @JsonProperty("MODAL_TIME_PLACEHOLDER") String modalTimePlaceHolder,
    @JsonProperty("MODAL_EXPLANATION_TITLE") String modalExplanationTitle,
    @JsonProperty("MODAL_EXPLANATION_PLACEHOLDER") String modalExplanationPlaceHolder,
    @JsonProperty("EMBED_ENTER_VALID_DATE_TITLE") String embedEnterValidDateTitle,
    @JsonProperty("EMBED_ENTER_VALID_DATE_BODY") String embedEnterValidDateBody,
    @JsonProperty("EMBED_ENTER_FUTURE_DATE_TITLE") String embedEnterFutureDateTitle,
    @JsonProperty("EMBED_ENTER_FUTURE_DATE_BODY") String embedEnterFutureDateBody,
    @JsonProperty("MODAL_START_DATE_TITLE") String modalStartDateTitle,
    @JsonProperty("MODAL_START_DATE_PLACEHOLDER") String modalStartDatePlaceholder,
    @JsonProperty("EMBED_START_DATE_INVALID_TITLE") String embedStartDateInvalidTitle,
    @JsonProperty("EMBED_START_DATE_INVALID_BODY") String embedStartDateInvalidBody,
    @JsonProperty("EMBED_START_DATE_PAST_TITLE") String embedStartDatePastTitle,
    @JsonProperty("EMBED_START_DATE_PAST_BODY") String embedStartDatePastBody,
    @JsonProperty("EMBED_START_DATE_AFTER_END_TITLE") String embedStartDateAfterEndTitle,
    @JsonProperty("EMBED_START_DATE_AFTER_END_BODY") String embedStartDateAfterEndBody,
    @JsonProperty("EMBED_DECISION_SENT_TITLE") String embedDecisionSentTitle,
    @JsonProperty("EMBED_DECISION_SENT_BODY") String embedDecisionSentBody,
    @JsonProperty("MODAL_REASON_ACTION_TITLE") String modalReasonActionTitle,
    @JsonProperty("MODAL_REASON_ACTION_REASON_TITLE") String modalReasonActionReasonTitle,
    @JsonProperty("MODAL_REASON_ACTION_REASON_PLACEHOLDER") String modalReasonActionPlaceholder,
    @JsonProperty("EMBED_NOTICE_TITLE") String embedNoticeTitle,
    @JsonProperty("EMBED_NOTICE_VIEW_TITLE") String embedNoticeViewTitle,
    @JsonProperty("TEXT_NOTICE_VIEW_FILED_BY") String textNoticeViewFiledBy,
    @JsonProperty("TEXT_NOTICE_VIEW_REASON") String textNoticeViewReason,
    @JsonProperty("EMBED_ENDED_TITLE") String embedEndedTitle,
    @JsonProperty("EMBED_ENDED_BODY") String embedEndedBody,
    @JsonProperty("EMBED_REVOKED_TITLE") String embedRevokedTitle,
    @JsonProperty("EMBED_REVOKATION_SENT_TITLE") String embedRevokationSentTitle,
    @JsonProperty("EMBED_REVOKATION_SENT_BODY") String embedRevokationSentBody
) {}
