package org.drbpatch;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

public class Settings {
   public  static final Path rootConfPath = Paths.get("./root.txt");
    public static boolean setRoot(String newRoot) {
        System.out.println("Setting root to: " + newRoot);
        BackendAPI.root = newRoot;
        
        try {
            if (!Files.exists(rootConfPath)) {
                System.out.println("Creating root config file: " + rootConfPath);
                Files.createFile(rootConfPath);
            }
            
            System.out.println("Writing root to file: " + rootConfPath);
            Files.write(rootConfPath, Collections.singletonList(newRoot), StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Root successfully written to file");
            return true;
        } catch (Exception e) {
            System.err.println("Error writing root to file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static void getRoot() {
        try {
            if (!Files.exists(rootConfPath)) {
                System.out.println("Root config file not found, creating new one");
                Files.createFile(rootConfPath);
                System.out.println("Prompting user for root directory");
                if (!BackendAPI.promptForRoot()) {
                    System.out.println("Failed to get root directory from user");
                }
            } else {
                System.out.println("Reading root from file: " + rootConfPath);
                String savedRoot = Files.readString(rootConfPath).trim();
                if (!savedRoot.isEmpty()) {
                    BackendAPI.root = savedRoot;
                    System.out.println("Root loaded from file: " + BackendAPI.root);
                } else {
                    System.out.println("Root file is empty, prompting user");
                    if (!BackendAPI.promptForRoot()) {
                        System.out.println("Failed to get root directory from user");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading root from file: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Prompting user for root directory due to error");
            if (!BackendAPI.promptForRoot()) {
                System.out.println("Failed to get root directory from user");
            }
        }
    }
}
