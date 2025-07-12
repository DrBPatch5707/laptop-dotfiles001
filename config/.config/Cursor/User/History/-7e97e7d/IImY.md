# GUI Testing Guide

This guide explains how to test the Project Manager GUI using the provided test backend service.

## Quick Start

### Option 1: Using the Test Script (Recommended)

1. **Navigate to the GUI directory:**
   ```bash
   cd /home/bpatch/src/main/java/gui/org/drbpatch
   ```

2. **Run the test script:**
   ```bash
   ./test_gui.sh
   ```

The script will automatically compile all Java files and launch the test application.

### Option 2: Manual Compilation and Execution

1. **Compile all Java files:**
   ```bash
   javac *.java
   ```

2. **Run the test application:**
   ```bash
   java gui.org.drbpatch.TestProjectManagerApp
   ```

## What the Test Application Provides

### Sample Data
The test application comes with pre-loaded sample data:

**Sample Projects:**
- Sample Web Project (In Development, Priority: High)
- Mobile App (Completed, Priority: High)
- Data Analysis Tool (In Development, Priority: Medium)

**Sample Editor Profiles:**
- VS Code
- IntelliJ IDEA
- Sublime Text

### Console Logging
All backend operations are logged to the console, so you can see:
- When projects are added, updated, or deleted
- When settings are changed
- When folder/editor operations are triggered
- Current state of the application

## Testing Checklist

### ✅ Basic GUI Functionality
- [ ] Application starts without errors
- [ ] Main window displays correctly
- [ ] Project table shows sample projects
- [ ] All buttons are visible and properly labeled
- [ ] Table selection works (buttons enable/disable)

### ✅ Project Management
- [ ] **Add New Project**
  - Click "Add New Project" button
  - Fill out the form (name, description, path, etc.)
  - Click "Browse..." to select a project directory
  - Click "Save" - project should appear in the table
  - Check console for "Added project" message

- [ ] **Edit Project**
  - Select a project in the table
  - Click "Edit Selected Project" button
  - Modify some fields in the dialog
  - Click "Save" - changes should appear in the table
  - Check console for "Updated project" message

- [ ] **Delete Project**
  - Select a project in the table
  - Click "Delete Selected Project" button
  - Confirm deletion in the dialog
  - Project should be removed from the table
  - Check console for "Deleted project" message

### ✅ Project Operations
- [ ] **Open Project Folder**
  - Select a project in the table
  - Click "Open Project Folder" button
  - Check console for "Opening project folder" message

- [ ] **Open Project With...**
  - Select a project in the table
  - Click "Open Project With..." button
  - Check console for "Opening project with editor" message

### ✅ Settings
- [ ] **Access Settings Dialog**
  - Click "Settings" button
  - Dialog should open showing current root directory
  - Editor profiles should be listed in the table

- [ ] **Change Root Directory**
  - In settings dialog, click "Browse..." next to root directory
  - Select a different directory
  - Click "Save Settings"
  - Check console for "Set root directory" message

### ✅ Form Validation
- [ ] **Required Fields**
  - Try to save a project without a name
  - Try to save a project without a path
  - Should see validation error messages

- [ ] **File Chooser**
  - Click "Browse..." buttons in dialogs
  - Should open file/directory chooser dialogs
  - Selected paths should populate the text fields

## Expected Console Output

When you run the test application, you should see output like this:

```
=== PROJECT MANAGER GUI TEST APPLICATION ===
Starting test application...

Test data initialized:
- 3 sample projects
- 3 sample editor profiles
- Root directory: /home/username/Projects

=== CURRENT TEST STATE ===
Projects (3):
  - Sample Web Project (ID: 1, Status: In Development)
  - Mobile App (ID: 2, Status: Completed)
  - Data Analysis Tool (ID: 3, Status: In Development)
Editor Profiles (3):
  - VS Code (/usr/bin/code)
  - IntelliJ IDEA (/usr/bin/idea)
  - Sublime Text (/usr/bin/subl)
Root Directory: /home/username/Projects
==========================

GUI launched successfully!
You can now test all GUI functionality:
- View projects in the table
- Add new projects
- Edit existing projects
- Delete projects
- Open project folders (simulated)
- Open projects with editors (simulated)
- Access settings dialog

Check the console for backend operation logs.
```

## Troubleshooting

### Common Issues

**Problem: Compilation errors**
- **Solution:** Make sure you have Java 17+ installed
- **Solution:** Check that all files are in the correct directory
- **Solution:** Verify all import statements are correct

**Problem: GUI doesn't appear**
- **Solution:** Check console for error messages
- **Solution:** Make sure you're running on a system with GUI support
- **Solution:** Try running with `java -Djava.awt.headless=false`

**Problem: Buttons don't work**
- **Solution:** Check that the GUI is properly integrated with the test backend
- **Solution:** Look for console error messages
- **Solution:** Verify that event handlers are properly set up

**Problem: File chooser doesn't work**
- **Solution:** Make sure you have proper file system permissions
- **Solution:** Check that the directory exists and is accessible

### Debug Mode

To see more detailed information, you can add debug output by modifying the test backend service. The console will show:
- All backend method calls
- Data changes
- Error conditions
- Current application state

## Next Steps After Testing

Once you've verified the GUI works correctly:

1. **Implement your real backend service** following the `BACKEND_INTERFACE_SPEC.txt`
2. **Replace the test backend** with your implementation
3. **Add data persistence** (database, files, etc.)
4. **Implement real file operations** (folder opening, editor launching)
5. **Add error handling** and validation
6. **Test with real data**

## Test Data Reset

The test application uses in-memory data that resets each time you restart the application. This is perfect for testing as it ensures a clean state each time.

If you want to test with different data, you can modify the `initializeTestData()` method in `TestBackendService.java`.

## Performance Testing

The test application is designed to be lightweight and fast. You can test performance by:
- Adding many projects to see how the table handles large datasets
- Testing dialog responsiveness
- Checking memory usage during extended use

This testing setup allows you to verify that your GUI is working correctly before implementing the full backend, saving you time and ensuring a solid foundation for your application. 