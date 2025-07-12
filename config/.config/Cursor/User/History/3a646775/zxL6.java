package org.drbpatch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

public class GUI_Connection implements BackendInterface{

    /**
     * Returns all projects for display in the GUI.
     *
     * @return List of all projects
     */
    @Override
    public List<Project> getProjects() {
        return DataBank.projects;
    }

    /**
     * Adds a new project to the backend storage.
     *
     * @param project The project to add
     * @throws Exception if the operation fails
     */
    @Override
    public void addProject(Project project) throws Exception {
        final String stmt =
                "INSERT INTO projects(" +
                        "name," +
                        "description," +
                        "start_date," +
                        "last_modified_date," +
                        "status," +
                        "category," +
                        "relative_path," +
                        "priority) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement pstmt = Main.connection.prepareStatement(stmt)) {

            pstmt.setString(1, project.getName());
            pstmt.setString(2, project.getDescription());
            pstmt.setString(3, LocalDateTime.now().toString());
            pstmt.setString(4, project.getLastModifiedDate());
            pstmt.setString(5, project.getStatus());
            pstmt.setString(6, project.getCategory());
            pstmt.setString(7, project.getRelativePath());
            pstmt.setInt(8, project.getPriority());


            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("project added successfully...");
            }
        } catch (SQLException e) {
            System.out.println("Error writing to database: " + e.getMessage());
            throw new Exception("Failed to add project to database: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing project in the backend storage.
     *
     * @param project The project to update
     * @throws Exception if the operation fails
     */
    @Override
    public void updateProject(Project project) throws Exception {

    }

    /**
     * Deletes a project from the backend storage.
     *
     * @param project The project to delete
     * @throws Exception if the operation fails
     */
    @Override
    public void deleteProject(Project project) throws Exception {

    }

    /**
     * Returns the current projects root directory.
     *
     * @return Root directory path
     */
    @Override
    public String getRootDirectory() {
        return "";
    }

    /**
     * Sets the projects root directory.
     *
     * @param path New root directory path
     * @throws Exception if the operation fails
     */
    @Override
    public void setRootDirectory(String path) throws Exception {

    }

    /**
     * Returns all editor profiles.
     *
     * @return List of editor profiles
     */
    @Override
    public List<EditorProfile> getEditorProfiles() {
        return List.of();
    }

    /**
     * Adds a new editor profile.
     *
     * @param profile The profile to add
     * @throws Exception if the operation fails
     */
    @Override
    public void addEditorProfile(EditorProfile profile) throws Exception {

    }

    /**
     * Updates an existing editor profile.
     *
     * @param profile The profile to update
     * @throws Exception if the operation fails
     */
    @Override
    public void updateEditorProfile(EditorProfile profile) throws Exception {

    }

    /**
     * Deletes an editor profile.
     *
     * @param profile The profile to delete
     * @throws Exception if the operation fails
     */
    @Override
    public void deleteEditorProfile(EditorProfile profile) throws Exception {

    }

    /**
     * Opens a project folder in the system file manager.
     *
     * @param project The project whose folder to open
     * @throws Exception if the operation fails
     */
    @Override
    public void openProjectFolder(Project project) throws Exception {

    }

    /**
     * Opens a project with the specified editor.
     *
     * @param project The project to open
     * @param editor  The editor to use
     * @throws Exception if the operation fails
     */
    @Override
    public void openProjectWith(Project project, EditorProfile editor) throws Exception {

    }

    /**
     * Initializes the backend service.
     * Called when the application starts.
     *
     * @throws Exception if initialization fails
     */
    @Override
    public void initialize() throws Exception {

    }

    /**
     * Shuts down the backend service.
     * Called when the application is closing.
     *
     * @throws Exception if shutdown fails
     */
    @Override
    public void shutdown() throws Exception {

    }
}
