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

            // Scan for unregistered projects
            BackendAPI.scanForUnregisteredProjects();

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
        java.util.List<Project> orphanedProjects = new java.util.ArrayList<>();
        java.util.List<Project> mismatchedProjects = new java.util.ArrayList<>();
        
        // Check for orphaned projects (exist in DB but not on filesystem)
        for (Project project : BackendAPI.projects.values()) {
            String relPath = project.getRelativePath();
            if (relPath != null && !relPath.trim().isEmpty() && BackendAPI.root != null) {
                java.nio.file.Path projectPath = java.nio.file.Paths.get(BackendAPI.root, relPath);
                if (!java.nio.file.Files.exists(projectPath)) {
                    System.out.println("Project directory not found: " + project.getName() + " (" + relPath + ")");
                    orphanedProjects.add(project);
                } else {
                    // Check for name mismatches (directory exists but name is different)
                    String dirName = projectPath.getFileName().toString();
                    if (!dirName.equals(project.getName())) {
                        System.out.println("Name mismatch detected: DB='" + project.getName() + "' vs FS='" + dirName + "' (" + relPath + ")");
                        mismatchedProjects.add(project);
                    }
                }
            }
        }
        
        // Handle orphaned projects
        if (!orphanedProjects.isEmpty()) {
            handleOrphanedProjects(orphanedProjects);
        }
        
        // Handle mismatched projects
        if (!mismatchedProjects.isEmpty()) {
            handleMismatchedProjects(mismatchedProjects);
        }
        
        if (orphanedProjects.isEmpty() && mismatchedProjects.isEmpty()) {
            System.out.println("All projects exist on filesystem and names match - no sync needed");
        }
    }
    
    private static void handleOrphanedProjects(java.util.List<Project> orphanedProjects) {
        System.out.println("\n=== ORPHANED PROJECTS DETECTED ===");
        for (Project project : orphanedProjects) {
            System.out.println("• " + project.getName() + " (" + project.getRelativePath() + ")");
        }
        
        String[] options = {"Delete All", "Keep All", "Review Each", "Skip"};
        int choice = javax.swing.JOptionPane.showOptionDialog(
            null,
            "Found " + orphanedProjects.size() + " projects in database that don't exist on filesystem.\n\n" +
            "What would you like to do?",
            "Orphaned Projects",
            javax.swing.JOptionPane.DEFAULT_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[3] // Default to "Skip"
        );
        
        switch (choice) {
            case 0: // Delete All
                if (javax.swing.JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to delete all " + orphanedProjects.size() + " orphaned projects from the database?",
                    "Confirm Delete All",
                    javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION) {
                    deleteOrphanedProjects(orphanedProjects);
                }
                break;
            case 1: // Keep All
                System.out.println("Keeping all orphaned projects in database");
                break;
            case 2: // Review Each
                reviewOrphanedProjects(orphanedProjects);
                break;
            case 3: // Skip
            default:
                System.out.println("Skipping orphaned project cleanup");
                break;
        }
    }
    
    private static void handleMismatchedProjects(java.util.List<Project> mismatchedProjects) {
        System.out.println("\n=== NAME MISMATCHES DETECTED ===");
        for (Project project : mismatchedProjects) {
            String dirName = java.nio.file.Paths.get(BackendAPI.root, project.getRelativePath()).getFileName().toString();
            System.out.println("• DB: '" + project.getName() + "' vs FS: '" + dirName + "'");
        }
        
        String[] options = {"Update All to Filesystem Names", "Keep Database Names", "Review Each", "Skip"};
        int choice = javax.swing.JOptionPane.showOptionDialog(
            null,
            "Found " + mismatchedProjects.size() + " projects with name mismatches between database and filesystem.\n\n" +
            "What would you like to do?",
            "Name Mismatches",
            javax.swing.JOptionPane.DEFAULT_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[3] // Default to "Skip"
        );
        
        switch (choice) {
            case 0: // Update All to Filesystem Names
                if (javax.swing.JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to update all " + mismatchedProjects.size() + " project names to match the filesystem?",
                    "Confirm Update All",
                    javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION) {
                    updateProjectNamesToFilesystem(mismatchedProjects);
                }
                break;
            case 1: // Keep Database Names
                System.out.println("Keeping database names for all projects");
                break;
            case 2: // Review Each
                reviewMismatchedProjects(mismatchedProjects);
                break;
            case 3: // Skip
            default:
                System.out.println("Skipping name mismatch cleanup");
                break;
        }
    }
    
    private static void deleteOrphanedProjects(java.util.List<Project> orphanedProjects) {
        java.util.List<Integer> projectIdsToRemove = new java.util.ArrayList<>();
        for (Project project : orphanedProjects) {
            System.out.println("Removing orphaned project from database: " + project.getName());
            BackendAPI.projects.remove(project.getId());
            projectIdsToRemove.add(project.getId());
        }
        DbConnection.cleanupOrphanedProjects(projectIdsToRemove);
        System.out.println("Removed " + orphanedProjects.size() + " orphaned projects from database");
    }
    
    private static void updateProjectNamesToFilesystem(java.util.List<Project> mismatchedProjects) {
        for (Project project : mismatchedProjects) {
            String dirName = java.nio.file.Paths.get(BackendAPI.root, project.getRelativePath()).getFileName().toString();
            System.out.println("Updating project name: '" + project.getName() + "' → '" + dirName + "'");
            project.setName(dirName);
            // Update in database
            DbConnection.updateProjectName(project.getId(), dirName);
        }
        System.out.println("Updated " + mismatchedProjects.size() + " project names to match filesystem");
    }
    
    private static void reviewOrphanedProjects(java.util.List<Project> orphanedProjects) {
        for (Project project : orphanedProjects) {
            String[] options = {"Delete", "Keep", "Add to Database"};
            int choice = javax.swing.JOptionPane.showOptionDialog(
                null,
                "Project: " + project.getName() + "\nPath: " + project.getRelativePath() + "\n\n" +
                "This project exists in the database but not on the filesystem.\n" +
                "What would you like to do?",
                "Review Project: " + project.getName(),
                javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1] // Default to "Keep"
            );
            
            switch (choice) {
                case 0: // Delete
                    if (javax.swing.JOptionPane.showConfirmDialog(null, 
                        "Are you sure you want to delete '" + project.getName() + "' from the database?",
                        "Confirm Delete",
                        javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION) {
                        BackendAPI.projects.remove(project.getId());
                        DbConnection.deleteProject(project);
                        System.out.println("Deleted project: " + project.getName());
                    }
                    break;
                case 1: // Keep
                    System.out.println("Keeping project: " + project.getName());
                    break;
                case 2: // Add to Database
                    // This option doesn't make sense for orphaned projects, but keeping for consistency
                    System.out.println("Project already exists in database: " + project.getName());
                    break;
            }
        }
    }
    
    private static void reviewMismatchedProjects(java.util.List<Project> mismatchedProjects) {
        for (Project project : mismatchedProjects) {
            String dirName = java.nio.file.Paths.get(BackendAPI.root, project.getRelativePath()).getFileName().toString();
            String[] options = {"Use Filesystem Name", "Keep Database Name", "Skip"};
            int choice = javax.swing.JOptionPane.showOptionDialog(
                null,
                "Project: " + project.getName() + "\nPath: " + project.getRelativePath() + "\n\n" +
                "Database name: '" + project.getName() + "'\n" +
                "Filesystem name: '" + dirName + "'\n\n" +
                "Which name would you like to use?",
                "Review Name Mismatch: " + project.getName(),
                javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2] // Default to "Skip"
            );
            
            switch (choice) {
                case 0: // Use Filesystem Name
                    if (javax.swing.JOptionPane.showConfirmDialog(null, 
                        "Are you sure you want to change the name from '" + project.getName() + "' to '" + dirName + "'?",
                        "Confirm Name Change",
                        javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION) {
                        project.setName(dirName);
                        DbConnection.updateProjectName(project.getId(), dirName);
                        System.out.println("Updated project name: '" + project.getName() + "' → '" + dirName + "'");
                    }
                    break;
                case 1: // Keep Database Name
                    System.out.println("Keeping database name: " + project.getName());
                    break;
                case 2: // Skip
                default:
                    System.out.println("Skipping name change for: " + project.getName());
                    break;
            }
        }
    }
}