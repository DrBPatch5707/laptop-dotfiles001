package org.drbpatch;

import javax.swing.*;
import java.sql.*;

public class init {



    public static void start() {
        try {
            //region establish connection to database
            try {
                Main.connection = DbConnection.connect();
                if (Main.connection != null) {
                    System.out.println("Database connection " + Main.connection.getMetaData().getURL() + " successful");
                    DbConnection.createProjectsTable(Main.connection);
                } else {
                    System.err.println("Error establishing database connection...");
                }
            }catch (SQLException e) {
                System.out.println("Error connecting to database: " + e.getMessage());
            }
            //endregion
            PopulateInfo.PopulateProjectInfo();

            // Sync database with filesystem
            syncDatabaseWithFilesystem();

            // Add shutdown hook to close database connection when application exits
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (Main.connection != null) {
                    DbConnection.close(Main.connection);
                    System.out.println("Database connection closed on application shutdown.");
                }
            }));

        } catch (Exception e) {
            System.out.println("An unexpected error has occurred in main: " + e.getMessage());
        }

        // Note: Connection will be closed when the application exits
        // We don't close it here because the GUI needs it to stay open

    }

    private static void syncDatabaseWithFilesystem() {
        System.out.println("Syncing database with filesystem...");
        java.util.List<Project> projectsToRemove = new java.util.ArrayList<>();
        
        for (Project project : BackendAPI.projects.values()) {
            String relPath = project.getRelativePath();
            if (relPath != null && !relPath.trim().isEmpty() && BackendAPI.root != null) {
                java.nio.file.Path projectPath = java.nio.file.Paths.get(BackendAPI.root, relPath);
                if (!java.nio.file.Files.exists(projectPath)) {
                    System.out.println("Project directory not found, marking for removal: " + project.getName() + " (" + relPath + ")");
                    projectsToRemove.add(project);
                }
            }
        }
        
        // Remove projects that no longer exist on filesystem
        if (!projectsToRemove.isEmpty()) {
            java.util.List<Integer> projectIdsToRemove = new java.util.ArrayList<>();
            for (Project project : projectsToRemove) {
                System.out.println("Removing project from database: " + project.getName());
                BackendAPI.projects.remove(project.getId());
                projectIdsToRemove.add(project.getId());
            }
            
            // Clean up the database
            DbConnection.cleanupOrphanedProjects(projectIdsToRemove);
            System.out.println("Removed " + projectsToRemove.size() + " projects that no longer exist on filesystem");
        } else {
            System.out.println("All projects exist on filesystem - no sync needed");
        }
    }
}