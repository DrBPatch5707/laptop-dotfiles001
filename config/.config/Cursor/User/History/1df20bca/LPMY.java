package org.drbpatch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PopulateInfo loads project information from the database into the application's in-memory map.
 * Provides SQL query constants and a static method to populate project info.
 */
public class PopulateInfo {
    /** SQL query to select all project fields. */
    public static final String categories =
            "SELECT project_id," +
            "name," +
            "description," +
            "start_date," +
            "last_modified_date," +
            "status," +
            "category," +
            "relative_path," +
            "priority," +
            "dir_exists FROM projects";

    /**
     * Loads all projects from the database and updates the BackendAPI.projects map.
     */
    public static void PopulateProjectInfo() {
        // Clear existing projects to avoid duplicates
        BackendAPI.projects.clear();
        
        try(PreparedStatement pstmt = Main.connection.prepareStatement(categories);
            ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Project project = new Project(rs.getInt("project_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("relative_path"),
                        rs.getString("status"),
                        rs.getString("start_date"),
                        rs.getString("last_modified_date"),
                        rs.getInt("priority"),
                        rs.getString("category"));
                project.setDirExists(rs.getInt("dir_exists") == 1);
                if (!BackendAPI.projects.containsKey(project.getId())) {
                    BackendAPI.projects.put(project.getId(), project);
                }else {
                    BackendAPI.projects.replace(project.getId(), project);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error reading from database: " + e.getMessage());
        }
    }
}
