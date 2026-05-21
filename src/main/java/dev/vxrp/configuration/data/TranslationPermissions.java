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

public record TranslationPermissions(
    @JsonProperty("EMBED_NOT_FOUND_TITLE") String embedNotFoundTitle,
    @JsonProperty("EMBED_NOT_FOUND_BODY") String embedNotFoundBody,
    @JsonProperty("EMBED_COMMAND_DENIED_TITLE") String embedCommandDeniedTitle,
    @JsonProperty("EMBED_COMMAND_DENIED_BODY") String embedCommandDeniedBody,
    @JsonProperty("EMBED_COULD_NOT_SEND_PANEL_TITLE") String embedCouldNotSendPanelTitle,
    @JsonProperty("EMBED_COULD_NOT_SEND_PANEL_BODY") String embedCouldNotSendPanelBody,
    @JsonProperty("EMBED_COULD_NOT_SEND_MODAL_TITLE") String embedCouldNotSendModalTitle,
    @JsonProperty("EMBED_COULD_NOT_SEND_MODAL_BODY") String embedCouldNotSendModalBody,
    @JsonProperty("EMBED_COULD_NOT_SEND_COMMAND_TITLE") String embedCouldNotSendCommandTitle,
    @JsonProperty("EMBED_COULD_NOT_SEND_COMMAND_BODY") String embedCouldNotSendCommandBody,
    @JsonProperty("EMBED_COULD_NOT_SEND_TEMPLATE_TITLE") String embedCouldNotSendTemplateTitle,
    @JsonProperty("EMBED_COULD_NOT_SEND_TEMPLATE_BODY") String embedCouldNotSendTemplateBody,
    @JsonProperty("EMBED_TICKET_DENIED_TITLE") String embedTicketDeniedTitle,
    @JsonProperty("EMBED_TICKET_DENIED_BODY") String embedTicketDeniedBody,
    @JsonProperty("EMBED_NOTICE_OF_DEPARTURE_DENIED_TITLE") String embedNoticeOfDepartureDeniedTitle,
    @JsonProperty("EMBED_NOTICE_OF_DEPARTURE_DENIED_BODY") String embedNoticeOfDepartureDeniedBody,
    @JsonProperty("TEXT_INSUFFICIENT_PERMISSION") String textInsufficientPermission,
    @JsonProperty("TEXT_INTERACTION_DISABLED") String textInteractionDisabled,
    @JsonProperty("MODAL_REASON_TITLE") String modalReasonTitle,
    @JsonProperty("MODAL_REASON_ENTER_REASON_TITLE") String modalReasonEnterReasonTitle,
    @JsonProperty("MODAL_REASON_ENTER_REASON_PLACEHOLDER") String modalReasonEnterReasonPlaceholder
) {}
