package org.drbpatch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
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
    private JButton saveButton;
    private JButton cancelButton;

    // ==================== CONSTRUCTOR ====================
    /**
     * Creates a new SettingsDialog.
     *
     * @param parent The parent frame
     * @param initialRootDir The current projects root directory
     */
    public SettingsDialog(JFrame parent, String initialRootDir) {
        super(parent, "Settings", true);
        setSize(400, 150);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        initComponents(initialRootDir);
        layoutComponents();
        setupEventHandlers();
    }

    // ==================== INITIALIZATION ====================
    private void initComponents(String initialRootDir) {
        rootDirField = new JTextField(initialRootDir != null ? initialRootDir : "", 30);
        browseRootButton = new JButton("Browse...");
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

        // Row 1: Save/Cancel buttons
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
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
                    DataBank.root = selected.getAbsolutePath();
                }
            }
        });

        // Save Settings
        saveButton.addActionListener(e -> {
            // Save the root directory to DataBank
            DataBank.root = rootDirField.getText().trim();
            dispose();
        });

        // Cancel
        cancelButton.addActionListener(e -> dispose());
    }
} 