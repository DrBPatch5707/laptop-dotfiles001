package org.drbpatch;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ProjectManagerGUI extends JFrame {
    private JTable projectTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, viewButton;

    public ProjectManagerGUI() {
        setTitle("Project Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        initComponents();
        loadProjects();
    }

    private void initComponents() {
        String[] columns = {"Name", "Status", "Priority"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        projectTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(projectTable);

        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        viewButton = new JButton("View");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(viewButton);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteSelectedProject());
        viewButton.addActionListener(e -> showViewDialog());
    }

    private void loadProjects() {
        tableModel.setRowCount(0);
        for (Project p : BackendAPI.projects.values()) {
            tableModel.addRow(new Object[]{p.getName(), p.getStatus(), p.getPriorityDisplay()});
        }
    }

    private Project getSelectedProject() {
        int row = projectTable.getSelectedRow();
        if (row == -1) return null;
        // Find the project by matching name, status, and priority (since ID is hidden)
        String name = (String) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 1);
        String priorityDisplay = (String) tableModel.getValueAt(row, 2);
        for (Project p : BackendAPI.projects.values()) {
            if (p.getName().equals(name) && p.getStatus().equals(status) && p.getPriorityDisplay().equals(priorityDisplay)) {
                return p;
            }
        }
        return null;
    }

    private void showAddDialog() {
        ProjectForm form = new ProjectForm(this, null);
        form.setVisible(true);
        if (form.isConfirmed()) {
            Project newProject = form.getProject();
            BackendAPI.addProject(newProject);
            loadProjects();
        }
    }

    private void showEditDialog() {
        Project selected = getSelectedProject();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a project to edit.");
            return;
        }
        ProjectForm form = new ProjectForm(this, selected);
        form.setVisible(true);
        if (form.isConfirmed()) {
            Project updated = form.getProject();
            BackendAPI.editProject(updated);
            loadProjects();
        }
    }

    private void deleteSelectedProject() {
        Project selected = getSelectedProject();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a project to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete project '" + selected.getName() + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            BackendAPI.deleteProject(selected);
            loadProjects();
        }
    }

    private void showViewDialog() {
        Project selected = getSelectedProject();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a project to view.");
            return;
        }
        // Skeleton: Show detailed info in a dialog
        String info = "ID: " + selected.getId() +
                "\nName: " + selected.getName() +
                "\nDescription: " + selected.getDescription() +
                "\nStatus: " + selected.getStatus() +
                "\nPriority: " + selected.getPriorityDisplay() +
                "\nCategory: " + selected.getCategory() +
                "\nCreated: " + selected.getCreationDate() +
                "\nLast Modified: " + selected.getLastModifiedDate() +
                "\nPath: " + selected.getRelativePath();
        JOptionPane.showMessageDialog(this, info, "Project Details", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProjectManagerGUI().setVisible(true));
    }
}

class ProjectForm extends JDialog {
    private JTextField nameField, relativePathField, categoryField;
    private JTextArea descriptionArea;
    private JComboBox<String> statusBox;
    private JComboBox<Integer> priorityBox;
    private boolean confirmed = false;
    private Project project;

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
            project.setName(nameField.getText().trim());
            project.setDescription(descriptionArea.getText().trim());
            project.setRelativePath(relativePathField.getText().trim());
            project.setStatus((String)statusBox.getSelectedItem());
            project.setPriority((Integer)priorityBox.getSelectedItem());
            project.setCategory(categoryField.getText().trim());
            confirmed = true;
            setVisible(false);
        });
        cancel.addActionListener(e -> setVisible(false));
    }

    public boolean isConfirmed() { return confirmed; }
    public Project getProject() { return project; }
} 