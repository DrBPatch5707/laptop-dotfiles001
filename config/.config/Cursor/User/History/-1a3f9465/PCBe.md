# Project Manager GUI Integration Guide

## Overview

This GUI package provides a complete, ready-to-use interface for managing projects. It's designed to be easily integrated with any backend system through a clean interface contract.

## Package Structure

```
gui/org/drbpatch/
├── BackendInterface.java          # Interface defining backend contract
├── Project.java                   # Project data model
├── EditorProfile.java             # Editor profile data model
├── ProjectManagerGUI.java         # Main application window
├── AddEditProjectDialog.java      # Project creation/editing dialog
├── SettingsDialog.java            # Settings configuration dialog
├── TestBackendService.java        # Test implementation (for development)
├── TestProjectManagerApp.java     # Test application
└── test_gui.sh                    # Test script
```

## Core Components

### 1. Data Models
- **Project.java**: Complete project data model with all required fields
- **EditorProfile.java**: Editor configuration model for launching external editors

### 2. GUI Components
- **ProjectManagerGUI.java**: Main application window with project table and action buttons
- **AddEditProjectDialog.java**: Modal dialog for creating/editing projects
- **SettingsDialog.java**: Settings configuration dialog

### 3. Integration Interface
- **BackendInterface.java**: Defines the contract between GUI and backend

## Integration Steps

### Step 1: Copy GUI Files to Your Backend Project

Copy all the `.java` files from the `gui/org/drbpatch/` directory to your backend project's source directory. Maintain the package structure:

```
your-backend-project/
├── src/
│   └── gui/
│       └── org/
│           └── drbpatch/
│               ├── BackendInterface.java
│               ├── Project.java
│               ├── EditorProfile.java
│               ├── ProjectManagerGUI.java
│               ├── AddEditProjectDialog.java
│               └── SettingsDialog.java
```

### Step 2: Implement BackendInterface

Create a class in your backend that implements `BackendInterface`:

```java
public class YourBackendService implements BackendInterface {
    
    @Override
    public void initialize() throws Exception {
        // Initialize your database, load configuration, etc.
    }
    
    @Override
    public List<Project> getProjects() {
        // Return projects from your database/storage
    }
    
    @Override
    public void addProject(Project project) throws Exception {
        // Save project to your database/storage
    }
    
    // Implement all other methods...
}
```

### Step 3: Create Your Main Application

Create a main class that ties everything together:

```java
public class ProjectManagerApp {
    public static void main(String[] args) {
        // Initialize your backend
        YourBackendService backend = new YourBackendService();
        
        try {
            backend.initialize();
            
            // Create and show GUI
            SwingUtilities.invokeLater(() -> {
                ProjectManagerGUI gui = new YourProjectManagerGUI(backend);
                gui.setVisible(true);
            });
            
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
```

### Step 4: Extend ProjectManagerGUI (Optional)

If you need custom behavior, extend `ProjectManagerGUI`:

```java
public class YourProjectManagerGUI extends ProjectManagerGUI {
    private final YourBackendService backend;
    
    public YourProjectManagerGUI(YourBackendService backend) {
        super();
        this.backend = backend;
        
        // Load initial data
        refreshProjectTable(backend.getProjects());
        
        // Override event handlers
        setupCustomEventHandlers();
    }
    
    private void setupCustomEventHandlers() {
        // Override button actions to use your backend
        addProjectButton.addActionListener(e -> addNewProject());
        editProjectButton.addActionListener(e -> editSelectedProject());
        // ... etc
    }
    
    private void addNewProject() {
        AddEditProjectDialog dialog = new AddEditProjectDialog(this, 
            (project, isEdit) -> {
                if (isEdit) {
                    backend.updateProject(project);
                } else {
                    backend.addProject(project);
                }
                refreshProjectTable(backend.getProjects());
            });
        dialog.setVisible(true);
    }
}
```

## Required Dependencies

The GUI only requires standard Java Swing libraries:
- `javax.swing.*`
- `java.awt.*`
- `java.util.*`

No external libraries or frameworks are required.

## Data Flow

1. **GUI → Backend**: User actions trigger calls to `BackendInterface` methods
2. **Backend → GUI**: Backend operations update data, GUI refreshes display
3. **Error Handling**: All backend methods can throw exceptions, GUI handles them gracefully

## Testing Your Integration

1. Use `TestBackendService` as a reference implementation
2. Run `TestProjectManagerApp` to see how the GUI should behave
3. Compare your backend implementation with the test implementation

## Key Integration Points

### Project Management
- `getProjects()`: Called when GUI loads and after data changes
- `addProject()`: Called when user creates new project
- `updateProject()`: Called when user edits existing project
- `deleteProject()`: Called when user deletes project

### Settings Management
- `getRootDirectory()`: Called when settings dialog opens
- `setRootDirectory()`: Called when user changes root directory
- `getEditorProfiles()`: Called when settings dialog opens
- `addEditorProfile()`, `updateEditorProfile()`, `deleteEditorProfile()`: Called when user manages editors

### Project Operations
- `openProjectFolder()`: Called when user clicks "Open Project Folder"
- `openProjectWith()`: Called when user clicks "Open Project With..."

## Error Handling

The GUI expects your backend to throw exceptions when operations fail. The GUI will:
- Display error messages to the user
- Log errors to console
- Continue running (won't crash)

## Best Practices

1. **Thread Safety**: Ensure your backend is thread-safe
2. **Data Validation**: Validate data in your backend before saving
3. **Error Messages**: Provide meaningful error messages
4. **Performance**: Implement efficient data loading for large project lists
5. **Persistence**: Ensure data is properly saved to your storage system

## Example Backend Implementation

See `TestBackendService.java` for a complete example of how to implement the `BackendInterface`. This class demonstrates:
- In-memory data storage
- Proper exception handling
- Console logging for debugging
- Sample data initialization

## Troubleshooting

### Common Issues

1. **GUI doesn't show projects**: Check that `getProjects()` returns data
2. **Buttons don't work**: Ensure you've properly connected event handlers
3. **Exceptions not handled**: Make sure your backend throws exceptions properly
4. **Settings not saved**: Implement the settings persistence methods

### Debugging

1. Use console output to track backend operations
2. Check that all `BackendInterface` methods are implemented
3. Verify data flows correctly between GUI and backend
4. Test with `TestBackendService` first to isolate issues

## Support

If you encounter issues:
1. Check the console output for error messages
2. Compare your implementation with `TestBackendService`
3. Ensure all required methods are implemented
4. Verify data types match the interface specification 