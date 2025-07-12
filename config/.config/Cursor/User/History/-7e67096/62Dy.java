package org.drbpatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class JacksonIdeConfigManager {
    private static ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static String configFilePath = "ide_config.json";

    public static void setConfigFilePath(String appConfigDir) {
        configFilePath = appConfigDir + File.separator + "ide_config.json";
    }

    public static Config loadConfiguration() throws IOException {
        File configFile = new File(configFilePath);
        if (!configFile.exists() || configFile.length() == 0) {
            System.out.println("JSON config file not found or empty. Creating default.");
            // Create and save a default configuration if file doesn't exist or is empty
            Config defaultConfig = createDefaultConfiguration();
            saveConfiguration(defaultConfig);
            return defaultConfig;
        }
        System.out.println("Loading JSON config from: " + configFilePath);
        return objectMapper.readValue(configFile, Config.class);
    }

    public static void saveConfiguration(Config config) throws IOException {
        File configFile = new File(configFilePath);
        File configDir = configFile.getParentFile();
        if (configDir != null && !configDir.exists()) {
            configDir.mkdirs(); // Ensure directory exists
        }
        objectMapper.writeValue(configFile, config);
        System.out.println("Saved JSON config to: " + configFilePath);
    }

    private static Config createDefaultConfiguration() {
        Config config = new Config();
        config.setDefaultIdeId("vscode");
        config.addIde("vscode", new IDE("vscode","Visual Studio Code", "/usr/bin/code", ""));
        config.addIde("intellij", new IDE("intellij","IntelliJ IDEA", "/opt/idea-IC/bin/idea.sh", ""));
        return config;
    }
}