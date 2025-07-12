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
        String[] columns = {"ID", "Name", "Status", "Priority"};
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
        List<Project> projects = BackendAPI.projects;
        for (Project p : projects) {
            tableModel.addRow(new Object[]{p.getId(), p.getName(), p.getStatus(), p.getPriorityDisplay()});
        }
    }

    private Project getSelectedProject() {
        int row = projectTable.getSelectedRow();
        if (row == -1) return null;
        int id = (int) tableModel.getValueAt(row, 0);
        return BackendAPI.getProjectById(id);
    }

    private void showAddDialog() {
        // Skeleton: Show dialog to add a project
        JOptionPane.showMessageDialog(this, "Add Project dialog (to be implemented)");
        // On success: BackendAPI.addProject(newProject); loadProjects();
    }

    private void showEditDialog() {
        Project selected = getSelectedProject();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a project to edit.");
            return;
        }
        // Skeleton: Show dialog to edit project
        JOptionPane.showMessageDialog(this, "Edit Project dialog (to be implemented) for: " + selected.getName());
        // On success: BackendAPI.editProject(updatedProject); loadProjects();
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