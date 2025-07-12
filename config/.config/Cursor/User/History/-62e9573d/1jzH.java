package org.drbpatch;

public class Projectmanager extends ProjectManagerGUI{

    private final GUI_Connection backend;

    public Projectmanager(GUI_Connection backend) {
        super();
        this.backend = backend;

        // Load initial data
        refreshProjectTable(backend.getProjects());

        // Set up event handlers
        setupEventHandlers();
    }

    protected void setupEventHandlers() {
        // Table selection listener
        projectTable.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = projectTable.getSelectedRow() != -1;
            editProjectButton.setEnabled(selected);
            deleteProjectButton.setEnabled(selected);
            openFolderButton.setEnabled(selected);
            openWithButton.setEnabled(selected);
            // Optionally: onProjectSelected callback
            // if (selected) onProjectSelected(getSelectedProject());
        });

        // Add New Project
        addProjectButton.addActionListener(e -> {
            AddEditProjectDialog dialog = new AddEditProjectDialog(this, (project, isEdit) -> {
                try {
                    backend.addProject(project);
                    // Refresh the table after adding a project
                    refreshProjectTable(backend.getProjects());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
            dialog.setVisible(true);
        });

        // Edit Selected Project
        editProjectButton.addActionListener(e -> {
            Project selected = getSelectedProject();
            if (selected != null) {
                // Example: Show AddEditProjectDialog in edit mode
                // new AddEditProjectDialog(this, selected, (project, isEdit) -> backend.editProject(project)).setVisible(true);
            }
        });

        // Delete Selected Project
        deleteProjectButton.addActionListener(e -> {
            Project selected = getSelectedProject();
            if (selected != null) {
                // Example: backend.deleteProject(selected);
            }
        });

        // Open Project Folder
        openFolderButton.addActionListener(e -> {
            Project selected = getSelectedProject();
            if (selected != null) {
                // Example: backend.openProjectFolder(selected);
            }
        });

        // Open Project With...
        openWithButton.addActionListener(e -> {
            Project selected = getSelectedProject();
            if (selected != null) {
                // Example: backend.openProjectWith(selected);
            }
        });

        // Settings
        settingsButton.addActionListener(e -> {
            // Example: new SettingsDialog(this, backend.getRootDir(), backend.getEditorProfiles()).setVisible(true);
        });
    }


}
