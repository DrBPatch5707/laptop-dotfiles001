package gui.org.drbpatch;

import java.util.List;

/**
 * Interface defining the contract between the Project Manager GUI and backend services.
 * 
 * This interface provides all the methods that the GUI needs to interact with the backend.
 * Your backend implementation should implement this interface to provide the actual
 * data persistence, file operations, and system integration.
 * 
 * @author Project Manager GUI
 * @version 1.0
 */
public interface BackendInterface {
    
    // ==================== PROJECT MANAGEMENT ====================
    
    /**
     * Returns all projects for display in the GUI.
     * @return List of all projects
     */
    List<Project> getProjects();
    
    /**
     * Adds a new project to the backend storage.
     * @param project The project to add
     * @throws Exception if the operation fails
     */
    void addProject(Project project) throws Exception;
    
    /**
     * Updates an existing project in the backend storage.
     * @param project The project to update
     * @throws Exception if the operation fails
     */
    void updateProject(Project project) throws Exception;
    
    /**
     * Deletes a project from the backend storage.
     * @param project The project to delete
     * @throws Exception if the operation fails
     */
    void deleteProject(Project project) throws Exception;
    
    // ==================== SETTINGS MANAGEMENT ====================
    
    /**
     * Returns the current projects root directory.
     * @return Root directory path
     */
    String getRootDirectory();
    
    /**
     * Sets the projects root directory.
     * @param path New root directory path
     * @throws Exception if the operation fails
     */
    void setRootDirectory(String path) throws Exception;
    
    /**
     * Returns all editor profiles.
     * @return List of editor profiles
     */
    List<EditorProfile> getEditorProfiles();
    
    /**
     * Adds a new editor profile.
     * @param profile The profile to add
     * @throws Exception if the operation fails
     */
    void addEditorProfile(EditorProfile profile) throws Exception;
    
    /**
     * Updates an existing editor profile.
     * @param profile The profile to update
     * @throws Exception if the operation fails
     */
    void updateEditorProfile(EditorProfile profile) throws Exception;
    
    /**
     * Deletes an editor profile.
     * @param profile The profile to delete
     * @throws Exception if the operation fails
     */
    void deleteEditorProfile(EditorProfile profile) throws Exception;
    
    // ==================== PROJECT OPERATIONS ====================
    
    /**
     * Opens a project folder in the system file manager.
     * @param project The project whose folder to open
     * @throws Exception if the operation fails
     */
    void openProjectFolder(Project project) throws Exception;
    
    /**
     * Opens a project with the specified editor.
     * @param project The project to open
     * @param editor The editor to use
     * @throws Exception if the operation fails
     */
    void openProjectWith(Project project, EditorProfile editor) throws Exception;
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Initializes the backend service.
     * Called when the application starts.
     * @throws Exception if initialization fails
     */
    void initialize() throws Exception;
    
    /**
     * Shuts down the backend service.
     * Called when the application is closing.
     * @throws Exception if shutdown fails
     */
    void shutdown() throws Exception;
} 