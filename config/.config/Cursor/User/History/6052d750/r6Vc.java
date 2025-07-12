package gui.org.drbpatch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Main application window for the Project Manager.
 * <p>
 * Provides a table view of projects and a control panel with all required actions.
 * All backend logic (database, file I/O, launching, etc.) is to be implemented separately.
 * This class is responsible for UI structure, event listener placeholders, and data refresh.
 *
 * @author Project Manager GUI
 * @version 1.0
 */
public class ProjectManagerGUI extends JFrame {
    // ==================== UI COMPONENTS ====================
    protected JTable projectTable;
    protected DefaultTableModel projectTableModel;
    protected JButton addProjectButton;
    protected JButton editProjectButton;
    protected JButton deleteProjectButton;
    protected JButton openFolderButton;
    protected JButton openWithButton;
    protected JButton settingsButton;

    // ==================== CONSTRUCTOR ====================
    public ProjectManagerGUI() {
        super("Project Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        initComponents();
        layoutComponents();
        setupEventHandlers();
    }

    // ==================== INITIALIZATION ====================
    private void initComponents() {
        // Table model with read-only columns
        projectTableModel = new DefaultTableModel(
                new Object[]{"Name", "Status", "Priority", "Category"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        projectTable = new JTable(projectTableModel);
        projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectTable.setFillsViewportHeight(true);

        // Buttons
        addProjectButton = new JButton("Add New Project");
        editProjectButton = new JButton("Edit Selected Project");
        deleteProjectButton = new JButton("Delete Selected Project");
        openFolderButton = new JButton("Open Project Folder");
        openWithButton = new JButton("Open Project With...");
        settingsButton = new JButton("Settings");

        // Initially disable buttons that require a selection
        editProjectButton.setEnabled(false);
        deleteProjectButton.setEnabled(false);
        openFolderButton.setEnabled(false);
        openWithButton.setEnabled(false);
    }

    private void layoutComponents() {
        // Table in center
        JScrollPane tableScroll = new JScrollPane(projectTable);
        add(tableScroll, BorderLayout.CENTER);

        // Control panel (vertical box)
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        controlPanel.add(addProjectButton);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(editProjectButton);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(deleteProjectButton);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(openFolderButton);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(openWithButton);
        controlPanel.add(Box.createVerticalStrut(16));
        controlPanel.add(settingsButton);

        add(controlPanel, BorderLayout.EAST);
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
            // Example: Show AddEditProjectDialog in add mode
            // new AddEditProjectDialog(this, (project, isEdit) -> backend.addProject(project)).setVisible(true);
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

    // ==================== PUBLIC METHODS ====================
    /**
     * Refreshes the project table with a new list of projects.
     *
     * @param projects List of Project objects to display
     */
    public void refreshProjectTable(List<Project> projects) {
        projectTableModel.setRowCount(0); // Clear table
        if (projects != null) {
            for (Project p : projects) {
                projectTableModel.addRow(new Object[]{
                        p.getName(),
                        p.getStatus(),
                        p.getPriorityDisplay(),
                        p.getCategory()
                });
            }
        }
    }

    /**
     * Returns the currently selected project, or null if none is selected.
     *
     * @return Selected Project object, or null
     */
    public Project getSelectedProject() {
        int row = projectTable.getSelectedRow();
        if (row == -1) return null;
        // This method should be adapted to retrieve the full Project object from your backend or model
        // For now, returns null as a placeholder
        return null;
    }

    // ==================== MAIN METHOD (for testing) ====================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProjectManagerGUI gui = new ProjectManagerGUI();
            gui.setVisible(true);
        });
    }
} 