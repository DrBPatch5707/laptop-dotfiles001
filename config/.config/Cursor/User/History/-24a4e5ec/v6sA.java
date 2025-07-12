package org.drbpatch;

import javax.swing.*;
import java.sql.*;
import org.drbpatch.ProjectSync;

/**
 * Handles application initialization, including database connection, project info loading,
 * and filesystem synchronization. All methods are static.
 */
public class init {

    //region Startup
    /**
     * Starts the application: connects to the database, loads project info, and sets up shutdown hooks.
     */
    public static void start() {
        try {
            //region establish connection to database
            try {
                Main.connection = DbConnection.connect();
                if (Main.connection != null) {
                    if (Config.DEBUG) System.out.println("Database connection " + Main.connection.getMetaData().getURL() + " successful");
                    DbConnection.createProjectsTable(Main.connection);
                } else {
                    if (Config.DEBUG) System.err.println("Error establishing database connection...");
                }
            }catch (SQLException e) {
                if (Config.DEBUG) System.out.println("Error connecting to database: " + e.getMessage());
            }
            //endregion
            PopulateInfo.PopulateProjectInfo();

            // Note: Filesystem sync will be triggered from GUI after it's created
            if (Config.DEBUG) System.out.println("Database loaded. Filesystem sync will be performed after GUI startup.");

            // Add shutdown hook to close database connection when application exits
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (Main.connection != null) {
                    DbConnection.close(Main.connection);
                    if (Config.DEBUG) System.out.println("Database connection closed on application shutdown.");
                }
            }));

        } catch (Exception e) {
            if (Config.DEBUG) System.out.println("An unexpected error has occurred in main: " + e.getMessage());
        }

        // Note: Connection will be closed when the application exits
        // We don't close it here because the GUI needs it to stay open

    }
    //endregion

    public static void handleOrphanedProjects(java.util.List<Project> orphanedProjects) {
        if (Config.DEBUG) System.out.println("\n=== ORPHANED PROJECTS DETECTED ===");
        for (Project project : orphanedProjects) {
            if (Config.DEBUG) System.out.println("• " + project.getName() + " (" + project.getRelativePath() + ")");
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
                if (Config.DEBUG) System.out.println("Keeping all orphaned projects in database");
                break;
            case 2: // Review Each
                reviewOrphanedProjects(orphanedProjects);
                break;
            case 3: // Skip
            default:
                if (Config.DEBUG) System.out.println("Skipping orphaned project cleanup");
                break;
        }
    }
    
    public static void handleMismatchedProjects(java.util.List<Project> mismatchedProjects) {
        if (Config.DEBUG) System.out.println("\n=== NAME MISMATCHES DETECTED ===");
        for (Project project : mismatchedProjects) {
            String dirName = java.nio.file.Paths.get(BackendAPI.root, project.getRelativePath()).getFileName().toString();
            if (Config.DEBUG) System.out.println("• DB: '" + project.getName() + "' vs FS='" + dirName + "'");
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
                if (Config.DEBUG) System.out.println("Keeping database names for all projects");
                break;
            case 2: // Review Each
                reviewMismatchedProjects(mismatchedProjects);
                break;
            case 3: // Skip
            default:
                if (Config.DEBUG) System.out.println("Skipping name mismatch cleanup");
                break;
        }
    }
    
    public static void handleUnregisteredProjects(java.util.List<String> unregisteredProjects) {
        if (Config.DEBUG) System.out.println("\n=== UNREGISTERED PROJECTS DETECTED ===");
        for (String projectPath : unregisteredProjects) {
            if (Config.DEBUG) System.out.println("• " + projectPath);
        }
        
        String[] options = {"Add All to Database", "Review Each", "Skip"};
        int choice = javax.swing.JOptionPane.showOptionDialog(
            null,
            "Found " + unregisteredProjects.size() + " projects on filesystem that are not in the database.\n\n" +
            "What would you like to do?",
            "Unregistered Projects",
            javax.swing.JOptionPane.DEFAULT_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[2] // Default to "Skip"
        );
        
        switch (choice) {
            case 0: // Add All to Database
                if (javax.swing.JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to add all " + unregisteredProjects.size() + " projects to the database?",
                    "Confirm Add All",
                    javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION) {
                    addUnregisteredProjectsToDatabase(unregisteredProjects);
                }
                break;
            case 1: // Review Each
                reviewUnregisteredProjects(unregisteredProjects);
                break;
            case 2: // Skip
            default:
                if (Config.DEBUG) System.out.println("Skipping unregistered project addition");
                break;
        }
    }
    
    private static void deleteOrphanedProjects(java.util.List<Project> orphanedProjects) {
        java.util.List<Integer> projectIdsToRemove = new java.util.ArrayList<>();
        for (Project project : orphanedProjects) {
            if (Config.DEBUG) System.out.println("Removing orphaned project from database: " + project.getName());
            BackendAPI.projects.remove(project.getId());
            projectIdsToRemove.add(project.getId());
        }
        DbConnection.cleanupOrphanedProjects(projectIdsToRemove);
        if (Config.DEBUG) System.out.println("Removed " + orphanedProjects.size() + " orphaned projects from database");
    }
    
    private static void updateProjectNamesToFilesystem(java.util.List<Project> mismatchedProjects) {
        for (Project project : mismatchedProjects) {
            String dirName = java.nio.file.Paths.get(BackendAPI.root, project.getRelativePath()).getFileName().toString();
            if (Config.DEBUG) System.out.println("Updating project name: '" + project.getName() + "' → '" + dirName + "'");
            project.setName(dirName);
            // Update in database
            DbConnection.updateProjectName(project.getId(), dirName);
        }
        if (Config.DEBUG) System.out.println("Updated " + mismatchedProjects.size() + " project names to match filesystem");
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
                        if (Config.DEBUG) System.out.println("Deleted project: " + project.getName());
                    }
                    break;
                case 1: // Keep
                    if (Config.DEBUG) System.out.println("Keeping project: " + project.getName());
                    break;
                case 2: // Add to Database
                    // This option doesn't make sense for orphaned projects, but keeping for consistency
                    if (Config.DEBUG) System.out.println("Project already exists in database: " + project.getName());
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
                        if (Config.DEBUG) System.out.println("Updated project name: '" + project.getName() + "' → '" + dirName + "'");
                    }
                    break;
                case 1: // Keep Database Name
                    if (Config.DEBUG) System.out.println("Keeping database name: " + project.getName());
                    break;
                case 2: // Skip
                default:
                    if (Config.DEBUG) System.out.println("Skipping name change for: " + project.getName());
                    break;
            }
        }
    }
    
    private static void addUnregisteredProjectsToDatabase(java.util.List<String> unregisteredProjects) {
        for (String projectPath : unregisteredProjects) {
            String projectName = java.nio.file.Paths.get(projectPath).getFileName().toString();
            Project newProject = new Project();
            newProject.setName(projectName);
            newProject.setRelativePath(projectPath);
            newProject.setStatus("In Development");
            newProject.setPriority(3);
            newProject.setCategory("");
            newProject.setDirExists(true); // Directory already exists
            
            if (BackendAPI.addProject(newProject, true)) { // true = skip directory creation
                if (Config.DEBUG) System.out.println("Added unregistered project to database: " + projectName + " (" + projectPath + ")");
            } else {
                if (Config.DEBUG) System.out.println("Failed to add project to database: " + projectName + " (" + projectPath + ")");
            }
        }
    }
    
    private static void reviewUnregisteredProjects(java.util.List<String> unregisteredProjects) {
        for (String projectPath : unregisteredProjects) {
            String projectName = java.nio.file.Paths.get(projectPath).getFileName().toString();
            String[] options = {"Add to Database", "Skip"};
            int choice = javax.swing.JOptionPane.showOptionDialog(
                null,
                "Project: " + projectName + "\nPath: " + projectPath + "\n\n" +
                "This project exists on the filesystem but is not in the database.\n" +
                "Would you like to add it to the database?",
                "Review Unregistered Project: " + projectName,
                javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1] // Default to "Skip"
            );
            
            switch (choice) {
                case 0: // Add to Database
                    if (javax.swing.JOptionPane.showConfirmDialog(null, 
                        "Are you sure you want to add '" + projectName + "' to the database?",
                        "Confirm Add",
                        javax.swing.JOptionPane.YES_NO_OPTION) == javax.swing.JOptionPane.YES_OPTION) {
                        
                        Project newProject = new Project();
                        newProject.setName(projectName);
                        newProject.setRelativePath(projectPath);
                        newProject.setStatus("In Development");
                        newProject.setPriority(3);
                        newProject.setCategory("");
                        newProject.setDirExists(true); // Directory already exists
                        
                        if (BackendAPI.addProject(newProject, true)) { // true = skip directory creation
                            if (Config.DEBUG) System.out.println("Added project to database: " + projectName + " (" + projectPath + ")");
                        } else {
                            if (Config.DEBUG) System.out.println("Failed to add project to database: " + projectName + " (" + projectPath + ")");
                        }
                    }
                    break;
                case 1: // Skip
                default:
                    if (Config.DEBUG) System.out.println("Skipping project: " + projectName);
                    break;
            }
        }
    }
}