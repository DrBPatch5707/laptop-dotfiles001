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

            // Note: Filesystem sync will be triggered from GUI after it's created
            System.out.println("Database loaded. Filesystem sync will be performed after GUI startup.");

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

    // Method to be called from GUI after it's created
    public static void performFilesystemSync() {
        System.out.println("Starting filesystem sync...");
        syncDatabaseWithFilesystem();
        // Scan for unregistered projects (this was previously in init)
        BackendAPI.scanForUnregisteredProjects();
    }

    private static void syncDatabaseWithFilesystem() {
        System.out.println("Syncing database with filesystem...");
        java.util.List<Project> orphanedProjects = new java.util.ArrayList<>();
        java.util.List<Project> mismatchedProjects = new java.util.ArrayList<>();
        java.util.List<String> unregisteredProjects = new java.util.ArrayList<>();
        
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
        
        // Scan for unregistered projects (exist on filesystem but not in DB)
        if (BackendAPI.root != null) {
            java.util.Set<String> registeredPaths = new java.util.HashSet<>();
            for (Project project : BackendAPI.projects.values()) {
                if (project.getRelativePath() != null && !project.getRelativePath().trim().isEmpty()) {
                    registeredPaths.add(BackendAPI.normalizeRelativePath(project.getRelativePath()));
                }
            }
            
            try {
                java.nio.file.Path rootPath = java.nio.file.Paths.get(BackendAPI.root);
                if (java.nio.file.Files.exists(rootPath)) {
                    scanForUnregisteredProjects(rootPath, "", registeredPaths, unregisteredProjects);
                }
            } catch (Exception e) {
                System.err.println("Error scanning for unregistered projects: " + e.getMessage());
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
        
        // Handle unregistered projects
        if (!unregisteredProjects.isEmpty()) {
            handleUnregisteredProjects(unregisteredProjects);
        }
        
        if (orphanedProjects.isEmpty() && mismatchedProjects.isEmpty() && unregisteredProjects.isEmpty()) {
            System.out.println("All projects exist on filesystem and names match - no sync needed");
        }
    }
    
    private static void scanForUnregisteredProjects(java.nio.file.Path dir, String relativePath, 
                                                   java.util.Set<String> registeredPaths, 
                                                   java.util.List<String> unregisteredProjects) {
        try {
            java.util.stream.Stream<java.nio.file.Path> entries = java.nio.file.Files.list(dir);
            for (java.nio.file.Path entry : entries.toArray(java.nio.file.Path[]::new)) {
                if (java.nio.file.Files.isDirectory(entry)) {
                    String entryRelativePath = relativePath.isEmpty() ? 
                        entry.getFileName().toString() : 
                        relativePath + "/" + entry.getFileName().toString();
                    
                    String normalizedPath = BackendAPI.normalizeRelativePath(entryRelativePath);
                    
                    // Check if this directory is already registered as a project
                    if (!registeredPaths.contains(normalizedPath)) {
                        // Check if this directory is a parent or child of any registered project
                        boolean isRelatedToProject = false;
                        
                        for (String registeredPath : registeredPaths) {
                            // Check if this directory is a parent of a registered project
                            if (registeredPath.startsWith(normalizedPath + "/")) {
                                isRelatedToProject = true;
                                break;
                            }
                            // Check if this directory is a child of a registered project
                            if (normalizedPath.startsWith(registeredPath + "/")) {
                                isRelatedToProject = true;
                                break;
                            }
                        }
                        
                        if (!isRelatedToProject) {
                            // Additional check: only suggest directories that appear to be actual projects
                            if (appearsToBeProject(entry)) {
                                unregisteredProjects.add(normalizedPath);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error scanning directory " + dir + ": " + e.getMessage());
        }
    }
    
    private static boolean appearsToBeProject(java.nio.file.Path dir) {
        try {
            // Count files and subdirectories
            java.util.List<java.nio.file.Path> contents = java.nio.file.Files.list(dir).collect(java.util.stream.Collectors.toList());
            long fileCount = contents.stream().filter(p -> java.nio.file.Files.isRegularFile(p)).count();
            long dirCount = contents.stream().filter(p -> java.nio.file.Files.isDirectory(p)).count();
            
            String dirName = dir.getFileName().toString();
            System.out.println("Checking directory: " + dirName + " (files: " + fileCount + ", dirs: " + dirCount + ")");
            
            // If it contains mostly directories and few/no files, it's likely a parent folder
            if (dirCount > 2 && fileCount <= 1) {
                System.out.println("  -> Rejected: Parent folder (many dirs, few files)");
                return false; // Likely a parent/organizational folder
            }
            
            // If it contains project-like files, it's likely a project
            boolean hasProjectFiles = contents.stream().anyMatch(p -> {
                String name = p.getFileName().toString().toLowerCase();
                return name.contains("pom.xml") || name.contains("build.gradle") || name.contains("package.json") ||
                       name.contains("requirements.txt") || name.contains("cargo.toml") || name.contains("go.mod") ||
                       name.contains("composer.json") || name.contains("gemfile") || name.contains("makefile") ||
                       name.endsWith(".sln") || name.endsWith(".csproj") || name.endsWith(".vcxproj") ||
                       name.contains("readme") || name.contains("license") || name.contains(".gitignore");
            });
            
            if (hasProjectFiles) {
                System.out.println("  -> Accepted: Contains project files");
                return true; // Definitely a project
            }
            
            // If it has a reasonable number of files (not just empty or mostly empty)
            if (fileCount >= 2) {
                System.out.println("  -> Accepted: Has multiple files");
                return true; // Likely a project with some content
            }
            
            // If it's a leaf directory (no subdirectories), it might be a simple project
            if (dirCount == 0 && fileCount > 0) {
                System.out.println("  -> Accepted: Leaf directory with files");
                return true; // Leaf directory with files
            }
            
            // More permissive: if it has any files at all, consider it a project
            if (fileCount > 0) {
                System.out.println("  -> Accepted: Has some files");
                return true;
            }
            
            // If it's empty but not a parent folder, it might be a new project
            if (dirCount == 0 && fileCount == 0) {
                System.out.println("  -> Accepted: Empty directory (potential new project)");
                return true;
            }
            
            System.out.println("  -> Rejected: Doesn't appear to be a project");
            return false; // Default to not suggesting it
        } catch (Exception e) {
            System.out.println("  -> Rejected: Error reading directory");
            return false; // If we can't read it, don't suggest it
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
            System.out.println("• DB: '" + project.getName() + "' vs FS='" + dirName + "'");
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
    
    private static void handleUnregisteredProjects(java.util.List<String> unregisteredProjects) {
        System.out.println("\n=== UNREGISTERED PROJECTS DETECTED ===");
        for (String projectPath : unregisteredProjects) {
            System.out.println("• " + projectPath);
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
                System.out.println("Skipping unregistered project addition");
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
                System.out.println("Added unregistered project to database: " + projectName + " (" + projectPath + ")");
            } else {
                System.out.println("Failed to add project to database: " + projectName + " (" + projectPath + ")");
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
                            System.out.println("Added project to database: " + projectName + " (" + projectPath + ")");
                        } else {
                            System.out.println("Failed to add project to database: " + projectName + " (" + projectPath + ")");
                        }
                    }
                    break;
                case 1: // Skip
                default:
                    System.out.println("Skipping project: " + projectName);
                    break;
            }
        }
    }
}