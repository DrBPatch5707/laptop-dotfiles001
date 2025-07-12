package org.drbpatch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ProjectSync handles syncing the database with the filesystem and project detection logic.
 * All methods are static.
 */
public class ProjectSync {
    /**
     * Called from GUI after creation to sync database and filesystem.
     */
    public static void performFilesystemSync() {
        System.out.println("Starting filesystem sync...");
        syncDatabaseWithFilesystem();
        // Scan for unregistered projects (this was previously in init)
        BackendAPI.scanForUnregisteredProjects();
    }

    /**
     * Syncs the database with the filesystem, handling orphaned, mismatched, and unregistered projects.
     */
    private static void syncDatabaseWithFilesystem() {
        System.out.println("Syncing database with filesystem...");
        List<Project> orphanedProjects = new ArrayList<>();
        List<Project> mismatchedProjects = new ArrayList<>();
        List<String> unregisteredProjects = new ArrayList<>();

        // Check for orphaned projects (exist in DB but not on filesystem)
        for (Project project : BackendAPI.projects.values()) {
            String relPath = project.getRelativePath();
            if (relPath != null && !relPath.trim().isEmpty() && BackendAPI.root != null) {
                Path projectPath = java.nio.file.Paths.get(BackendAPI.root, relPath);
                if (!Files.exists(projectPath)) {
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
            Set<String> registeredPaths = new HashSet<>();
            for (Project project : BackendAPI.projects.values()) {
                if (project.getRelativePath() != null && !project.getRelativePath().trim().isEmpty()) {
                    registeredPaths.add(BackendAPI.normalizeRelativePath(project.getRelativePath()));
                }
            }

            try {
                Path rootPath = java.nio.file.Paths.get(BackendAPI.root);
                if (Files.exists(rootPath)) {
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

    private static void scanForUnregisteredProjects(Path dir, String relativePath, Set<String> registeredPaths, List<String> unregisteredProjects) {
        try {
            List<Path> entryList = Files.list(dir).collect(Collectors.toList());
            for (Path entry : entryList) {
                if (Files.isDirectory(entry)) {
                    String entryRelativePath = relativePath.isEmpty() ?
                            entry.getFileName().toString() :
                            relativePath + "/" + entry.getFileName().toString();

                    String normalizedPath = BackendAPI.normalizeRelativePath(entryRelativePath);

                    // Check if this directory is already registered as a project
                    if (!registeredPaths.contains(normalizedPath)) {
                        // Check if this directory is a parent or child of any registered project
                        boolean isRelatedToProject = false;
                        for (String registeredPath : registeredPaths) {
                            if (registeredPath.startsWith(normalizedPath + "/") || normalizedPath.startsWith(registeredPath + "/")) {
                                isRelatedToProject = true;
                                break;
                            }
                        }
                        if (!isRelatedToProject) {
                            if (appearsToBeProject(entry)) {
                                unregisteredProjects.add(normalizedPath);
                            }
                        }
                    }
                    // RECURSE into subdirectories
                    scanForUnregisteredProjects(entry, entryRelativePath, registeredPaths, unregisteredProjects);
                }
            }
        } catch (Exception e) {
            System.err.println("Error scanning directory " + dir + ": " + e.getMessage());
        }
    }

    /**
     * Determines if a directory appears to be a project based on its contents.
     * @param dir The directory to check
     * @return true if it appears to be a project, false otherwise
     */
    private static boolean appearsToBeProject(Path dir) {
        try {
            List<Path> contents = Files.list(dir).collect(Collectors.toList());
            long fileCount = contents.stream().filter(Files::isRegularFile).count();
            long dirCount = contents.stream().filter(Files::isDirectory).count();

            String dirName = dir.getFileName().toString();
            System.out.println("Checking directory: " + dirName + " (files: " + fileCount + ", dirs: " + dirCount + ")");

            if (dirCount > 2 && fileCount <= 1) {
                System.out.println("  -> Rejected: Parent folder (many dirs, few files)");
                return false;
            }

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
                return true;
            }

            if (fileCount >= 2) {
                System.out.println("  -> Accepted: Has multiple files");
                return true;
            }

            if (dirCount == 0) {
                if (fileCount > 0) {
                    System.out.println("  -> Accepted: Leaf directory with files");
                    return true;
                } else {
                    System.out.println("  -> Accepted: Empty leaf directory (potential new project)");
                    return true;
                }
            }

            System.out.println("  -> Rejected: Doesn't appear to be a project");
            return false;
        } catch (Exception e) {
            System.out.println("  -> Rejected: Error reading directory");
            return false;
        }
    }

    // Dummy handlers for orphaned/mismatched/unregistered projects (to be implemented or delegated)
    private static void handleOrphanedProjects(List<Project> orphanedProjects) {
        org.drbpatch.init.handleOrphanedProjects(orphanedProjects);
    }
    private static void handleMismatchedProjects(List<Project> mismatchedProjects) {
        org.drbpatch.init.handleMismatchedProjects(mismatchedProjects);
    }
    private static void handleUnregisteredProjects(List<String> unregisteredProjects) {
        org.drbpatch.init.handleUnregisteredProjects(unregisteredProjects);
    }
} 