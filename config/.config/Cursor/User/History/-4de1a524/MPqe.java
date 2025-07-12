package org.drbpatch;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.IOException;

public class BackendAPI {
    //read and update/edit can be done through the projects list
    public static Map<Integer, Project> projects = new HashMap<>();

    //region setting variables
    public static String root;
    public static boolean setRoot(String newRoot) {
        return Settings.setRoot(newRoot);
    }

    //endregion

    public static boolean addProject(Project project) {
        return addProject(project, false);
    }
    public static boolean addProject(Project project, boolean skipDirCreate) {
        System.out.println("addProject called...");
        String relPath = project.getRelativePath();
        if (relPath == null || relPath.trim().isEmpty()) {
            System.err.println("Project relative path is null or empty.");
            return false;
        }
        if (!skipDirCreate) {
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
    public static Map<String, IDE> getIdes() {
        return Main.config.getIdes();
    }
    
    public static boolean addIDE(IDE ide) {
        if (ide.getId() == null || ide.getId().trim().isEmpty()) {
            return false;
        }
        Main.config.addIde(ide.getId(), ide);
        return true;
    }
    
    public static boolean removeIDE(String ideId) {
        if (ideId == null || ideId.trim().isEmpty()) {
            return false;
        }
        IDE removed = Main.config.getIdes().remove(ideId);
        return removed != null;
    }
    
    public static IDE getIDEById(String ideId) {
        return Main.config.getIdes().get(ideId);
    }
    public static boolean accessProject(Project project, IDE selectedIDE) {
        try {
            String projectPath = Paths.get(root, project.getRelativePath()).toString();
            java.util.List<String> command = new java.util.ArrayList<>();
            command.add(selectedIDE.getPath());
            command.add(projectPath);
            String args = selectedIDE.getArgs();
            if (args != null && !args.trim().isEmpty()) {
                // Split by whitespace, respecting quoted args
                java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(args);
                while (tokenizer.hasMoreTokens()) {
                    command.add(tokenizer.nextToken());
                }
            }
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.start();
        } catch (Exception e) {
            return false;
        }
        System.exit(0);
        return true;
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



    public static Map<String, IDE> getIDEs() {
        return Main.config.getIdes();
    }
}
