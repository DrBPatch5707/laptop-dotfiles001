package org.drbpatch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Modal dialog for adding new projects or editing existing ones.
 * 
 * <p>This dialog provides a form-based interface for project data entry.
 * It can operate in two modes:
 * - ADD mode: All fields are empty, used for creating new projects
 * - EDIT mode: Fields are pre-populated with existing project data</p>
 * 
 * <p>Features:
 * - Form validation for required fields
 * - File chooser integration for project path selection
 * - Dropdown menus for status and priority selection
 * - Multi-line text area for project description
 * - Callback mechanism for saving project data</p>
 * 
 * <p>Backend Integration:
 * - The dialog does not perform any data persistence
 * - All data validation and saving is handled by the parent GUI
 * - The dialog communicates with the parent through callback methods
 * - File chooser provides absolute paths; backend handles relative path conversion</p>
 * 
 * @author Project Manager GUI
 * @version 1.0
 */
public class AddEditProjectDialog extends JDialog {
    
    // ==================== CONSTANTS ====================
    
    /**
     * Dialog title for adding new projects
     */
    private static final String ADD_TITLE = "Add New Project";
    
    /**
     * Dialog title for editing existing projects
     */
    private static final String EDIT_TITLE = "Edit Project";
    
    /**
     * Default dialog width in pixels
     */
    private static final int DIALOG_WIDTH = 500;
    
    /**
     * Default dialog height in pixels
     */
    private static final int DIALOG_HEIGHT = 600;
    
    /**
     * Standard margin for form components
     */
    private static final int MARGIN = 10;
    
    // ==================== INSTANCE VARIABLES ====================
    
    /**
     * Whether this dialog is in edit mode (true) or add mode (false)
     */
    private final boolean isEditMode;
    
    /**
     * The project being edited (null in add mode)
     */
    private final Project originalProject;
    
    /**
     * Callback interface for when the user saves the project
     */
    private final ProjectSaveCallback saveCallback;
    
    // ==================== UI COMPONENTS ====================
    
    /**
     * Text field for project name (required field)
     */
    private JTextField nameField;
    
    /**
     * Text area for project description (optional field)
     */
    private JTextArea descriptionArea;
    
    /**
     * Text field for relative path (required field)
     */
    private JTextField pathField;
    
    /**
     * Dropdown for project status selection
     */
    private JComboBox<String> statusComboBox;
    
    /**
     * Dropdown for project priority selection
     */
    private JComboBox<String> priorityComboBox;
    
    /**
     * Text field for project category (optional field)
     */
    private JTextField categoryField;
    
    /**
     * Save button - triggers the save callback
     */
    private JButton saveButton;
    
    /**
     * Cancel button - closes the dialog without saving
     */
    private JButton cancelButton;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * Creates a new dialog for adding a project.
     * 
     * @param parent The parent frame (main GUI)
     * @param saveCallback Callback to invoke when project is saved
     */
    public AddEditProjectDialog(JFrame parent, ProjectSaveCallback saveCallback) {
        this(parent, null, saveCallback);
    }
    
    /**
     * Creates a new dialog for editing an existing project.
     * 
     * @param parent The parent frame (main GUI)
     * @param project The project to edit (must not be null)
     * @param saveCallback Callback to invoke when project is saved
     * @throws IllegalArgumentException if project is null
     */
    public AddEditProjectDialog(JFrame parent, Project project, ProjectSaveCallback saveCallback) {
        super(parent, true); // Modal dialog
        
        if (project == null) {
            this.isEditMode = false;
            this.originalProject = null;
        } else {
            this.isEditMode = true;
            this.originalProject = project.copy(); // Create a copy to avoid modifying original
        }
        
        this.saveCallback = saveCallback;
        
        initializeDialog();
        createComponents();
        layoutComponents();
        setupEventHandlers();
        populateFields();
        
        // Set dialog properties
        setResizable(false);
        setLocationRelativeTo(parent);
    }
    
    // ==================== INITIALIZATION ====================
    
    /**
     * Initializes the dialog properties.
     */
    private void initializeDialog() {
        setTitle(isEditMode ? EDIT_TITLE : ADD_TITLE);
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    /**
     * Creates all UI components.
     */
    private void createComponents() {
        // Create text fields
        nameField = new JTextField(30);
        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        pathField = new JTextField(30);
        pathField.setEditable(true); // Path is entered manually as relative path
        categoryField = new JTextField(30);
        
        // Create combo boxes
        statusComboBox = new JComboBox<>(new String[]{
            "In Development", "Completed", "Depreciated"
        });
        
        priorityComboBox = new JComboBox<>(new String[]{
            "High (1)", "High (2)", "Medium (3)", "Low (4)", "Low (5)"
        });
        
        // Create buttons
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        
        // Set default button (activated by Enter key)
        getRootPane().setDefaultButton(saveButton);
    }
    
    /**
     * Lays out all components using GridBagLayout for proper alignment.
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Create main form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Row 0: Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(nameField, gbc);
        
        // Row 1: Description
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setPreferredSize(new Dimension(0, 100));
        formPanel.add(descriptionScrollPane, gbc);
        
        // Row 2: Relative Path
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0.0;
        formPanel.add(new JLabel("Relative Path:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(pathField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        formPanel.add(browseButton, gbc);
        
        // Row 3: Status
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(statusComboBox, gbc);
        
        // Row 4: Priority
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1;
        formPanel.add(priorityComboBox, gbc);
        
        // Row 5: Category
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        formPanel.add(categoryField, gbc);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // Add panels to dialog
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Sets up event handlers for all interactive components.
     */
    private void setupEventHandlers() {
        // Browse button - opens file chooser for directory selection
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseForProjectDirectory();
            }
        });
        
        // Save button - validates and saves project data
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProject();
            }
        });
        
        // Cancel button - closes dialog without saving
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Handle Escape key to close dialog
        getRootPane().registerKeyboardAction(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            },
            "Cancel",
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    /**
     * Populates form fields with existing project data (edit mode only).
     */
    private void populateFields() {
        if (!isEditMode || originalProject == null) {
            return;
        }
        
        // Populate fields with existing project data
        nameField.setText(originalProject.getName());
        descriptionArea.setText(originalProject.getDescription());
        pathField.setText(originalProject.getRelativePath());
        categoryField.setText(originalProject.getCategory());
        
        // Set status combo box
        String status = originalProject.getStatus();
        for (int i = 0; i < statusComboBox.getItemCount(); i++) {
            if (statusComboBox.getItemAt(i).equals(status)) {
                statusComboBox.setSelectedIndex(i);
                break;
            }
        }
        
        // Set priority combo box
        int priority = originalProject.getPriority();
        if (priority >= 1 && priority <= 5) {
            priorityComboBox.setSelectedIndex(priority - 1);
        }
    }
    
    // ==================== EVENT HANDLERS ====================
    
    /**
     * Opens a file chooser dialog for selecting the project directory.
     * Updates the path field with the selected directory's absolute path.
     */
    private void browseForProjectDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Project Directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        // Set current directory if path field has content
        String currentPath = pathField.getText().trim();
        if (!currentPath.isEmpty()) {
            File currentFile = new File(currentPath);
            if (currentFile.exists()) {
                fileChooser.setCurrentDirectory(currentFile);
            }
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                // Store the absolute path - backend will convert to relative
                pathField.setText(selectedFile.getAbsolutePath());
            }
        }
    }
    
    /**
     * Validates form data and saves the project.
     * Calls the save callback with the project data if validation passes.
     */
    private void saveProject() {
        // Validate required fields
        String name = nameField.getText().trim();
        String path = pathField.getText().trim();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Project name is required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return;
        }
        
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Project path is required. Please use the Browse button to select a directory.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            browseButton.requestFocus();
            return;
        }
        
        // Create or update project object
        Project project;
        if (isEditMode && originalProject != null) {
            project = originalProject.copy();
        } else {
            project = new Project();
        }
        
        // Populate project with form data
        project.setName(name);
        project.setDescription(descriptionArea.getText().trim());
        project.setRelativePath(path); // Backend will convert absolute to relative
        project.setCategory(categoryField.getText().trim());
        
        // Set status
        String selectedStatus = (String) statusComboBox.getSelectedItem();
        if (selectedStatus != null) {
            project.setStatus(selectedStatus);
        }
        
        // Set priority (convert combo box selection to numeric value)
        int selectedPriorityIndex = priorityComboBox.getSelectedIndex();
        int priority = selectedPriorityIndex + 1; // Convert 0-based index to 1-5 range
        project.setPriority(priority);
        
        // Call the save callback
        if (saveCallback != null) {
            try {
                saveCallback.onProjectSave(project, isEditMode);
                dispose(); // Close dialog on successful save
            } catch (Exception e) {
                // Handle save errors
                JOptionPane.showMessageDialog(this,
                    "Error saving project: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Shows the dialog and returns the result.
     * 
     * @return true if the user saved the project, false if cancelled
     */
    public boolean showDialog() {
        setVisible(true);
        return false; // Dialog result is handled through callback
    }
    
    // ==================== CALLBACK INTERFACE ====================
    
    /**
     * Callback interface for project save operations.
     * Implemented by the parent GUI to handle project persistence.
     */
    public interface ProjectSaveCallback {
        /**
         * Called when the user saves a project from the dialog.
         * 
         * @param project The project data to save
         * @param isEdit True if editing an existing project, false if creating new
         * @throws Exception if the save operation fails
         */
        void onProjectSave(Project project, boolean isEdit) throws Exception;
    }
} 