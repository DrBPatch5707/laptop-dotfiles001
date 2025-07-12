package gui.org.drbpatch;

import java.util.Objects;

/**
 * Data Transfer Object (DTO) representing a project in the Project Manager application.
 * This class serves as a bridge between the GUI and backend services.
 * 
 * <p>All fields are mutable to support both creation and editing operations.
 * The backend is responsible for managing the actual data persistence and validation.</p>
 * 
 * <p>Usage in GUI:
 * - Used to populate JTable rows in ProjectManagerGUI
 * - Passed to AddEditProjectDialog for editing existing projects
 * - Returned from dialog callbacks to the main GUI
 * - Used as parameters for backend service method calls</p>
 * 
 * @author Project Manager GUI
 * @version 1.0
 */
public class Project {
    
    /**
     * Unique identifier for the project.
     * Set by the backend when creating new projects.
     * Used for database operations and project identification.
     */
    private int id;
    
    /**
     * Human-readable name of the project.
     * Required field - should not be null or empty.
     * Used for display in GUI tables and dialogs.
     */
    private String name;
    
    /**
     * Detailed description of the project.
     * Can be null or empty for simple projects.
     * Supports multi-line text in GUI text areas.
     */
    private String description;
    
    /**
     * Relative path from the projects root directory to this project's folder.
     * Used by the backend to construct absolute paths and manage project locations.
     * Should be normalized (use forward slashes, no leading slash).
     */
    private String relativePath;
    
    /**
     * Current status of the project.
     * Predefined values: "In Development", "Completed", "Depreciated"
     * Used for filtering and sorting in the GUI.
     */
    private String status;
    
    /**
     * Date when the project was created.
     * Format: ISO 8601 string (YYYY-MM-DD HH:MM:SS)
     * Managed by the backend, typically read-only in GUI.
     */
    private String creationDate;
    
    /**
     * Date when the project was last modified.
     * Format: ISO 8601 string (YYYY-MM-DD HH:MM:SS)
     * Updated by the backend when project data changes.
     */
    private String lastModifiedDate;
    
    /**
     * Priority level of the project (1-5, where 1 is highest priority).
     * Used for sorting and visual indicators in the GUI.
     * Alternative text values: "High" (1), "Medium" (3), "Low" (5)
     */
    private int priority;
    
    /**
     * Category or tag for grouping related projects.
     * Can be null or empty.
     * Used for filtering and organization in the GUI.
     */
    private String category;
    
    /**
     * Default constructor for creating new projects.
     * All fields are initialized to default values.
     * The backend should set appropriate defaults (e.g., current date, default status).
     */
    public Project() {
        this.id = -1; // Indicates a new project not yet saved
        this.name = "";
        this.description = "";
        this.relativePath = "";
        this.status = "In Development";
        this.creationDate = "";
        this.lastModifiedDate = "";
        this.priority = 3; // Medium priority
        this.category = "";
    }
    
    /**
     * Constructor for creating projects with initial data.
     * Typically used by the backend when loading existing projects.
     * 
     * @param id Unique project identifier
     * @param name Project name
     * @param description Project description
     * @param relativePath Relative path to project folder
     * @param status Current project status
     * @param creationDate Project creation date
     * @param lastModifiedDate Last modification date
     * @param priority Project priority (1-5)
     * @param category Project category
     */
    public Project(int id, String name, String description, String relativePath, 
                   String status, String creationDate, String lastModifiedDate, 
                   int priority, String category) {
        this.id = id;
        this.name = name != null ? name : "";
        this.description = description != null ? description : "";
        this.relativePath = relativePath != null ? relativePath : "";
        this.status = status != null ? status : "In Development";
        this.creationDate = creationDate != null ? creationDate : "";
        this.lastModifiedDate = lastModifiedDate != null ? lastModifiedDate : "";
        this.priority = priority;
        this.category = category != null ? category : "";
    }
    
    // ==================== GETTERS ====================
    
    /**
     * Gets the unique project identifier.
     * @return Project ID, or -1 if this is a new unsaved project
     */
    public int getId() {
        return id;
    }
    
    /**
     * Gets the project name.
     * @return Project name, never null (empty string if not set)
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the project description.
     * @return Project description, never null (empty string if not set)
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the relative path to the project folder.
     * @return Relative path, never null (empty string if not set)
     */
    public String getRelativePath() {
        return relativePath;
    }
    
    /**
     * Gets the current project status.
     * @return Project status, never null
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Gets the project creation date.
     * @return Creation date as ISO 8601 string, never null (empty string if not set)
     */
    public String getCreationDate() {
        return creationDate;
    }
    
    /**
     * Gets the last modification date.
     * @return Last modified date as ISO 8601 string, never null (empty string if not set)
     */
    public String getLastModifiedDate() {
        return lastModifiedDate;
    }
    
    /**
     * Gets the project priority level.
     * @return Priority (1-5, where 1 is highest)
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Gets the project category.
     * @return Project category, never null (empty string if not set)
     */
    public String getCategory() {
        return category;
    }
    
    // ==================== SETTERS ====================
    
    /**
     * Sets the project identifier.
     * Should only be called by the backend when saving new projects.
     * 
     * @param id Unique project identifier
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Sets the project name.
     * @param name Project name (null values are converted to empty string)
     */
    public void setName(String name) {
        this.name = name != null ? name : "";
    }
    
    /**
     * Sets the project description.
     * @param description Project description (null values are converted to empty string)
     */
    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }
    
    /**
     * Sets the relative path to the project folder.
     * @param relativePath Relative path (null values are converted to empty string)
     */
    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath != null ? relativePath : "";
    }
    
    /**
     * Sets the project status.
     * @param status Project status (null values default to "In Development")
     */
    public void setStatus(String status) {
        this.status = status != null ? status : "In Development";
    }
    
    /**
     * Sets the creation date.
     * @param creationDate Creation date as ISO 8601 string (null values are converted to empty string)
     */
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate != null ? creationDate : "";
    }
    
    /**
     * Sets the last modification date.
     * @param lastModifiedDate Last modified date as ISO 8601 string (null values are converted to empty string)
     */
    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate != null ? lastModifiedDate : "";
    }
    
    /**
     * Sets the project priority.
     * @param priority Priority level (1-5, where 1 is highest)
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    /**
     * Sets the project category.
     * @param category Project category (null values are converted to empty string)
     */
    public void setCategory(String category) {
        this.category = category != null ? category : "";
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Checks if this project is new (not yet saved to the backend).
     * @return true if the project ID is -1, false otherwise
     */
    public boolean isNew() {
        return id == -1;
    }
    
    /**
     * Gets a display-friendly priority string.
     * @return "High" for priority 1-2, "Medium" for 3, "Low" for 4-5
     */
    public String getPriorityDisplay() {
        if (priority <= 2) return "High";
        if (priority == 3) return "Medium";
        return "Low";
    }
    
    /**
     * Creates a copy of this project.
     * Useful for editing operations where you want to preserve the original.
     * 
     * @return A new Project instance with the same data
     */
    public Project copy() {
        return new Project(id, name, description, relativePath, status, 
                          creationDate, lastModifiedDate, priority, category);
    }
    
    // ==================== OBJECT METHODS ====================
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Project project = (Project) obj;
        return id == project.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", priority=" + priority +
                ", category='" + category + '\'' +
                '}';
    }
} 