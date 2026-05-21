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

public record TranslationHelp(
    @JsonProperty("EMBED_PAGE_ONE_TITLE") String embedPageOneTitle,
    @JsonProperty("EMBED_PAGE_ONE_BODY") String embedPageOneBody,
    @JsonProperty("EMBED_PAGE_TWO_TITLE") String embedPageTwoTitle,
    @JsonProperty("EMBED_PAGE_TWO_BODY") String embedPageTwoBody,
    @JsonProperty("EMBED_PAGE_THREE_TITLE") String embedPageThreeTitle,
    @JsonProperty("EMBED_PAGE_THREE_BODY") String embedPageThreeBody,
    @JsonProperty("EMBED_PAGE_FOUR_TITLE") String embedPageFourTitle,
    @JsonProperty("EMBED_PAGE_FOUR_BODY") String embedPageFourBody,
    @JsonProperty("EMBED_PAGE_FIVE_TITLE") String embedPageFiveTitle,
    @JsonProperty("EMBED_PAGE_FIVE_BODY") String embedPageFiveBody,
    @JsonProperty("EMBED_PAGE_SIX_TITLE") String embedPageSixTitle,
    @JsonProperty("EMBED_PAGE_SIX_BODY") String embedPageSixBody,
    @JsonProperty("EMBED_FOOTER_TEXT") String embedFooterText,
    @JsonProperty("EMBED_FOOTER_IMG") String embedFooterImg
) {}
