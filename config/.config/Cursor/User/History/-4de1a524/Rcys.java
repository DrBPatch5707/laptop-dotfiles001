package org.drbpatch;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.JOptionPane;
import java.awt.Component;

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
        System.out.println("addProject called...");
        return DbConnection.addProject(project);
    }
    public static boolean deleteProject(Project project) {
       return DbConnection.deleteProject(project);
    }

    // Skeleton: Edit a project by ID (replace info in the list)
    public static boolean editProject(Project updatedProject) {
        // TODO: Implement actual update logic
        try {
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
        return Config.getIdes();
    }
    public static boolean accessProject(Project project, IDE selectedIDE) {
        //TODO:I will implement this later
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    selectedIDE.getPath(),
                    Paths.get(project.getRelativePath()).toAbsolutePath() +
                            "/ " +
                            selectedIDE.getArgs());
            pb.start();
        }catch (Exception e) {
            return  false;
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







}
