package org.drbpatch;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.IOException;
import org.drbpatch.NotificationManager;
import org.drbpatch.IDEManager;

/**
 * BackendAPI provides core backend operations for project management, including
 * project CRUD, filesystem sync, IDE integration, and notification management.
 * All methods are static and operate on the global state.
 */
public class BackendAPI {
    //read and update/edit can be done through the projects list
    public static Map<Integer, Project> projects = new HashMap<>();
    
    //region setting variables
    public static String root;
    public static boolean setRoot(String newRoot) {
        return Settings.setRoot(newRoot);
    }

    //endregion

    //region Project CRUD
    /**
     * Adds a project to the database and filesystem.
     * @param project The project to add
     * @return true if successful, false otherwise
     */
    public static boolean addProject(Project project) {
        return addProject(project, false);
    }
    /**
     * Adds a project, optionally skipping directory creation.
     * @param project The project to add
     * @param skipDirCreate If true, does not create directory
     * @return true if successful, false otherwise
     */
    public static boolean addProject(Project project, boolean skipDirCreate) {
        System.out.println("addProject called...");
        String relPath = normalizeRelativePath(project.getRelativePath());
        if (relPath == null || relPath.trim().isEmpty()) {
            System.err.println("Project relative path is null or empty.");
            return false;
        }
        // Use the project's dirExists flag, but allow override with parameter
        boolean shouldSkipDirCreate = skipDirCreate || project.isDirExists();
        if (!shouldSkipDirCreate) {
            try {
                Files.createDirectories(Paths.get(root, relPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!Files.exists(Paths.get(root, relPath))) {
                return false;
            }
        }
        return DbConnection.addProject(project);
    }
    
    /**
     * Normalizes a relative path by removing leading dots, slashes, and normalizing separators.
     * This prevents IDEs from interpreting the path as a file instead of a directory.
     * @param relativePath The path to normalize
     * @return Normalized path
     */
    public static String normalizeRelativePath(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return relativePath;
        }
        
        String normalized = relativePath.trim();
        
        // Remove leading dots and slashes
        while (normalized.startsWith(".") || normalized.startsWith("/") || normalized.startsWith("\\")) {
            if (normalized.startsWith("./") || normalized.startsWith(".\\")) {
                normalized = normalized.substring(2);
            } else if (normalized.startsWith(".")) {
                normalized = normalized.substring(1);
            } else if (normalized.startsWith("/") || normalized.startsWith("\\")) {
                normalized = normalized.substring(1);
            }
        }
        
        // Normalize path separators to forward slashes
        normalized = normalized.replace("\\", "/");
        
        // Remove trailing slashes
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        
        return normalized;
    }
    /**
     * Deletes a project from the database and filesystem.
     * @param project The project to delete
     * @return true if successful, false otherwise
     */
    public static boolean deleteProject(Project project) {
        String relPath = project.getRelativePath();
        if (relPath == null || relPath.trim().isEmpty()) {
            System.err.println("Project relative path is null or empty.");
            return false;
        }
        try {
            deleteDirectoryRecursively(Paths.get(root, relPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Files.exists(Paths.get(root, relPath))) {
            return false;
        }
        return DbConnection.deleteProject(project);
    }

    // Helper to recursively delete a directory
    private static void deleteDirectoryRecursively(java.nio.file.Path path) throws IOException {
        if (Files.notExists(path)) return;
        if (Files.isDirectory(path)) {
            try (java.util.stream.Stream<java.nio.file.Path> entries = Files.list(path)) {
                for (java.nio.file.Path entry : entries.toArray(java.nio.file.Path[]::new)) {
                    deleteDirectoryRecursively(entry);
                }
            }
        }
        Files.deleteIfExists(path);
    }

    // Skeleton: Edit a project by ID (replace info in the list)
    public static boolean editProject(Project updatedProject) {
        // TODO: Implement actual update logic
        try {
            deleteProject(projects.get(updatedProject.getId()));
            addProject(updatedProject);
            projects.replace(updatedProject.getId(), updatedProject);

        }catch (Exception e) {
            return false;
        }
        return true;
    }

    // Skeleton: Get a project by ID
    public static Project getProjectById(int id) {
        // TODO: Implement actual fetch logic
        return projects.get(id);
    }
    //endregion

    //region IDE Management
    /**
     * Returns the configured IDEs.
     * @return Map of IDE id to IDE
     */
    public static Map<String, IDE> getIdes() {
        return IDEManager.getIdes();
    }
    
    /**
     * Adds an IDE to the config.
     * @param ide The IDE to add
     * @return true if successful, false otherwise
     */
    public static boolean addIDE(IDE ide) {
        if (ide.getId() == null || ide.getId().trim().isEmpty()) {
            return false;
        }
        IDEManager.addIDE(ide);
        return true;
    }
    
    /**
     * Removes an IDE from the config.
     * @param ideId The IDE id
     * @return true if removed, false otherwise
     */
    public static boolean removeIDE(String ideId) {
        if (ideId == null || ideId.trim().isEmpty()) {
            return false;
        }
        IDE removed = IDEManager.getIdes().remove(ideId);
        return removed != null;
    }
    
    /**
     * Gets an IDE by id.
     * @param ideId The IDE id
     * @return The IDE, or null if not found
     */
    public static IDE getIDEById(String ideId) {
        return IDEManager.getIdes().get(ideId);
    }
    //endregion

    //region Project Access
    /**
     * Opens a project in the selected IDE.
     * @param project The project to access
     * @param selectedIDE The IDE to use
     * @return true if successful, false otherwise
     */
    public static boolean accessProject(Project project, IDE selectedIDE) {
        try {
            String normalizedPath = normalizeRelativePath(project.getRelativePath());
            String projectPath = Paths.get(root, normalizedPath).toAbsolutePath().toString();
            
            // Ensure the path ends with a slash to indicate it's a directory
            if (!projectPath.endsWith("/") && !projectPath.endsWith("\\")) {
                projectPath += "/";
            }
            
            // Verify the path is actually a directory
            if (!Files.isDirectory(Paths.get(projectPath.substring(0, projectPath.length() - 1)))) {
                System.err.println("Error: Project path is not a directory: " + projectPath);
                return false;
            }
            
            java.util.List<String> command = new java.util.ArrayList<>();
            command.add(selectedIDE.getPath());
            command.add(projectPath);
            
            // Add IDE arguments if they exist
            String args = selectedIDE.getArgs();
            if (args != null && !args.trim().isEmpty()) {
                // Split arguments by whitespace and add each as a separate command argument
                String[] argArray = args.trim().split("\\s+");
                for (String arg : argArray) {
                    if (!arg.trim().isEmpty()) {
                        command.add(arg.trim());
                    }
                }
            }
            
            ProcessBuilder pb = new ProcessBuilder(command);
            System.out.println("=== IDE Command Debug ===");
            System.out.println("IDE Name: " + selectedIDE.getName());
            System.out.println("IDE Path: " + selectedIDE.getPath());
            System.out.println("Project Path: " + projectPath);
            System.out.println("IDE Args: " + (args != null ? args : "none"));
            System.out.println("Full Command: " + String.join(" ", command));
            System.out.println("Project exists: " + Files.exists(Paths.get(projectPath.substring(0, projectPath.length() - 1))));
            System.out.println("Project is directory: " + Files.isDirectory(Paths.get(projectPath.substring(0, projectPath.length() - 1))));
            System.out.println("========================");
            
            // Update last modified date when accessing project
            updateProjectLastModified(project);
            
            pb.start();
        } catch (Exception e) {
            System.err.println("Error accessing project: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        System.exit(0);
        return true;
    }
    //endregion
    
    /**
     * Updates the last modified date of a project.
     * Tries to use git commit date if available, otherwise uses current time.
     */
    public static void updateProjectLastModified(Project project) {
        try {
            String projectPath = Paths.get(root, project.getRelativePath()).toString();
            String lastModifiedDate = getGitLastCommitDate(projectPath);
            
            if (lastModifiedDate == null) {
                // Fallback to current time if git is not available
                lastModifiedDate = java.time.LocalDateTime.now().toString();
            }
            
            project.setLastModifiedDate(lastModifiedDate);
            
            // Update in database
            DbConnection.updateProjectLastModified(project.getId(), lastModifiedDate);
            
            System.out.println("Updated last modified date for project '" + project.getName() + "': " + lastModifiedDate);
        } catch (Exception e) {
            System.err.println("Error updating last modified date for project '" + project.getName() + "': " + e.getMessage());
        }
    }
    
    /**
     * Gets the date of the most recent git commit for a project.
     * Returns null if not a git repository or if git is not available.
     */
    private static String getGitLastCommitDate(String projectPath) {
        try {
            java.nio.file.Path gitDir = Paths.get(projectPath, ".git");
            if (!Files.exists(gitDir)) {
                return null; // Not a git repository
            }
            
            ProcessBuilder pb = new ProcessBuilder("git", "log", "-1", "--format=%cd", "--date=iso");
            pb.directory(new java.io.File(projectPath));
            
            Process process = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            
            String output = reader.readLine();
            int exitCode = process.waitFor();
            
            if (exitCode == 0 && output != null && !output.trim().isEmpty()) {
                return output.trim();
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Error getting git commit date: " + e.getMessage());
            return null;
        }
    }

    public static boolean promptForRoot() {
        String temp = JOptionPane.showInputDialog("Enter root directory:");
        if (temp == null || temp.trim().isEmpty()) {
            System.out.println("User cancelled or entered empty root directory");
            return false;
        }
        
        temp = temp.trim();
        if (Files.isDirectory(Paths.get(temp))) {
            boolean success = setRoot(temp);
            if (success) {
                System.out.println("Root directory set successfully to: " + root);
                return true;
            } else {
                System.out.println("Failed to save root directory to file");
                return false;
            }
        } else {
            System.out.println("Invalid directory path: " + temp);
            JOptionPane.showMessageDialog(null, "Invalid directory path: " + temp);
            return false;
        }
    }


    //if more info or actions are required
    // please add additional skeleton functions and variables here
    // I will write implementation for them later.



    //region Notification System
    /**
     * Returns a copy of the notifications list.
     * @return List of notifications
     */
    public static java.util.List<String> getNotifications() {
        return NotificationManager.getNotifications();
    }
    
    /**
     * Clears all notifications.
     */
    public static void clearNotifications() {
        NotificationManager.clearNotifications();
    }
    
    /**
     * Adds a notification to the list.
     * @param notification The notification message
     */
    public static void addNotification(String notification) {
        NotificationManager.addNotification(notification);
    }
    //endregion
    
    public static void scanForUnregisteredProjects() {
        if (root == null) {
            return;
        }
        
        NotificationManager.clearNotifications();
        java.util.Set<String> registeredPaths = new java.util.HashSet<>();
        
        // Get all registered project paths
        for (Project project : projects.values()) {
            if (project.getRelativePath() != null && !project.getRelativePath().trim().isEmpty()) {
                registeredPaths.add(normalizeRelativePath(project.getRelativePath()));
            }
        }
        
        // Scan filesystem for unregistered directories
        try {
            java.nio.file.Path rootPath = java.nio.file.Paths.get(root);
            if (java.nio.file.Files.exists(rootPath)) {
                scanDirectoryForNotifications(rootPath, "", registeredPaths);
            }
        } catch (Exception e) {
            System.err.println("Error scanning filesystem for unregistered projects: " + e.getMessage());
        }
    }
    
    private static void scanDirectoryForNotifications(java.nio.file.Path dir, String relativePath, java.util.Set<String> registeredPaths) {
        try {
            java.util.stream.Stream<java.nio.file.Path> entries = java.nio.file.Files.list(dir);
            for (java.nio.file.Path entry : entries.toArray(java.nio.file.Path[]::new)) {
                if (java.nio.file.Files.isDirectory(entry)) {
                    String entryRelativePath = relativePath.isEmpty() ? 
                        entry.getFileName().toString() : 
                        relativePath + "/" + entry.getFileName().toString();
                    
                    String normalizedPath = normalizeRelativePath(entryRelativePath);
                    
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
                                NotificationManager.addNotification(normalizedPath);
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
            
            // If it contains mostly directories and few/no files, it's likely a parent folder
            if (dirCount > 2 && fileCount <= 1) {
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
                return true; // Definitely a project
            }
            
            // If it has a reasonable number of files (not just empty or mostly empty)
            if (fileCount >= 2) {
                return true; // Likely a project with some content
            }
            
            // If it's a leaf directory (no subdirectories), it might be a simple project
            if (dirCount == 0 && fileCount > 0) {
                return true; // Leaf directory with files
            }
            
            return false; // Default to not suggesting it
        } catch (Exception e) {
            return false; // If we can't read it, don't suggest it
        }
    }
}
