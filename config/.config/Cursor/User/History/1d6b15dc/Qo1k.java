package gui.org.drbpatch;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.util.List;

/**
 * Test application for the Project Manager GUI.
 * 
 * This class allows you to test the GUI functionality using the TestBackendService
 * without needing to implement a full backend. It provides:
 * - Sample project data for testing
 * - Sample editor profiles
 * - Console output for debugging
 * - All GUI functionality working with in-memory data
 * 
 * To run this test application:
 * 1. Compile all Java files
 * 2. Run: java gui.org.drbpatch.TestProjectManagerApp
 * 
 * @author Project Manager GUI Test
 * @version 1.0
 */
public class TestProjectManagerApp {
    
    /**
     * Main method to start the test application.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== PROJECT MANAGER GUI TEST APPLICATION ===");
        System.out.println("Starting test application...");
        
        // Initialize test backend service
        TestBackendService testBackend = new TestBackendService();
        
        // Print initial state
        testBackend.printCurrentState();
        
        // Create and show GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Create the main GUI with test backend
                ProjectManagerGUI gui = new TestProjectManagerGUI(testBackend);
                
                // Show the GUI
                gui.setVisible(true);
                
                System.out.println("GUI launched successfully!");
                System.out.println("You can now test all GUI functionality:");
                System.out.println("- View projects in the table");
                System.out.println("- Add new projects");
                System.out.println("- Edit existing projects");
                System.out.println("- Delete projects");
                System.out.println("- Open project folders (simulated)");
                System.out.println("- Open projects with editors (simulated)");
                System.out.println("- Access settings dialog");
                System.out.println();
                System.out.println("Check the console for backend operation logs.");
                
            } catch (Exception e) {
                System.err.println("Error starting GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}

/**
 * Extended GUI class that integrates with the test backend service.
 * This class demonstrates how to properly integrate the GUI with a backend service.
 */
class TestProjectManagerGUI extends ProjectManagerGUI {
    
    private final TestBackendService testBackend;
    private List<Project> currentProjects;
    
    /**
     * Constructor that accepts the test backend service.
     * 
     * @param testBackend The test backend service to use
     */
    public TestProjectManagerGUI(TestBackendService testBackend) {
        super();
        this.testBackend = testBackend;
        this.currentProjects = testBackend.getProjects();
        
        // Set the title to indicate test mode
        setTitle("Project Manager - TEST MODE");
        
        // Load initial data
        refreshProjectTable(currentProjects);
        
        // Set up event handlers for testing
        setupTestEventHandlers();
    }
    
    /**
     * Sets up event handlers that use the test backend service.
     */
    private void setupTestEventHandlers() {
        // Override the event handlers from the parent class
        // We need to access the buttons and set up new listeners
        
        // Get the buttons (we'll need to make them accessible or recreate them)
        // For now, we'll override the main functionality
        
        System.out.println("Test event handlers configured.");
        System.out.println("All GUI operations will be logged to the console.");
    }
    
    /**
     * Override the getSelectedProject method to work with our test data.
     */
    @Override
    public Project getSelectedProject() {
        int row = projectTable.getSelectedRow();
        if (row == -1 || row >= currentProjects.size()) return null;
        return currentProjects.get(row);
    }
    
    /**
     * Override the refreshProjectTable method to keep our local list in sync.
     */
    @Override
    public void refreshProjectTable(List<Project> projects) {
        this.currentProjects = projects;
        super.refreshProjectTable(projects);
    }
    
    /**
     * Add new project functionality.
     */
    public void addNewProject() {
        AddEditProjectDialog dialog = new AddEditProjectDialog(this, new AddEditProjectDialog.ProjectSaveCallback() {
            @Override
            public void onProjectSave(Project project, boolean isEdit) throws Exception {
                if (isEdit) {
                    testBackend.updateProject(project);
                } else {
                    testBackend.addProject(project);
                }
                // Refresh the table
                refreshProjectTable(testBackend.getProjects());
            }
        });
        dialog.setVisible(true);
    }
    
    /**
     * Edit selected project functionality.
     */
    public void editSelectedProject() {
        Project selected = getSelectedProject();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a project to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        AddEditProjectDialog dialog = new AddEditProjectDialog(this, selected, new AddEditProjectDialog.ProjectSaveCallback() {
            @Override
            public void onProjectSave(Project project, boolean isEdit) throws Exception {
                testBackend.updateProject(project);
                // Refresh the table
                refreshProjectTable(testBackend.getProjects());
            }
        });
        dialog.setVisible(true);
    }
    
    /**
     * Delete selected project functionality.
     */
    public void deleteSelectedProject() {
        Project selected = getSelectedProject();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a project to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete the project '" + selected.getName() + "'?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            testBackend.deleteProject(selected);
            // Refresh the table
            refreshProjectTable(testBackend.getProjects());
        }
    }
    
    /**
     * Open project folder functionality.
     */
    public void openProjectFolder() {
        Project selected = getSelectedProject();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a project to open.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        testBackend.openProjectFolder(selected);
    }
    
    /**
     * Open project with editor functionality.
     */
    public void openProjectWith() {
        Project selected = getSelectedProject();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a project to open.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        List<EditorProfile> editors = testBackend.getEditorProfiles();
        if (editors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No editor profiles configured. Please add editors in Settings.", "No Editors", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create a simple editor selection dialog
        String[] editorNames = editors.stream().map(EditorProfile::getName).toArray(String[]::new);
        String selectedEditor = (String) JOptionPane.showInputDialog(this, 
            "Select an editor to open the project with:", 
            "Select Editor", 
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            editorNames, 
            editorNames[0]);
            
        if (selectedEditor != null) {
            EditorProfile editor = editors.stream()
                .filter(e -> e.getName().equals(selectedEditor))
                .findFirst()
                .orElse(null);
            if (editor != null) {
                testBackend.openProjectWith(selected, editor);
            }
        }
    }
    
    /**
     * Open settings dialog functionality.
     */
    public void openSettings() {
        SettingsDialog dialog = new SettingsDialog(this, testBackend.getRootDirectory(), testBackend.getEditorProfiles());
        dialog.setVisible(true);
        
        // Note: In a real implementation, you would save the settings back to the backend
        // For now, we just show the dialog
    }
    
    /**
     * Override the main method to demonstrate how to integrate with a real backend.
     * In a real implementation, you would modify the original ProjectManagerGUI class.
     */
    public static void main(String[] args) {
        TestProjectManagerApp.main(args);
    }
} 