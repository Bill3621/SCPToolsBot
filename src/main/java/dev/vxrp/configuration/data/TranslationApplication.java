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

public record TranslationApplication(
    @JsonProperty("EMBED_ACTIVATION_MENU_TITLE") String embedActivationMenuTitle,
    @JsonProperty("EMBED_ACTIVATION_MENU_BODY") String embedActivationMenuBody,
    @JsonProperty("EMBED_DEACTIVATION_MENU_TITLE") String embedDeactivationMenuTitle,
    @JsonProperty("EMBED_DEACTIVATION_MENU_BODY") String embedDeactivationMenuBody,
    @JsonProperty("EMBED_APPLICATION_MESSAGE_TITLE") String embedApplicationMessageTitle,
    @JsonProperty("EMBED_APPLICATION_MESSAGE_BODY") String embedApplicationMessageBody,
    @JsonProperty("TEXT_ROLE_STATUS_TEMPLATE") String textRoleStatusTemplate,
    @JsonProperty("TEXT_STATUS_ACTIVE") String textStatusActive,
    @JsonProperty("TEXT_STATUS_DEACTIVATED") String textStatusDeactivated,
    @JsonProperty("EMBED_CHOOSE_POSITION_TITLE") String embedChoosePositionTitle,
    @JsonProperty("EMBED_CHOOSE_POSITION_BODY") String embedChoosePositionBody,
    @JsonProperty("MODAL_CHOOSE_COUNT_TITLE") String modalChooseCountTitle,
    @JsonProperty("MODAL_CHOOSE_COUNT_NUMBER_TITLE") String modalChooseCountNumberTitle,
    @JsonProperty("MODAL_CHOOSE_COUNT_NUMBER_PLACEHOLDER") String modalChooseCountNumberPlaceholder,
    @JsonProperty("EMBED_APPLICATION_ACTIVATED_TITLE") String embedApplicationActivatedTitle,
    @JsonProperty("EMBED_APPLICATION_ACTIVATED_BODY") String embedApplicationActivatedBody,
    @JsonProperty("EMBED_APPLICATION_DEACTIVATED_TITLE") String embedApplicationDeactivatedTitle,
    @JsonProperty("EMBED_APPLICATION_DEACTIVATED_BODY") String embedApplicationDeactivatedBody,
    @JsonProperty("EMBED_POSITION_NOT_ACTIVE_TITLE") String embedPositionNotActiveTitle,
    @JsonProperty("EMBED_POSITION_NOT_ACTIVE_BODY") String embedPositionNotActiveBody
) {}
