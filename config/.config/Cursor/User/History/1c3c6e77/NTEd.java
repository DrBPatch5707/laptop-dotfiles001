package org.drbpatch;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.ListSelectionModel;
import org.drbpatch.ProjectForm;

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
            init.performFilesystemSync();
        });
    }

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
            JOptionPane.showMessageDialog(this, "Root directory set to: " + BackendAPI.root);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid directory or operation cancelled.");
        }
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

    private void loadProjects() {
        tableModel.setRowCount(0);
        for (Project p : BackendAPI.projects.values()) {
            tableModel.addRow(new Object[]{p.getName(), p.getStatus(), p.getPriorityDisplay()});
        }
    }

    // Public static method to reload projects from anywhere
    public static void reloadProjectsInAllGUIs() {
        // Find all open ProjectManagerGUI windows and reload their projects
        for (Frame frame : Frame.getFrames()) {
            if (frame instanceof ProjectManagerGUI) {
                ((ProjectManagerGUI) frame).loadProjects();
            }
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
            boolean dirExists = form.isDirExistsChecked();
            BackendAPI.addProject(newProject, dirExists);
            loadProjects();
            ProjectManagerGUI.reloadProjectsInAllGUIs();
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
            ProjectManagerGUI.reloadProjectsInAllGUIs();
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
            ProjectManagerGUI.reloadProjectsInAllGUIs();
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

    private void showAccessDialog() {
        Project selected = getSelectedProject();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a project to access.");
            return;
        }
        java.util.Map<String, IDE> ides = BackendAPI.getIdes();
        if (ides == null || ides.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No IDEs configured.");
            return;
        }
        String[] ideNames = ides.values().stream().map(IDE::getName).toArray(String[]::new);
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
        IDE selectedIDE = ides.values().stream().filter(ide -> ide.getName().equals(chosen)).findFirst().orElse(null);
        if (selectedIDE == null) {
            JOptionPane.showMessageDialog(this, "IDE not found.");
            return;
        }
        boolean result = BackendAPI.accessProject(selected, selectedIDE);
        if (result) {
            JOptionPane.showMessageDialog(this, "Project opened in " + selectedIDE.getName() + ".");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to open project in " + selectedIDE.getName() + ".");
        }
    }

    private void showNotificationsDialog() {
        NotificationsDialog dialog = new NotificationsDialog(this);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProjectManagerGUI().setVisible(true));
    }

    public static String promptForRoot(Component parent, String currentRoot) {
        return JOptionPane.showInputDialog(parent, "Enter new root directory:", currentRoot);
    }
}

class NotificationsDialog extends JDialog {
    private JTextArea notificationsArea;
    private JButton refreshButton;
    private JButton clearButton;

    public NotificationsDialog(JFrame parent) {
        super(parent, "Filesystem Notifications", true);
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        initComponents();
        loadNotifications();
    }

    private void initComponents() {
        // Title
        JLabel titleLabel = new JLabel("Filesystem Changes Detected");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Notifications area
        notificationsArea = new JTextArea();
        notificationsArea.setEditable(false);
        notificationsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(notificationsArea);
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        refreshButton = new JButton("Refresh");
        clearButton = new JButton("Clear All");
        
        refreshButton.addActionListener(e -> loadNotifications());
        clearButton.addActionListener(e -> {
            notificationsArea.setText("");
            BackendAPI.clearNotifications();
        });
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        
        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadNotifications() {
        java.util.List<String> notifications = BackendAPI.getNotifications();
        if (notifications.isEmpty()) {
            notificationsArea.setText("No filesystem changes detected.\n\nAll projects are properly registered in the database.");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("The following directories were found but are not registered as projects:\n\n");
            for (String notification : notifications) {
                sb.append("• ").append(notification).append("\n");
            }
            sb.append("\nYou can add these as projects using the 'Add Project' button and checking 'Directory already exists'.");
            notificationsArea.setText(sb.toString());
        }
    }
}

class SettingsDialog extends JDialog {
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
        
        rootPanel.add(rootInfoPanel, BorderLayout.CENTER);
        rootPanel.add(changeRootButton, BorderLayout.SOUTH);
        
        // IDE Configuration Tab
        JPanel idePanel = new JPanel(new BorderLayout());
        idePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // IDE List
        DefaultListModel<String> ideListModel = new DefaultListModel<>();
        JList<String> ideList = new JList<>(ideListModel);
        ideList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane ideScrollPane = new JScrollPane(ideList);
        ideScrollPane.setPreferredSize(new Dimension(300, 200));
        
        // Buttons
        JButton addIdeButton = new JButton("Add IDE");
        JButton editIdeButton = new JButton("Edit IDE");
        JButton removeIdeButton = new JButton("Remove IDE");
        
        addIdeButton.addActionListener(e -> addIDE(ideListModel));
        editIdeButton.addActionListener(e -> editIDE(ideList, ideListModel));
        removeIdeButton.addActionListener(e -> removeIDE(ideList, ideListModel));
        
        JPanel ideButtonPanel = new JPanel();
        ideButtonPanel.add(addIdeButton);
        ideButtonPanel.add(editIdeButton);
        ideButtonPanel.add(removeIdeButton);
        
        idePanel.add(new JLabel("Configured IDEs:"), BorderLayout.NORTH);
        idePanel.add(ideScrollPane, BorderLayout.CENTER);
        idePanel.add(ideButtonPanel, BorderLayout.SOUTH);
        
        // Store references for later use
        this.ideListModel = ideListModel;
        this.ideList = ideList;
        
        // Add tabs
        tabbedPane.addTab("Root Settings", rootPanel);
        tabbedPane.addTab("IDE Configuration", idePanel);
        
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
        // Load IDE list
        ideListModel.clear();
        java.util.Map<String, IDE> ides = BackendAPI.getIdes();
        if (ides != null && !ides.isEmpty()) {
            for (IDE ide : ides.values()) {
                if (!"default".equals(ide.getId())) {
                    ideListModel.addElement(ide.getName());
                }
            }
        }
    }

    private void changeRoot() {
        boolean success = BackendAPI.promptForRoot();
        if (success) {
            currentRootLabel.setText(BackendAPI.root);
            JOptionPane.showMessageDialog(this, "Root directory updated successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update root directory.");
        }
    }

    private void addIDE(DefaultListModel<String> model) {
        IDEForm form = new IDEForm(this, null);
        form.setVisible(true);
        if (form.isConfirmed()) {
            IDE newIDE = form.getIDE();
            if (BackendAPI.getIdes().containsKey(newIDE.getId())) {
                JOptionPane.showMessageDialog(this, "IDE with ID '" + newIDE.getId() + "' already exists.");
                return;
            }
            Main.config.addIde(newIDE.getId(), newIDE);
            try {
                JacksonIdeConfigManager.saveConfiguration(Main.config);
            } catch (Exception e) {
                e.printStackTrace();
            }
            loadCurrentSettings();
            JOptionPane.showMessageDialog(this, "IDE added successfully!");
        }
    }

    private void editIDE(JList<String> list, DefaultListModel<String> model) {
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Select an IDE to edit.");
            return;
        }
        String selectedName = list.getSelectedValue();
        // Find the IDE by name
        IDE ide = null;
        String ideId = null;
        for (java.util.Map.Entry<String, IDE> entry : BackendAPI.getIdes().entrySet()) {
            if (entry.getValue().getName().equals(selectedName)) {
                ide = entry.getValue();
                ideId = entry.getKey();
                break;
            }
        }
        if (ide == null) {
            JOptionPane.showMessageDialog(this, "IDE not found.");
            return;
        }
        IDEForm form = new IDEForm(this, ide);
        form.setVisible(true);
        if (form.isConfirmed()) {
            IDE updatedIDE = form.getIDE();
            Main.config.addIde(updatedIDE.getId(), updatedIDE);
            try {
                JacksonIdeConfigManager.saveConfiguration(Main.config);
            } catch (Exception e) {
                e.printStackTrace();
            }
            loadCurrentSettings();
            JOptionPane.showMessageDialog(this, "IDE edited successfully!");
        }
    }

    private void removeIDE(JList<String> list, DefaultListModel<String> model) {
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Select an IDE to remove.");
            return;
        }
        String selectedName = list.getSelectedValue();
        // Find the IDE by name
        IDE ide = null;
        String ideId = null;
        for (java.util.Map.Entry<String, IDE> entry : BackendAPI.getIdes().entrySet()) {
            if (entry.getValue().getName().equals(selectedName)) {
                ide = entry.getValue();
                ideId = entry.getKey();
                break;
            }
        }
        if (ide == null) {
            JOptionPane.showMessageDialog(this, "IDE not found.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove IDE '" + selectedName + "'?", "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            BackendAPI.getIdes().remove(ideId);
            try {
                JacksonIdeConfigManager.saveConfiguration(Main.config);
            } catch (Exception e) {
                e.printStackTrace();
            }
            loadCurrentSettings();
            JOptionPane.showMessageDialog(this, "IDE removed successfully!");
        }
    }
} 

class IDEForm extends JDialog {
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
        nameField = new JTextField();
        pathField = new JTextField();
        argsField = new JTextField();

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
        JButton ok = new JButton("OK");
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
            if (idField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID is required.");
                return;
            }
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required.");
                return;
            }
            if (pathField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Path is required.");
                return;
            }
            
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