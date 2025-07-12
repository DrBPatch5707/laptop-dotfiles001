package org.drbpatch;

import java.sql.*;
import java.time.LocalDateTime;


public class DbConnection {
    private static final String DB_URL = "jdbc:sqlite:projects.db";
    public static Connection connect() {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Database connection successful...");
        }catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver not found: " + e.getMessage());
        }
        return connection;
    }
    public static void close(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Database connection successfully closed...");
            }
        }catch (SQLException e) {
            System.out.println("Error closing database connection: " + e.getMessage());
        }
    }

    public static void createProjectsTable(Connection connection) {
        // SQL statement to create the projects table
        // The "IF NOT EXISTS" clause is crucial to prevent errors if the table already exists.
        String sql = "CREATE TABLE IF NOT EXISTS projects (" +
                "project_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "description TEXT," +
                "start_date TEXT," +
                "last_modified_date TEXT," +
                "status TEXT," +
                "category TEXT," +
                "relative_path TEXT," +
                "priority INTEGER," +
                "dir_exists INTEGER DEFAULT 0" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'projects' created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    public static boolean addProject(Project project) {
        final String stmt =
                "INSERT INTO projects(" +
                        "name," +
                        "description," +
                        "start_date," +
                        "last_modified_date," +
                        "status," +
                        "category," +
                        "relative_path," +
                        "priority," +
                        "dir_exists) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = Main.connection.prepareStatement(stmt)) {

            pstmt.setString(1, project.getName());
            pstmt.setString(2, project.getDescription());
            pstmt.setString(3, LocalDateTime.now().toString());
            pstmt.setString(4, project.getLastModifiedDate());
            pstmt.setString(5, project.getStatus());
            pstmt.setString(6, project.getCategory());
            pstmt.setString(7, project.getRelativePath());
            pstmt.setInt(8, project.getPriority());
            pstmt.setInt(9, project.isDirExists() ? 1 : 0);


            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0 && !BackendAPI.projects.containsKey(project.getId())){
                System.out.println("project added successfully...");
                //TODO:add the method of adding the new prodject to the filesystem
                // Obviously not right now because I don't want to have a bunch of random
                // test project directories in my file system.
                BackendAPI.projects.put(project.getId(), project);
            }
            else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error writing to database: " + e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean deleteProject(Project project) {
        String stmt = "DELETE FROM projects WHERE project_id = " + project.getId();
        try(PreparedStatement pstmt = Main.connection.prepareStatement(stmt)) {
            if (BackendAPI.projects.containsKey(project.getId()))
                BackendAPI.projects.remove(project.getId());
            if (pstmt.executeUpdate() == 0) {
             System.out.println("error did not delete project...");
             return false;
            }
        }catch (SQLException e) {
            System.out.println("error with deleting project...");
            return false;
        }
    return true;
    }
}
