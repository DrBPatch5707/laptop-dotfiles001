package org.drbpatch;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.ListSelectionModel;


/**
 * ProjectManagerGUI is the main Swing-based GUI for managing projects.
 * Provides table view, dialogs for CRUD operations, and menu integration.
 */
public class ProjectManagerGUI extends JFrame {
    private JTable projectTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, viewButton;
    private JButton accessButton;
    private JMenuBar menuBar;
    private JMenu settingsMenu;
    private JMenuItem setRootMenuItem;
    private JButton settingsButton;
    private JButton notificationsButton;

    public ProjectManagerGUI() {
        setTitle("Project Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        initMenu();
        initComponents();
        loadProjects();
        
        // Perform filesystem sync after GUI is created so dialogs can be displayed
        javax.swing.SwingUtilities.invokeLater(() -> {
            ProjectSync.performFilesystemSync();
        });
    }

    //region GUI Initialization
    /**
     * Initializes the menu bar and its actions.
     */
    private void initMenu() {
        menuBar = new JMenuBar();
        settingsButton = new JButton("Settings");
        settingsButton.setBorder(BorderFactory.createEtchedBorder());
        settingsButton.addActionListener(e -> showSettingsDialog());
        menuBar.add(settingsButton);
        
        notificationsButton = new JButton("Notifications");
        notificationsButton.setBorder(BorderFactory.createEtchedBorder());
        notificationsButton.addActionListener(e -> showNotificationsDialog());
        menuBar.add(notificationsButton);
        
        setJMenuBar(menuBar);
    }

    private void showSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(this);
        dialog.setVisible(true);
    }

    private void showSetRootDialog() {
        boolean success = BackendAPI.promptForRoot();
        if (success) {
            showDialog("Root directory set to: " + BackendAPI.root, "Notice", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showDialog("Invalid directory or operation cancelled.", "Notice", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Initializes the main GUI components and layout.
     */
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
        accessButton = new JButton("Access");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(accessButton);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteSelectedProject());
        viewButton.addActionListener(e -> showViewDialog());
        accessButton.addActionListener(e -> showAccessDialog());
    }
    //endregion

    //region Project Table Operations
    /**
     * Loads all projects into the table.
     */
    private void loadProjects() {
        tableModel.setRowCount(0);
        for (Project p : BackendAPI.projects.values()) {
            tableModel.addRow(new Object[]{p.getName(), p.getStatus(), p.getPriorityDisplay()});
        }
    }

    /**
     * Gets the currently selected project from the table.
     * @return The selected Project, or null if none selected
     */
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
    //endregion

    //region Dialogs
    /**
     * Shows the Add Project dialog.
     */
    private void showAddDialog() {
        ProjectForm form = new ProjectForm(this, null);
        form.setVisible(true);
        if (form.isConfirmed()) {
            Project newProject = form.getProject();
            boolean dirExists = form.isDirExistsChecked();
            BackendAPI.addProject(newProject, dirExists);
            loadProjects();
            ProjectManagerGUI.reloadProjectsInAllGUIs();
        }
    }

    /**
     * Shows the Edit Project dialog for the selected project.
     */
    private void showEditDialog() {
        Project selected = getSelectedProject();
        if (selected == null) {
            showDialog("Select a project to edit.", "No Project Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ProjectForm form = new ProjectForm(this, selected);
        form.setVisible(true);
        if (form.isConfirmed()) {
            Project updated = form.getProject();
            BackendAPI.editProject(updated);
            loadProjects();
            ProjectManagerGUI.reloadProjectsInAllGUIs();
        }
    }

    /**
     * Shows the View Project dialog for the selected project.
     */
    private void showViewDialog() {
        Project selected = getSelectedProject();
        if (selected == null) {
            showDialog("Select a project to view.", "No Project Selected", JOptionPane.WARNING_MESSAGE);
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
        showDialog(info, "Project Details", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows the Access Project dialog for the selected project.
     */
    private void showAccessDialog() {
        Project selected = getSelectedProject();
        if (selected == null) {
            showDialog("Select a project to access.", "No Project Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        java.util.Map<String, IDE> ides = BackendAPI.getIdes();
        // Filter out the default IDE
        java.util.List<IDE> realIdes = new java.util.ArrayList<>();
        for (IDE ide : ides.values()) {
            if (!"default".equals(ide.getId())) {
                realIdes.add(ide);
            }
        }
        if (realIdes.isEmpty()) {
            showDialog("No IDEs configured.\n\nPlease go to Settings to add and configure an IDE before accessing projects.", "No IDEs Available", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] ideNames = realIdes.stream().map(IDE::getName).toArray(String[]::new);
        String chosen = (String) JOptionPane.showInputDialog(
                this,
                "Select IDE to open project:",
                "Access Project",
                JOptionPane.PLAIN_MESSAGE,
                null,
                ideNames,
                ideNames[0]
        );
        if (chosen == null) return;
        IDE selectedIDE = realIdes.stream().filter(ide -> ide.getName().equals(chosen)).findFirst().orElse(null);
        if (selectedIDE == null) {
            showDialog("IDE not found.", "Notice", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean result = BackendAPI.accessProject(selected, selectedIDE);
        if (result) {
            showDialog("Project opened in " + selectedIDE.getName() + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showDialog("Failed to open project in " + selectedIDE.getName() + ".", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Shows the Notifications dialog.
     */
    private void showNotificationsDialog() {
        NotificationsDialog dialog = new NotificationsDialog(this);
        dialog.setVisible(true);
    }
    //endregion

    //region Project Actions
    /**
     * Deletes the selected project after confirmation.
     */
    private void deleteSelectedProject() {
        Project selected = getSelectedProject();
        if (selected == null) {
            showDialog("Select a project to delete.", "No Project Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete project '" + selected.getName() + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            BackendAPI.deleteProject(selected);
            loadProjects();
            ProjectManagerGUI.reloadProjectsInAllGUIs();
        }
    }
    //endregion

    //region Static Methods
    /**
     * Reloads projects in all open GUIs.
     */
    public static void reloadProjectsInAllGUIs() {
        // Find all open ProjectManagerGUI windows and reload their projects
        for (Frame frame : Frame.getFrames()) {
            if (frame instanceof ProjectManagerGUI) {
                ((ProjectManagerGUI) frame).loadProjects();
            }
        }
    }
    //endregion

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProjectManagerGUI().setVisible(true));
    }

    public static String promptForRoot(Component parent, String currentRoot) {
        return JOptionPane.showInputDialog(parent, "Enter new root directory:", currentRoot);
    }

    // Utility method for consistent dialogs
    private void showDialog(String message, String title, int messageType) {
        showDialog(message, title, messageType);
    }
}

class NotificationsDialog extends JDialog {
    private ModernTablePanel tablePanel;
    private DefaultTableModel tableModel;

    public NotificationsDialog(JFrame parent) {
        super(parent, "Filesystem Notifications", true);
        setSize(700, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        initComponents();
        loadNotifications();
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("Filesystem Notifications");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Type", "Message"};
        tablePanel = new ModernTablePanel(columns, false, true);
        tableModel = tablePanel.getTableModel();
        JTable table = tablePanel.getTable();
        table.setDefaultRenderer(Object.class, new NotificationTableCellRenderer());
        add(tablePanel, BorderLayout.CENTER);

        // Toolbar actions
        if (tablePanel.getRefreshButton() != null)
            tablePanel.getRefreshButton().addActionListener(e -> loadNotifications());
        if (tablePanel.getClearButton() != null)
            tablePanel.getClearButton().addActionListener(e -> {
                tableModel.setRowCount(0);
                BackendAPI.clearNotifications();
            });
        if (tablePanel.getExportButton() != null)
            tablePanel.getExportButton().addActionListener(e -> exportNotifications());
        if (tablePanel.getFilterButton() != null)
            tablePanel.getFilterButton().addActionListener(e -> filterNotifications());

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadNotifications() {
        tableModel.setRowCount(0);
        java.util.List<String> notifications = BackendAPI.getNotifications();
        if (notifications.isEmpty()) {
            tableModel.addRow(new Object[]{"Info", "No filesystem changes detected. All projects are properly registered in the database."});
        } else {
            for (String notification : notifications) {
                tableModel.addRow(new Object[]{"Warning", notification});
            }
        }
    }

    private void exportNotifications() {
        // Simple export to text file (future: add file chooser)
        try {
            java.io.File file = new java.io.File("notifications_export.txt");
            java.io.PrintWriter pw = new java.io.PrintWriter(file);
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                pw.println(tableModel.getValueAt(i, 0) + ": " + tableModel.getValueAt(i, 1));
            }
            pw.close();
            JOptionPane.showMessageDialog(this, "Notifications exported to " + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to export notifications: " + ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterNotifications() {
        // Placeholder for filter dialog (future: implement real filter UI)
        JOptionPane.showMessageDialog(this, "Filtering not yet implemented.", "Filter", JOptionPane.INFORMATION_MESSAGE);
    }

    // Custom cell renderer for coloring rows by type
    static class NotificationTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String type = (String) table.getModel().getValueAt(row, 0);
            if (type.equalsIgnoreCase("Warning")) {
                c.setForeground(new Color(180, 120, 0));
            } else if (type.equalsIgnoreCase("Error")) {
                c.setForeground(Color.RED);
            } else {
                c.setForeground(Color.DARK_GRAY);
            }
            return c;
        }
    }
}


class SettingsDialog extends JDialog {
    // Utility method for consistent dialogs
    private void showDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }
    private void showDialog(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    private int showConfirmDialog(String message, String title, int optionType) {
        return JOptionPane.showConfirmDialog(this, message, title, optionType);
    }
    private int showConfirmDialog(String message, String title, int optionType, int messageType) {
        return JOptionPane.showConfirmDialog(this, message, title, optionType, messageType);
    }
    private JTabbedPane tabbedPane;
    private JLabel currentRootLabel;
    private JButton changeRootButton;
    private DefaultListModel<String> ideListModel;
    private JList<String> ideList;

    public SettingsDialog(JFrame parent) {
        super(parent, "Settings", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        initComponents();
        loadCurrentSettings();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        // Root Settings Tab
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel rootInfoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        rootInfoPanel.add(new JLabel("Current Root Directory:"));
        currentRootLabel = new JLabel(BackendAPI.root != null ? BackendAPI.root : "Not set");
        currentRootLabel.setBorder(BorderFactory.createEtchedBorder());
        rootInfoPanel.add(currentRootLabel);
        
        changeRootButton = new JButton("Change Root Directory");
        changeRootButton.addActionListener(e -> changeRoot());
        
        // Remove reset button from root tab
        JPanel resetPanel = new JPanel(new BorderLayout());
        resetPanel.add(changeRootButton, BorderLayout.CENTER);
        rootPanel.add(rootInfoPanel, BorderLayout.CENTER);
        rootPanel.add(resetPanel, BorderLayout.SOUTH);
        
        // IDE Configuration Tab
        JPanel idePanel = new JPanel(new BorderLayout());
        idePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] ideColumns = {"ID", "Name", "Path", "Arguments"};
        ModernTablePanel ideTablePanel = new ModernTablePanel(ideColumns, true, false);
        JTable ideTable = ideTablePanel.getTable();
        DefaultTableModel ideTableModel = ideTablePanel.getTableModel();
        
        // Populate table with IDEs
        java.util.Map<String, IDE> ides = BackendAPI.getIdes();
        for (IDE ide : ides.values()) {
            ideTableModel.addRow(new Object[]{ide.getId(), ide.getName(), ide.getPath(), ide.getArgs()});
        }
        
        // Toolbar button actions
        ideTablePanel.getAddButton().addActionListener(e -> addIDE(ideTableModel));
        ideTablePanel.getEditButton().addActionListener(e -> editIDE(ideTable, ideTableModel));
        ideTablePanel.getRemoveButton().addActionListener(e -> removeIDE(ideTable, ideTableModel));
        
        idePanel.add(new JLabel("Configured IDEs:"), BorderLayout.NORTH);
        idePanel.add(ideTablePanel, BorderLayout.CENTER);
        
        // No need to store ideTableModel or ideTable as fields.
        
        // Add tabs
        tabbedPane.addTab("Root Settings", rootPanel);
        tabbedPane.addTab("IDE Configuration", idePanel);

        // --- Reset Tab ---
        JPanel resetTabPanel = new JPanel();
        resetTabPanel.setLayout(new BoxLayout(resetTabPanel, BoxLayout.Y_AXIS));
        JLabel warningLabel = new JLabel("Warning: This will DELETE ALL PROJECTS and RESET the root directory.");
        warningLabel.setForeground(Color.RED);
        warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton resetButton = new JButton("Reset");
        resetButton.setForeground(Color.RED);
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetButton.addActionListener(e -> resetDatabaseAndRoot());
        resetTabPanel.add(Box.createVerticalGlue());
        resetTabPanel.add(warningLabel);
        resetTabPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        resetTabPanel.add(resetButton);
        resetTabPanel.add(Box.createVerticalGlue());
        tabbedPane.addTab("Reset", resetTabPanel);
        
        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCurrentSettings() {
        // Update root display
        currentRootLabel.setText(BackendAPI.root != null ? BackendAPI.root : "Not set");
    }

    private void changeRoot() {
        String oldRoot = BackendAPI.root;
        String warningMsg = "Changing the root directory will RESET the database.\nAre you sure you want to continue?";
        int confirm = showConfirmDialog(warningMsg, "Confirm Root Change", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean success = BackendAPI.promptForRoot();
        String newRoot = BackendAPI.root;
        if (success) {
        }
}

class IDEForm extends JDialog {
    private JButton ok;
    // Utility method for consistent dialogs
    private void showDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }
    private void showDialog(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    // Real-time validation
    private boolean validateFields() {
        boolean valid = true;
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String path = pathField.getText().trim();
        idField.setBackground(Color.white);
        nameField.setBackground(Color.white);
        pathField.setBackground(Color.white);
        ok.setEnabled(true);
        if (id.isEmpty()) {
            idField.setBackground(new Color(255,230,230));
            valid = false;
        }
        if (name.isEmpty()) {
            nameField.setBackground(new Color(255,230,230));
            valid = false;
        }
        if (path.isEmpty()) {
            pathField.setBackground(new Color(255,230,230));
            valid = false;
        }
        ok.setEnabled(valid);
        return valid;
    }
    private JTextField idField, nameField, pathField, argsField;
    private boolean confirmed = false;
    private IDE ide;

    public IDEForm(JDialog parent, IDE ideToEdit) {
        super(parent, true);
        setTitle(ideToEdit == null ? "Add IDE" : "Edit IDE");
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0,2,5,5));
        idField = new JTextField();
        idField.setToolTipText("Unique identifier for the IDE (required)");
        nameField = new JTextField();
        nameField.setToolTipText("Display name for the IDE (required)");
        pathField = new JTextField();
        pathField.setToolTipText("Command or path to launch the IDE (required)");
        argsField = new JTextField();
        argsField.setToolTipText("Optional launch arguments for the IDE");

        // Real-time validation
        javax.swing.event.DocumentListener docListener = new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateFields(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateFields(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateFields(); }
        };
        idField.getDocument().addDocumentListener(docListener);
        nameField.getDocument().addDocumentListener(docListener);
        pathField.getDocument().addDocumentListener(docListener);

        formPanel.add(new JLabel("Unique ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Call:")); //easier to understand
        formPanel.add(pathField);
        formPanel.add(new JLabel("Arguments:"));
        formPanel.add(argsField);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        add(buttonPanel, BorderLayout.SOUTH);

        if (ideToEdit != null) {
            idField.setText(ideToEdit.getId());
            nameField.setText(ideToEdit.getName());
            pathField.setText(ideToEdit.getPath());
            argsField.setText(ideToEdit.getArgs());
            this.ide = ideToEdit;
        } else {
            this.ide = new IDE();
        }

        ok.addActionListener(e -> {
            if (!validateFields()) return;
            ide.setId(idField.getText().trim());
            ide.setName(nameField.getText().trim());
            ide.setPath(pathField.getText().trim());
            ide.setArgs(argsField.getText().trim());
            confirmed = true;
            setVisible(false);
        });
        cancel.addActionListener(e -> setVisible(false));
    }

    public boolean isConfirmed() { return confirmed; }
    public IDE getIDE() { return ide; }

    
} 