package org.drbpatch;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog form for creating or editing a Project.
 * Provides fields for all editable project properties and validation logic.
 * Used by ProjectManagerGUI for Add/Edit operations.
 */
public class ProjectForm extends JDialog {
    private JTextField nameField, relativePathField, categoryField;
    private JTextArea descriptionArea;
    private JComboBox<String> statusBox;
    private JComboBox<Integer> priorityBox;
    private boolean confirmed = false;
    private Project project;
    private JCheckBox dirExistsCheckBox;
    private boolean dirExists = false;
    private JLabel lastModifiedLabel;
    private JButton refreshLastModifiedButton;

    public ProjectForm(JFrame parent, Project projectToEdit) {
        super(parent, true);
        setTitle(projectToEdit == null ? "Add Project" : "Edit Project");
        setSize(400, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0,2,5,5));
        nameField = new JTextField();
        descriptionArea = new JTextArea(3, 20);
        relativePathField = new JTextField();
        statusBox = new JComboBox<>(new String[]{"In Development", "Completed", "Depreciated"});
        priorityBox = new JComboBox<>(new Integer[]{1,2,3,4,5});
        categoryField = new JTextField();

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(new JScrollPane(descriptionArea));
        formPanel.add(new JLabel("Relative Path:"));
        formPanel.add(relativePathField);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusBox);
        formPanel.add(new JLabel("Priority (1=High, 5=Low):"));
        formPanel.add(priorityBox);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryField);

        // Add checkbox for directory already exists
        dirExistsCheckBox = new JCheckBox("Directory already exists (do not create)");
        formPanel.add(new JLabel()); // empty label for alignment
        formPanel.add(dirExistsCheckBox);

        // Add last modified date display and refresh button
        if (projectToEdit != null) {
            formPanel.add(new JLabel("Last Modified:"));
            lastModifiedLabel = new JLabel(projectToEdit.getLastModifiedDate().isEmpty() ? "Not set" : projectToEdit.getLastModifiedDate());
            lastModifiedLabel.setBorder(BorderFactory.createEtchedBorder());
            formPanel.add(lastModifiedLabel);
            
            refreshLastModifiedButton = new JButton("Refresh Last Modified");
            refreshLastModifiedButton.addActionListener(e -> refreshLastModifiedDate());
            formPanel.add(new JLabel()); // empty label for alignment
            formPanel.add(refreshLastModifiedButton);
        }

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        add(buttonPanel, BorderLayout.SOUTH);

        if (projectToEdit != null) {
            nameField.setText(projectToEdit.getName());
            descriptionArea.setText(projectToEdit.getDescription());
            relativePathField.setText(projectToEdit.getRelativePath());
            statusBox.setSelectedItem(projectToEdit.getStatus());
            priorityBox.setSelectedItem(projectToEdit.getPriority());
            categoryField.setText(projectToEdit.getCategory());
            this.project = projectToEdit.copy();
        } else {
            this.project = new Project();
        }

        ok.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required.");
                return;
            }
            
            String relativePath = relativePathField.getText().trim();
            if (relativePath.startsWith(".") || relativePath.startsWith("./")) {
                JOptionPane.showMessageDialog(this, 
                    "Relative path cannot start with '.' or './' as this may cause IDEs to open the project as a file instead of a directory.\n\n" +
                    "Please use a path without the leading dot (e.g., 'myproject' instead of './myproject').", 
                    "Invalid Path", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            project.setName(nameField.getText().trim());
            project.setDescription(descriptionArea.getText().trim());
            project.setRelativePath(relativePath);
            project.setStatus((String)statusBox.getSelectedItem());
            project.setPriority((Integer)priorityBox.getSelectedItem());
            project.setCategory(categoryField.getText().trim());
            project.setDirExists(dirExistsCheckBox.isSelected());
            dirExists = dirExistsCheckBox.isSelected();
            confirmed = true;
            setVisible(false);
        });
        cancel.addActionListener(e -> setVisible(false));
    }

    /**
     * @return true if the user confirmed the dialog, false otherwise
     */
    public boolean isConfirmed() { return confirmed; }
    /**
     * @return the Project object with the entered/edited data
     */
    public Project getProject() { return project; }
    /**
     * @return true if the directory exists checkbox is checked
     */
    public boolean isDirExistsChecked() { return dirExists; }

    private void checkDirectoryExists() {
        String relPath = relativePathField.getText().trim();
        if (!relPath.isEmpty() && BackendAPI.root != null) {
            boolean exists = java.nio.file.Files.exists(java.nio.file.Paths.get(BackendAPI.root, relPath));
            dirExistsCheckBox.setSelected(exists);
            dirExists = exists;
        }
    }

    private void refreshLastModifiedDate() {
        if (project != null) {
            BackendAPI.updateProjectLastModified(project);
            lastModifiedLabel.setText(project.getLastModifiedDate());
        }
    }
} 