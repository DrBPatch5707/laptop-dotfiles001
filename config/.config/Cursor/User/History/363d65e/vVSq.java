package gui.org.drbpatch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/**
 * Modal dialog for configuring application-level settings.
 * <p>
 * Allows the user to set the projects root directory and manage editor profiles.
 * Editor profiles define which external editors can be used to open projects.
 * <p>
 * Backend integration:
 * - No file I/O or persistence is performed here; all data is passed to/from the parent GUI.
 * - The dialog provides event listener placeholders for all actions.
 *
 * @author Project Manager GUI
 * @version 1.0
 */
public class SettingsDialog extends JDialog {
    // ==================== UI COMPONENTS ====================
    private JTextField rootDirField;
    private JButton browseRootButton;
    private JTable editorsTable;
    private JButton addEditorButton;
    private JButton editEditorButton;
    private JButton removeEditorButton;
    private JButton saveButton;
    private JButton cancelButton;
    private EditorProfileTableModel editorProfileTableModel;

    // ==================== CONSTRUCTOR ====================
    /**
     * Creates a new SettingsDialog.
     *
     * @param parent The parent frame
     * @param initialRootDir The current projects root directory
     * @param editorProfiles The list of editor profiles to display
     */
    public SettingsDialog(JFrame parent, String initialRootDir, List<EditorProfile> editorProfiles) {
        super(parent, "Settings", true);
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        initComponents(initialRootDir, editorProfiles);
        layoutComponents();
        setupEventHandlers();
    }

    // ==================== INITIALIZATION ====================
    private void initComponents(String initialRootDir, List<EditorProfile> editorProfiles) {
        rootDirField = new JTextField(initialRootDir != null ? initialRootDir : "", 30);
        browseRootButton = new JButton("Browse...");
        editorProfileTableModel = new EditorProfileTableModel(editorProfiles);
        editorsTable = new JTable(editorProfileTableModel);
        editorsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        editorsTable.setFillsViewportHeight(true);
        addEditorButton = new JButton("Add Editor Profile");
        editEditorButton = new JButton("Edit Selected Profile");
        removeEditorButton = new JButton("Remove Selected Profile");
        saveButton = new JButton("Save Settings");
        cancelButton = new JButton("Cancel");
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Row 0: Projects Root Directory
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Projects Root Directory:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(rootDirField, gbc);
        gbc.gridx = 2; gbc.weightx = 0.0;
        mainPanel.add(browseRootButton, gbc);

        // Row 1: Editor Profiles label
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        mainPanel.add(new JLabel("Configured Editors:"), gbc);

        // Row 2: Editors table
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        JScrollPane tableScroll = new JScrollPane(editorsTable);
        tableScroll.setPreferredSize(new Dimension(0, 120));
        mainPanel.add(tableScroll, gbc);

        // Row 3: Editor profile buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0.0;
        JPanel editorButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        editorButtonPanel.add(addEditorButton);
        editorButtonPanel.add(editEditorButton);
        editorButtonPanel.add(removeEditorButton);
        mainPanel.add(editorButtonPanel, gbc);

        // Row 4: Save/Cancel buttons
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        JPanel saveCancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        saveCancelPanel.add(saveButton);
        saveCancelPanel.add(cancelButton);
        mainPanel.add(saveCancelPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        // Browse for root directory
        browseRootButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Projects Root Directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selected = chooser.getSelectedFile();
                if (selected != null) {
                    rootDirField.setText(selected.getAbsolutePath());
                }
            }
        });

        // Add Editor Profile
        addEditorButton.addActionListener(e -> {
            // TODO: Show dialog to add new EditorProfile
            // Example: new AddEditEditorProfileDialog(this, null, profile -> editorProfileTableModel.addProfile(profile)).setVisible(true);
        });

        // Edit Selected Profile
        editEditorButton.addActionListener(e -> {
            int selectedRow = editorsTable.getSelectedRow();
            if (selectedRow >= 0) {
                EditorProfile profile = editorProfileTableModel.getProfileAt(selectedRow);
                // TODO: Show dialog to edit profile and update model
                // Example: new AddEditEditorProfileDialog(this, profile, updated -> editorProfileTableModel.updateProfile(selectedRow, updated)).setVisible(true);
            }
        });

        // Remove Selected Profile
        removeEditorButton.addActionListener(e -> {
            int selectedRow = editorsTable.getSelectedRow();
            if (selectedRow >= 0) {
                // TODO: Confirm removal
                editorProfileTableModel.removeProfile(selectedRow);
            }
        });

        // Save Settings
        saveButton.addActionListener(e -> {
            // TODO: Gather all settings and call backend callback
            // Example: settingsCallback.onSaveSettings(rootDirField.getText(), editorProfileTableModel.getProfiles());
            dispose();
        });

        // Cancel
        cancelButton.addActionListener(e -> dispose());
    }

    // ==================== TABLE MODEL ====================
    /**
     * Table model for displaying EditorProfile objects in a JTable.
     */
    private static class EditorProfileTableModel extends AbstractTableModel {
        private final String[] columns = {"Name", "Path"};
        private final java.util.List<EditorProfile> profiles;

        public EditorProfileTableModel(List<EditorProfile> profiles) {
            this.profiles = profiles != null ? new java.util.ArrayList<>(profiles) : new java.util.ArrayList<>();
        }

        public int getRowCount() { return profiles.size(); }
        public int getColumnCount() { return columns.length; }
        public String getColumnName(int col) { return columns[col]; }
        public Object getValueAt(int row, int col) {
            EditorProfile p = profiles.get(row);
            switch (col) {
                case 0: return p.getName();
                case 1: return p.getExecutablePath();
                default: return "";
            }
        }
        public EditorProfile getProfileAt(int row) { return profiles.get(row); }
        public void addProfile(EditorProfile p) {
            profiles.add(p);
            fireTableRowsInserted(profiles.size() - 1, profiles.size() - 1);
        }
        public void updateProfile(int row, EditorProfile p) {
            profiles.set(row, p);
            fireTableRowsUpdated(row, row);
        }
        public void removeProfile(int row) {
            profiles.remove(row);
            fireTableRowsDeleted(row, row);
        }
        public List<EditorProfile> getProfiles() { return new java.util.ArrayList<>(profiles); }
    }
} 