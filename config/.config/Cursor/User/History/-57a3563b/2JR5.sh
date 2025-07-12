#!/bin/bash

# Project Manager GUI - Integration Package Script
# This script creates a clean package of GUI files for backend integration

echo "=== PROJECT MANAGER GUI - INTEGRATION PACKAGE ==="
echo ""

# Check if we're in the right directory
if [ ! -f "Project.java" ]; then
    echo "Error: Project.java not found. Please run this script from the gui/org/drbpatch directory."
    exit 1
fi

# Create integration package directory
PACKAGE_DIR="project-manager-gui-integration"
echo "Creating integration package in: $PACKAGE_DIR"
rm -rf "$PACKAGE_DIR"
mkdir -p "$PACKAGE_DIR"

# Copy core GUI files (excluding test files)
echo "Copying core GUI files..."
cp BackendInterface.java "$PACKAGE_DIR/"
cp Project.java "$PACKAGE_DIR/"
cp EditorProfile.java "$PACKAGE_DIR/"
cp ProjectManagerGUI.java "$PACKAGE_DIR/"
cp AddEditProjectDialog.java "$PACKAGE_DIR/"
cp SettingsDialog.java "$PACKAGE_DIR/"

# Copy documentation
echo "Copying documentation..."
cp INTEGRATION_GUIDE.md "$PACKAGE_DIR/"
cp BACKEND_INTERFACE_SPEC.txt "$PACKAGE_DIR/"
cp QUICK_START_GUIDE.txt "$PACKAGE_DIR/"

# Create a simple integration example
echo "Creating integration example..."
cat > "$PACKAGE_DIR/IntegrationExample.java" << 'EOF'
package gui.org.drbpatch;

import javax.swing.SwingUtilities;

/**
 * Example of how to integrate the Project Manager GUI with your backend.
 * 
 * This is a minimal example showing the basic integration pattern.
 * Replace YourBackendService with your actual backend implementation.
 */
public class IntegrationExample {
    
    public static void main(String[] args) {
        // Initialize your backend service
        YourBackendService backend = new YourBackendService();
        
        try {
            // Initialize the backend
            backend.initialize();
            
            // Create and show GUI on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                try {
                    // Create the main GUI with your backend
                    ProjectManagerGUI gui = new YourProjectManagerGUI(backend);
                    
                    // Show the GUI
                    gui.setVisible(true);
                    
                    System.out.println("Project Manager GUI launched successfully!");
                    
                } catch (Exception e) {
                    System.err.println("Error starting GUI: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error initializing backend: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

/**
 * Example backend service implementation.
 * Replace this with your actual backend implementation.
 */
class YourBackendService implements BackendInterface {
    
    @Override
    public void initialize() throws Exception {
        // Initialize your database, load configuration, etc.
        System.out.println("Your backend initialized");
    }
    
    @Override
    public void shutdown() throws Exception {
        // Clean up resources
        System.out.println("Your backend shutting down");
    }
    
    @Override
    public java.util.List<Project> getProjects() {
        // Return projects from your database/storage
        return new java.util.ArrayList<>();
    }
    
    @Override
    public void addProject(Project project) throws Exception {
        // Save project to your database/storage
        System.out.println("Adding project: " + project.getName());
    }
    
    @Override
    public void updateProject(Project project) throws Exception {
        // Update project in your database/storage
        System.out.println("Updating project: " + project.getName());
    }
    
    @Override
    public void deleteProject(Project project) throws Exception {
        // Delete project from your database/storage
        System.out.println("Deleting project: " + project.getName());
    }
    
    @Override
    public String getRootDirectory() {
        // Return your projects root directory
        return System.getProperty("user.home") + "/Projects";
    }
    
    @Override
    public void setRootDirectory(String path) throws Exception {
        // Save root directory to your configuration
        System.out.println("Setting root directory to: " + path);
    }
    
    @Override
    public java.util.List<EditorProfile> getEditorProfiles() {
        // Return editor profiles from your configuration
        return new java.util.ArrayList<>();
    }
    
    @Override
    public void addEditorProfile(EditorProfile profile) throws Exception {
        // Save editor profile to your configuration
        System.out.println("Adding editor profile: " + profile.getName());
    }
    
    @Override
    public void updateEditorProfile(EditorProfile profile) throws Exception {
        // Update editor profile in your configuration
        System.out.println("Updating editor profile: " + profile.getName());
    }
    
    @Override
    public void deleteEditorProfile(EditorProfile profile) throws Exception {
        // Delete editor profile from your configuration
        System.out.println("Deleting editor profile: " + profile.getName());
    }
    
    @Override
    public void openProjectFolder(Project project) throws Exception {
        // Open project folder in system file manager
        System.out.println("Opening project folder: " + project.getName());
    }
    
    @Override
    public void openProjectWith(Project project, EditorProfile editor) throws Exception {
        // Launch editor with project
        System.out.println("Opening project '" + project.getName() + "' with editor '" + editor.getName() + "'");
    }
}

/**
 * Example GUI extension.
 * Extend ProjectManagerGUI to integrate with your backend.
 */
class YourProjectManagerGUI extends ProjectManagerGUI {
    
    private final YourBackendService backend;
    
    public YourProjectManagerGUI(YourBackendService backend) {
        super();
        this.backend = backend;
        
        // Load initial data
        refreshProjectTable(backend.getProjects());
        
        // Set up event handlers
        setupEventHandlers();
    }
    
    private void setupEventHandlers() {
        // Override the default event handlers to use your backend
        // This is a simplified example - see INTEGRATION_GUIDE.md for full details
        
        System.out.println("Event handlers configured for your backend");
    }
}
EOF

# Create a README for the package
cat > "$PACKAGE_DIR/README.md" << 'EOF'
# Project Manager GUI - Integration Package

This package contains all the GUI components needed to integrate the Project Manager with your backend system.

## Files Included

### Core GUI Components
- `BackendInterface.java` - Interface defining the backend contract
- `Project.java` - Project data model
- `EditorProfile.java` - Editor profile data model
- `ProjectManagerGUI.java` - Main application window
- `AddEditProjectDialog.java` - Project creation/editing dialog
- `SettingsDialog.java` - Settings configuration dialog

### Documentation
- `INTEGRATION_GUIDE.md` - Complete integration guide
- `BACKEND_INTERFACE_SPEC.txt` - Detailed interface specification
- `QUICK_START_GUIDE.txt` - Quick start instructions

### Example
- `IntegrationExample.java` - Minimal integration example

## Quick Start

1. Copy all `.java` files to your backend project's source directory
2. Implement the `BackendInterface` in your backend service
3. Create a main class similar to `IntegrationExample.java`
4. Compile and run

## Dependencies

Only standard Java libraries are required:
- `javax.swing.*`
- `java.awt.*`
- `java.util.*`

No external libraries or frameworks needed.

## Next Steps

1. Read `INTEGRATION_GUIDE.md` for detailed instructions
2. Review `BACKEND_INTERFACE_SPEC.txt` for interface details
3. Use `IntegrationExample.java` as a starting point
4. Test with your backend implementation

## Support

If you encounter issues:
1. Check the console output for error messages
2. Ensure all `BackendInterface` methods are implemented
3. Verify data types match the interface specification
4. Test with the provided example first
EOF

# Create a compilation script
cat > "$PACKAGE_DIR/compile.sh" << 'EOF'
#!/bin/bash

# Compilation script for the Project Manager GUI
echo "Compiling Project Manager GUI..."

# Compile all Java files
javac *.java

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo ""
    echo "To test the integration example:"
    echo "java gui.org.drbpatch.IntegrationExample"
else
    echo "✗ Compilation failed. Please check for errors above."
    exit 1
fi
EOF

chmod +x "$PACKAGE_DIR/compile.sh"

echo ""
echo "✓ Integration package created successfully!"
echo ""
echo "Package contents:"
ls -la "$PACKAGE_DIR/"
echo ""
echo "To use this package:"
echo "1. Copy the files from $PACKAGE_DIR/ to your backend project"
echo "2. Implement BackendInterface in your backend service"
echo "3. Create a main class similar to IntegrationExample.java"
echo "4. Compile and run"
echo ""
echo "See README.md in the package for detailed instructions."
echo ""
echo "=== PACKAGE CREATION COMPLETE ===" 