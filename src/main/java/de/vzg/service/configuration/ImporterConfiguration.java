package de.vzg.service.configuration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class ImporterConfiguration {

    private Map<String, ImporterConfigurationPart> parts;

    private ImporterConfiguration() {
        this.parts = new HashMap<>();
    }

    public static ImporterConfiguration getConfiguration() {
        return ConfigurationInstanceHolder.instance;
    }

    public Map<String, ImporterConfigurationPart> getParts() {
        return parts;
    }

    private void setParts(Map<String, ImporterConfigurationPart> parts) {
        this.parts = parts;
    }

    private static class ConfigurationInstanceHolder {
        private static final ImporterConfiguration instance = initConfiguration();

        private static ImporterConfiguration initConfiguration() {
            try {
                final String homeFolder = System.getProperty("user.home");
                final Path homeFolderPath = Paths.get(homeFolder);
                final Path wpimportFolder = homeFolderPath.resolve(".wpimport");
                final Path wpConfigFile = wpimportFolder.resolve("config.json");

                if (!Files.exists(wpimportFolder)) {
                    Files.createDirectories(wpimportFolder);
                }

                if (!Files.exists(wpConfigFile)) {
                    return new ImporterConfiguration();
                }

                try (InputStream is = Files.newInputStream(wpConfigFile)) {
                    try (Reader reader = new InputStreamReader(is)) {
                        final ImporterConfiguration config = new Gson().fromJson(reader, ImporterConfiguration.class);
                        return config;
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException("Error while reading configuration!", e);
            }
        }
    }
}
