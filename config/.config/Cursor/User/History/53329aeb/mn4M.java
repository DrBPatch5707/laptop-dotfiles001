package org.drbpatch;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static Config config;
    public static Connection connection = null;
    public static void main(String[] args) throws IOException {
        JacksonIdeConfigManager.setConfigFilePath(".");
        config = JacksonIdeConfigManager.loadConfiguration();
        init.start();
        
        // Load root directory from file first
        try {
            if (!Files.exists(Settings.rootConfPath)) {
                if (Config.DEBUG) System.out.println("Root config file not found, creating new one");
                Files.createFile(Settings.rootConfPath);
                if (Config.DEBUG) System.out.println("Please set the root directory through the Settings menu in the GUI");
            } else {
                if (Config.DEBUG) System.out.println("Reading root from file: " + Settings.rootConfPath);
                String savedRoot = Files.readString(Settings.rootConfPath).trim();
                if (!savedRoot.isEmpty()) {
                    BackendAPI.root = savedRoot;
                    if (Config.DEBUG) System.out.println("Root loaded from file: " + BackendAPI.root);
                } else {
                    if (Config.DEBUG) System.out.println("Root file is empty, please set it through the Settings menu");
                }
            }
        } catch (Exception e) {
            if (Config.DEBUG) System.err.println("Error reading root from file: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Launch the frontend GUI
        javax.swing.SwingUtilities.invokeLater(() -> new ProjectManagerGUI().setVisible(true));
    }
}