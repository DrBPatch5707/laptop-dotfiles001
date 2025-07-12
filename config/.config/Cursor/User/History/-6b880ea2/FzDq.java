package org.drbpatch;

import javax.swing.*;
import java.sql.*;

public class Main {

    public static GUI_Connection GUI = new GUI_Connection();
    public static Connection connection = null;
    public static void main(String[] args) {
        try {
            //region establish connection to database
            try {
                connection = DbConnection.connect();
                if (connection != null) {
                    System.out.println("Database connection " + connection.getMetaData().getURL() + " successful");
                    DbConnection.createProjectsTable(connection);
                } else {
                    System.err.println("Error establishing database connection...");
                }
            }catch (SQLException e) {
                System.out.println("Error connecting to database: " + e.getMessage());
            }
            //endregion
            PopulateInfo.PopulateProjectInfo();
            
            // Add shutdown hook to close database connection when application exits
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (connection != null) {
                    DbConnection.close(connection);
                    System.out.println("Database connection closed on application shutdown.");
                }
            }));
            
            SwingUtilities.invokeLater(() -> {
                ProjectManagerGUI gui = new Projectmanager(GUI);
                gui.setVisible(true);
            });

        } catch (Exception e) {
            System.out.println("An unexpected error has occurred in main: " + e.getMessage());
        }
        // Note: Connection will be closed when the application exits
        // We don't close it here because the GUI needs it to stay open

    }
}