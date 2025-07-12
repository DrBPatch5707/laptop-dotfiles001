package org.drbpatch;

import java.io.IOException;
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
        // Launch the frontend GUI
        javax.swing.SwingUtilities.invokeLater(() -> new ProjectManagerGUI().setVisible(true));
        Settings.getRoot();
    }
}