package org.drbpatch;

import java.util.LinkedList;
import java.util.List;

public class BackendAPI {
    //read and update/edit can be done through the projects list
    public static List<Project> projects = new LinkedList<>();
    //region setting variables
    public static String root;

    //endregion

    public static boolean addProject(Project project) {
        return DbConnection.addProject(project);
    }
    public static boolean deleteProject(Project project) {
       return DbConnection.deleteProject(project);
    }

    // Skeleton: Edit a project by ID (replace info in the list)
    public static boolean editProject(Project updatedProject) {
        // TODO: Implement actual update logic
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getId() == updatedProject.getId()) {
                projects.set(i, updatedProject);
                return true;
            }
        }
        return false;
    }

    // Skeleton: Get a project by ID
    public static Project getProjectById(int id) {
        // TODO: Implement actual fetch logic
        for (Project p : projects) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    //if more info or actions are required
    // please add additional skeleton functions and variables here
    // I will write implementation for them later.







}
