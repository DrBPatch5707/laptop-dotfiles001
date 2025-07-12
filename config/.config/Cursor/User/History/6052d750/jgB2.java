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
    
    // ==================== BACKEND INTEGRATION ====================
    protected BackendInterface backend;
    protected List<Project> currentProjects;

    // ==================== CONSTRUCTOR ====================
    public ProjectManagerGUI() {
        this(null);
    }
    
    public ProjectManagerGUI(BackendInterface backend) {
        super("Project Manager");
        this.backend = backend;
        this.currentProjects = backend != null ? backend.getProjects() : List.of();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        initComponents();
        layoutComponents();
        setupEventHandlers();
        
        // Load initial data if backend is available
        if (backend != null) {
            refreshProjectTable(currentProjects);
        }
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
        });

        // Add New Project
        addProjectButton.addActionListener(e -> {
            if (backend == null) {
                JOptionPane.showMessageDialog(this, 
                    "No backend service available. Please initialize the backend first.", 
                    "Backend Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            AddEditProjectDialog dialog = new AddEditProjectDialog(this, new AddEditProjectDialog.ProjectSaveCallback() {
                @Override
                public void onProjectSave(Project project, boolean isEdit) throws Exception {
                    if (isEdit) {
                        backend.updateProject(project);
                    } else {
                        backend.addProject(project);
                    }
                    // Refresh the table
                    currentProjects = backend.getProjects();
                    refreshProjectTable(currentProjects);
                }
            });
            dialog.setVisible(true);
        });

        // Edit Selected Project
        editProjectButton.addActionListener(e -> {
            Project selected = getSelectedProject();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select a project to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (backend == null) {
                JOptionPane.showMessageDialog(this, 
                    "No backend service available. Please initialize the backend first.", 
                    "Backend Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            AddEditProjectDialog dialog = new AddEditProjectDialog(this, selected, new AddEditProjectDialog.ProjectSaveCallback() {
                @Override
                public void onProjectSave(Project project, boolean isEdit) throws Exception {
                    backend.updateProject(project);
                    // Refresh the table
                    currentProjects = backend.getProjects();
                    refreshProjectTable(currentProjects);
                }
            });
            dialog.setVisible(true);
        });

        // Delete Selected Project
        deleteProjectButton.addActionListener(e -> {
            Project selected = getSelectedProject();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select a project to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (backend == null) {
                JOptionPane.showMessageDialog(this, 
                    "No backend service available. Please initialize the backend first.", 
                    "Backend Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete project '" + selected.getName() + "'?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
                
            if (result == JOptionPane.YES_OPTION) {
                try {
                    backend.deleteProject(selected);
                    currentProjects = backend.getProjects();
                    refreshProjectTable(currentProjects);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Error deleting project: " + ex.getMessage(), 
                        "Delete Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Open Project Folder
        openFolderButton.addActionListener(e -> {
            Project selected = getSelectedProject();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select a project to open.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (backend == null) {
                JOptionPane.showMessageDialog(this, 
                    "No backend service available. Please initialize the backend first.", 
                    "Backend Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                backend.openProjectFolder(selected);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error opening project folder: " + ex.getMessage(), 
                    "Open Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Open Project With...
        openWithButton.addActionListener(e -> {
            Project selected = getSelectedProject();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select a project to open.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (backend == null) {
                JOptionPane.showMessageDialog(this, 
                    "No backend service available. Please initialize the backend first.", 
                    "Backend Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get editor profiles and show selection dialog
            List<EditorProfile> editors = backend.getEditorProfiles();
            if (editors.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No editor profiles configured. Please add editors in Settings.", 
                    "No Editors", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Simple editor selection - you might want to create a proper dialog for this
            String[] editorNames = editors.stream().map(EditorProfile::getName).toArray(String[]::new);
            String selectedEditor = (String) JOptionPane.showInputDialog(this, 
                "Select editor to open project with:", 
                "Select Editor", 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                editorNames, 
                editorNames[0]);
                
            if (selectedEditor != null) {
                EditorProfile selectedEditorProfile = editors.stream()
                    .filter(e -> e.getName().equals(selectedEditor))
                    .findFirst()
                    .orElse(null);
                    
                if (selectedEditorProfile != null) {
                    try {
                        backend.openProjectWith(selected, selectedEditorProfile);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, 
                            "Error opening project with editor: " + ex.getMessage(), 
                            "Open Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // Settings
        settingsButton.addActionListener(e -> {
            if (backend == null) {
                JOptionPane.showMessageDialog(this, 
                    "No backend service available. Please initialize the backend first.", 
                    "Backend Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                SettingsDialog dialog = new SettingsDialog(this, backend.getRootDirectory(), backend.getEditorProfiles());
                dialog.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error opening settings: " + ex.getMessage(), 
                    "Settings Error", JOptionPane.ERROR_MESSAGE);
            }
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
        if (row == -1 || row >= currentProjects.size()) return null;
        return currentProjects.get(row);
    }

    // ==================== MAIN METHOD (for testing) ====================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProjectManagerGUI gui = new ProjectManagerGUI();
            gui.setVisible(true);
        });
    }
} 