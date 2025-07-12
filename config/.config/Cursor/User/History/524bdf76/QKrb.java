package gui.org.drbpatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple test backend service for testing the GUI without a full backend implementation.
 * Uses in-memory data and provides basic functionality to test all GUI features.
 * 
 * This class allows you to test:
 * - Project display in the main table
 * - Add/Edit/Delete project operations
 * - Settings dialog functionality
 * - Project folder and editor operations (simulated)
 * 
 * @author Project Manager GUI Test
 * @version 1.0
 */
public class TestBackendService {
    
    // In-memory data storage
    private List<Project> projects = new ArrayList<>();
    private List<EditorProfile> editorProfiles = new ArrayList<>();
    private String rootDirectory = System.getProperty("user.home") + "/Projects";
    
    /**
     * Constructor that initializes test data.
     */
    public TestBackendService() {
        initializeTestData();
    }
    
    // ==================== PROJECT MANAGEMENT ====================
    
    /**
     * Returns all projects for display in the GUI.
     * @return List of all projects
     */
    public List<Project> getProjects() {
        return new ArrayList<>(projects);
    }
    
    /**
     * Adds a new project to the test data.
     * @param project The project to add
     */
    public void addProject(Project project) {
        // Assign a new ID if this is a new project
        if (project.getId() == -1) {
            project.setId(generateNewId());
        }
        
        // Set creation date if not set
        if (project.getCreationDate().isEmpty()) {
            project.setCreationDate(java.time.LocalDateTime.now().toString());
        }
        
        // Set last modified date
        project.setLastModifiedDate(java.time.LocalDateTime.now().toString());
        
        projects.add(project);
        System.out.println("Added project: " + project.getName() + " (ID: " + project.getId() + ")");
    }
    
    /**
     * Updates an existing project in the test data.
     * @param project The project to update
     */
    public void updateProject(Project project) {
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getId() == project.getId()) {
                // Update last modified date
                project.setLastModifiedDate(java.time.LocalDateTime.now().toString());
                
                projects.set(i, project);
                System.out.println("Updated project: " + project.getName() + " (ID: " + project.getId() + ")");
                return;
            }
        }
        System.out.println("Warning: Project not found for update: " + project.getName());
    }
    
    /**
     * Deletes a project from the test data.
     * @param project The project to delete
     */
    public void deleteProject(Project project) {
        projects.removeIf(p -> p.getId() == project.getId());
        System.out.println("Deleted project: " + project.getName() + " (ID: " + project.getId() + ")");
    }
    
    // ==================== SETTINGS MANAGEMENT ====================
    
    /**
     * Returns the current root directory.
     * @return Root directory path
     */
    public String getRootDirectory() {
        return rootDirectory;
    }
    
    /**
     * Sets the root directory.
     * @param path New root directory path
     */
    public void setRootDirectory(String path) {
        this.rootDirectory = path;
        System.out.println("Set root directory to: " + path);
    }
    
    /**
     * Returns all editor profiles.
     * @return List of editor profiles
     */
    public List<EditorProfile> getEditorProfiles() {
        return new ArrayList<>(editorProfiles);
    }
    
    /**
     * Adds a new editor profile.
     * @param profile The profile to add
     */
    public void addEditorProfile(EditorProfile profile) {
        editorProfiles.add(profile);
        System.out.println("Added editor profile: " + profile.getName());
    }
    
    /**
     * Updates an existing editor profile.
     * @param profile The profile to update
     */
    public void updateEditorProfile(EditorProfile profile) {
        for (int i = 0; i < editorProfiles.size(); i++) {
            if (editorProfiles.get(i).getName().equals(profile.getName())) {
                editorProfiles.set(i, profile);
                System.out.println("Updated editor profile: " + profile.getName());
                return;
            }
        }
        System.out.println("Warning: Editor profile not found for update: " + profile.getName());
    }
    
    /**
     * Deletes an editor profile.
     * @param profile The profile to delete
     */
    public void deleteEditorProfile(EditorProfile profile) {
        editorProfiles.removeIf(p -> p.getName().equals(profile.getName()));
        System.out.println("Deleted editor profile: " + profile.getName());
    }
    
    // ==================== PROJECT OPERATIONS ====================
    
    /**
     * Simulates opening a project folder.
     * @param project The project whose folder to open
     */
    public void openProjectFolder(Project project) {
        String absolutePath = getAbsoluteProjectPath(project);
        System.out.println("Opening project folder: " + absolutePath);
        System.out.println("(This would open the system file manager in a real implementation)");
    }
    
    /**
     * Simulates opening a project with an editor.
     * @param project The project to open
     * @param editor The editor to use
     */
    public void openProjectWith(Project project, EditorProfile editor) {
        String absolutePath = getAbsoluteProjectPath(project);
        System.out.println("Opening project '" + project.getName() + "' with editor '" + editor.getName() + "'");
        System.out.println("Project path: " + absolutePath);
        System.out.println("Editor command: " + editor.getExecutablePath());
        System.out.println("Editor arguments: " + editor.getCommandLineArgs());
        System.out.println("(This would launch the editor process in a real implementation)");
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Generates a new unique ID for projects.
     * @return New unique ID
     */
    private int generateNewId() {
        return projects.stream()
            .mapToInt(Project::getId)
            .max()
            .orElse(0) + 1;
    }
    
    /**
     * Gets the absolute path for a project.
     * @param project The project
     * @return Absolute path
     */
    private String getAbsoluteProjectPath(Project project) {
        if (rootDirectory == null || rootDirectory.isEmpty()) {
            return project.getRelativePath();
        }
        return rootDirectory + "/" + project.getRelativePath();
    }
    
    /**
     * Initializes test data for demonstration purposes.
     */
    private void initializeTestData() {
        // Add sample projects
        Project project1 = new Project();
        project1.setId(1);
        project1.setName("Sample Web Project");
        project1.setDescription("A sample web development project for testing the GUI");
        project1.setRelativePath("web-project");
        project1.setStatus("In Development");
        project1.setPriority(2);
        project1.setCategory("Web Development");
        project1.setCreationDate("2024-01-15 10:30:00");
        project1.setLastModifiedDate("2024-01-20 14:45:00");
        projects.add(project1);
        
        Project project2 = new Project();
        project2.setId(2);
        project2.setName("Mobile App");
        project2.setDescription("A mobile application project");
        project2.setRelativePath("mobile-app");
        project2.setStatus("Completed");
        project2.setPriority(1);
        project2.setCategory("Mobile Development");
        project2.setCreationDate("2024-01-10 09:15:00");
        project2.setLastModifiedDate("2024-01-25 16:20:00");
        projects.add(project2);
        
        Project project3 = new Project();
        project3.setId(3);
        project3.setName("Data Analysis Tool");
        project3.setDescription("A tool for analyzing large datasets");
        project3.setRelativePath("data-tool");
        project3.setStatus("In Development");
        project3.setPriority(3);
        project3.setCategory("Data Science");
        project3.setCreationDate("2024-01-05 11:00:00");
        project3.setLastModifiedDate("2024-01-18 13:30:00");
        projects.add(project3);
        
        // Add sample editor profiles
        editorProfiles.add(EditorProfile.createVSCodeProfile());
        editorProfiles.add(EditorProfile.createIntelliJProfile());
        editorProfiles.add(EditorProfile.createSublimeTextProfile());
        
        System.out.println("Test data initialized:");
        System.out.println("- " + projects.size() + " sample projects");
        System.out.println("- " + editorProfiles.size() + " sample editor profiles");
        System.out.println("- Root directory: " + rootDirectory);
    }
    
    /**
     * Prints current state for debugging.
     */
    public void printCurrentState() {
        System.out.println("\n=== CURRENT TEST STATE ===");
        System.out.println("Projects (" + projects.size() + "):");
        for (Project p : projects) {
            System.out.println("  - " + p.getName() + " (ID: " + p.getId() + ", Status: " + p.getStatus() + ")");
        }
        System.out.println("Editor Profiles (" + editorProfiles.size() + "):");
        for (EditorProfile ep : editorProfiles) {
            System.out.println("  - " + ep.getName() + " (" + ep.getExecutablePath() + ")");
        }
        System.out.println("Root Directory: " + rootDirectory);
        System.out.println("==========================\n");
    }
} 