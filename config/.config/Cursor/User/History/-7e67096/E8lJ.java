package org.drbpatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * JacksonIdeConfigManager handles loading and saving IDE configuration using Jackson JSON serialization.
 * Provides methods to set the config file path, load, and save configuration.
 */
public class JacksonIdeConfigManager {
    //region Fields
    /** Jackson ObjectMapper for JSON serialization. */
    private static ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    /** Path to the config file. */
    private static String configFilePath = "ide_config.json";
    //endregion

    //region Config File Path
    /**
     * Sets the config file path based on the application config directory.
     * @param appConfigDir The directory to use
     */
    public static void setConfigFilePath(String appConfigDir) {
        configFilePath = appConfigDir + File.separator + "ide_config.json";
    }
    //endregion

    //region Load/Save
    /**
     * Loads the configuration from the config file, creating a default if missing or empty.
     * @return The loaded Config object
     * @throws IOException if file operations fail
     */
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
    /**
     * Saves the given configuration to the config file.
     * @param config The Config to save
     * @throws IOException if file operations fail
     */
    public static void saveConfiguration(Config config) throws IOException {
        File configFile = new File(configFilePath);
        File configDir = configFile.getParentFile();
        if (configDir != null && !configDir.exists()) {
            configDir.mkdirs(); // Ensure directory exists
        }
        objectMapper.writeValue(configFile, config);
        System.out.println("Saved JSON config to: " + configFilePath);
    }
    //endregion

    //region Defaults
    /**
     * Creates a default configuration with a default IDE.
     * @return The default Config object
     */
    private static Config createDefaultConfiguration() {
        Config config = new Config();
        config.setDefaultIdeId("vscode");
        config.addIde("default", new IDE("default","default", "", ""));
        return config;
    }
    //endregion
}