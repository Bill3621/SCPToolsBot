/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.configuration.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.vxrp.configuration.data.Translation;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TranslationFileHandler {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(TranslationFileHandler.class);

    public void create(String dir, List<Path> files) {
        try {
            Files.createDirectories(Path.of(dir + "/SCPToolsBot/lang/"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create lang directories", e);
        }

        for (Path file : files) {
            InputStream content = TranslationFileHandler.class.getResourceAsStream(file.toString());

            Path path = Path.of(dir + file.toString());
            File currentFile = path.toFile();

            if (!currentFile.exists()) {
                try {
                    currentFile.createNewFile();
                    logger.info("Created translation file {}{}", dir, file);

                    if (content != null) {
                        Files.write(path, content.readAllBytes());
                        logger.info("Wrote contents to {}{}", dir, file);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create translation file: " + file, e);
                }
            }
        }
    }

    public Translation query(String dir, String lang) {
        File currentFile = Path.of(dir + "/SCPToolsBot/lang/" + lang + ".yml").toFile();

        if (!currentFile.exists()) {
            logger.error("Could not load configuration set with name: {}", lang);
            System.exit(2);
        }

        logger.debug("Query translation file {}{}", dir, currentFile);

        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            Translation result = mapper.readValue(currentFile, Translation.class);
            logger.debug("Query of translation file {}{} completed", dir, currentFile);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse translation file: " + lang, e);
        }
    }
}
