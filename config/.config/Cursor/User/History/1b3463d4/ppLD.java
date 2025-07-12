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

    // Exclude common system/build folders
    private static final Set<String> EXCLUDED_DIRS = new java.util.HashSet<>(java.util.Arrays.asList(
        ".git", "build", ".idea", "out", "node_modules", "venv", "__pycache__", ".gradle", ".vscode", ".svn", ".hg", ".DS_Store"
    ));

    private static void scanForUnregisteredProjects(Path dir, String relativePath, Set<String> registeredPaths, List<String> unregisteredProjects) {
        boolean dbIsEmpty = registeredPaths.isEmpty();
        try {
            List<Path> entryList = Files.list(dir).collect(Collectors.toList());
            for (Path entry : entryList) {
                if (Files.isDirectory(entry)) {
                    String entryName = entry.getFileName().toString();
                    if (EXCLUDED_DIRS.contains(entryName)) {
                        System.out.println("[EXCLUDED] " + entryName);
                        continue; // skip excluded directories
                    }
                    String entryRelativePath = relativePath.isEmpty() ?
                            entryName :
                            relativePath + "/" + entryName;

                    String normalizedPath = BackendAPI.normalizeRelativePath(entryRelativePath);

                    if (dbIsEmpty) {
                        // Only consider leaf directories as projects if DB is empty
                        boolean isLeaf = Files.list(entry).noneMatch(Files::isDirectory);
                        if (isLeaf) {
                            System.out.println("[DETECTED PROJECT - LEAF ONLY, DB EMPTY] " + normalizedPath);
                            unregisteredProjects.add(normalizedPath);
                        }
                    } else {
                        // Check if this directory is already registered as a project
                        if (!registeredPaths.contains(normalizedPath)) {
                            // Improved: Check if this directory is a parent or child (any ancestor/descendant) of any registered project
                            boolean isRelatedToProject = isParentOrChild(normalizedPath, registeredPaths);
                            if (!isRelatedToProject) {
                                System.out.println("[DETECTED PROJECT] " + normalizedPath);
                                if (appearsToBeProject(entry)) {
                                    unregisteredProjects.add(normalizedPath);
                                }
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

    // Helper: returns true if dir is a parent, child, or grandparent, etc. of any registered project
    private static boolean isParentOrChild(String dir, Set<String> registeredPaths) {
        for (String reg : registeredPaths) {
            String normReg = BackendAPI.normalizeRelativePath(reg);
            if (isAncestor(dir, normReg) || isAncestor(normReg, dir)) {
                return true;
            }
        }
        return false;
    }
    // Helper: returns true if ancestor is a true ancestor of descendant (not equal)
    private static boolean isAncestor(String ancestor, String descendant) {
        if (ancestor.equals(descendant)) return false;
        return descendant.startsWith(ancestor + "/");
    }

    /**
     * Determines if a directory appears to be a project based on its contents.
     * @param dir The directory to check
     * @return true if it appears to be a project, false otherwise
     */
    private static boolean appearsToBeProject(Path dir) {
        // Accept any directory that is not an ancestor or descendant of a registered project
        return true;
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