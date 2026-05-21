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

public record TranslationSettings(
    @JsonProperty("EMBED_SETTINGS_TITLE") String embedSettingsTitle,
    @JsonProperty("EMBED_SETTINGS_BODY") String embedSettingsBody,
    @JsonProperty("EMBED_SETTINGS_FIELD_LANGUAGE_TITLE") String embedSettingsFieldLanguageTitle,
    @JsonProperty("EMBED_SETTINGS_FIELD_LANGUAGE_VALUE") String embedSettingsFieldLanguageValue,
    @JsonProperty("EMBED_SETTINGS_FIELD_GUILD_TITLE") String embedSettingsFieldGuildTitle,
    @JsonProperty("EMBED_SETTINGS_FIELD_GUILD_VALUE") String embedSettingsFieldGuildValue,
    @JsonProperty("EMBED_SETTINGS_FIELD_DATABASE_TITLE") String embedSettingsFieldDatabaseTitle,
    @JsonProperty("EMBED_SETTINGS_FIELD_DATABASE_VALUE") String embedSettingsFieldDatabaseValue,
    @JsonProperty("EMBED_SETTINGS_FIELD_CEDMOD_TITLE") String embedSettingsFieldCedmodTitle,
    @JsonProperty("EMBED_SETTINGS_FIELD_CEDMOD_VALUE") String embedSettingsFieldCedmodValue,
    @JsonProperty("EMBED_SETTINGS_FIELD_VERSION_TITLE") String embedSettingsFieldVersionTitle,
    @JsonProperty("EMBED_SETTINGS_FIELD_VERSION_VALUE") String embedSettingsFieldVersionValue,
    @JsonProperty("EMBED_SETTINGS_FIELD_BUILD_TITLE") String embedSettingsFieldBuildTitle,
    @JsonProperty("EMBED_SETTINGS_FIELD_BUILD_VALUE") String embedSettingsFieldBuildValue,
    @JsonProperty("EMBED_SETTINGS_FIELD_GATEWAY_TITLE") String embedSettingsFieldGatewayTitle,
    @JsonProperty("EMBED_SETTINGS_FIELD_GATEWAY_VALUE") String embedSettingsFieldGatewayValue,
    @JsonProperty("EMBED_SETTINGS_FIELD_REST_TITLE") String embedSettingsFieldRestTitle,
    @JsonProperty("EMBED_SETTINGS_FIELD_REST_VALUE") String embedSettingsFieldRestValue,
    @JsonProperty("TEXT_CEDMOD_ONLINE") String textCedmodOnline,
    @JsonProperty("TEXT_CEDMOD_OFFLINE") String textCedmodOffline,
    @JsonProperty("TEXT_DATABASE_ONLINE") String textDatabaseOnline,
    @JsonProperty("TEXT_DATABASE_OFFLINE") String textDatabaseOffline
) {}
