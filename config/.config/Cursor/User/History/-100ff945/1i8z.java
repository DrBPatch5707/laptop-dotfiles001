package gui.org.drbpatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object (DTO) representing an editor profile in the Project Manager application.
 * This class defines how to launch external editors for opening project files.
 * 
 * <p>Editor profiles are used in the Settings dialog to configure which editors
 * can be used to open projects. Each profile contains the executable path and
 * optional command-line arguments.</p>
 * 
 * <p>Usage in GUI:
 * - Displayed in SettingsDialog as a list of available editors
 * - Used when user selects "Open Project With..." from the main GUI
 * - Managed through Settings dialog (add, edit, remove profiles)</p>
 * 
 * @author Project Manager GUI
 * @version 1.0
 */
public class EditorProfile {
    
    /**
     * Human-readable name for the editor profile.
     * Used for display in GUI lists and menus.
     * Examples: "VS Code", "IntelliJ IDEA", "Sublime Text"
     */
    private String name;
    
    /**
     * Full path to the editor's executable file.
     * Should be an absolute path that can be executed by the system.
     * Examples: "/usr/bin/code", "C:\\Program Files\\Microsoft VS Code\\Code.exe"
     */
    private String executablePath;
    
    /**
     * List of command-line arguments to pass to the editor executable.
     * These arguments are typically used to specify the project folder to open.
     * Examples: ["."], ["--new-window"], ["-n", "."]
     * 
     * <p>When launching the editor, the backend should:
     * 1. Replace any placeholder arguments (like ".") with the actual project path
     * 2. Append the project path if no placeholder is specified
     * 3. Handle platform-specific argument formatting</p>
     */
    private List<String> commandLineArgs;
    
    /**
     * Default constructor for creating new editor profiles.
     * Initializes with empty values that should be set by the user.
     */
    public EditorProfile() {
        this.name = "";
        this.executablePath = "";
        this.commandLineArgs = new ArrayList<>();
    }
    
    /**
     * Constructor for creating editor profiles with initial data.
     * 
     * @param name Human-readable name for the editor
     * @param executablePath Path to the editor executable
     * @param commandLineArgs List of command-line arguments
     */
    public EditorProfile(String name, String executablePath, List<String> commandLineArgs) {
        this.name = name != null ? name : "";
        this.executablePath = executablePath != null ? executablePath : "";
        this.commandLineArgs = commandLineArgs != null ? new ArrayList<>(commandLineArgs) : new ArrayList<>();
    }
    
    /**
     * Constructor for creating editor profiles with initial data.
     * Convenience constructor that takes command-line arguments as varargs.
     * 
     * @param name Human-readable name for the editor
     * @param executablePath Path to the editor executable
     * @param commandLineArgs Command-line arguments as varargs
     */
    public EditorProfile(String name, String executablePath, String... commandLineArgs) {
        this.name = name != null ? name : "";
        this.executablePath = executablePath != null ? executablePath : "";
        this.commandLineArgs = new ArrayList<>();
        if (commandLineArgs != null) {
            for (String arg : commandLineArgs) {
                if (arg != null) {
                    this.commandLineArgs.add(arg);
                }
            }
        }
    }
    
    // ==================== GETTERS ====================
    
    /**
     * Gets the human-readable name for this editor profile.
     * @return Editor name, never null (empty string if not set)
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the path to the editor executable.
     * @return Executable path, never null (empty string if not set)
     */
    public String getExecutablePath() {
        return executablePath;
    }
    
    /**
     * Gets the list of command-line arguments for this editor.
     * @return List of command-line arguments, never null (empty list if not set)
     */
    public List<String> getCommandLineArgs() {
        return new ArrayList<>(commandLineArgs); // Return a copy to prevent external modification
    }
    
    /**
     * Gets the command-line arguments as an array.
     * Useful for ProcessBuilder and similar APIs.
     * @return Array of command-line arguments
     */
    public String[] getCommandLineArgsArray() {
        return commandLineArgs.toArray(new String[0]);
    }
    
    // ==================== SETTERS ====================
    
    /**
     * Sets the human-readable name for this editor profile.
     * @param name Editor name (null values are converted to empty string)
     */
    public void setName(String name) {
        this.name = name != null ? name : "";
    }
    
    /**
     * Sets the path to the editor executable.
     * @param executablePath Executable path (null values are converted to empty string)
     */
    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath != null ? executablePath : "";
    }
    
    /**
     * Sets the command-line arguments for this editor.
     * @param commandLineArgs List of arguments (null values result in empty list)
     */
    public void setCommandLineArgs(List<String> commandLineArgs) {
        this.commandLineArgs = commandLineArgs != null ? new ArrayList<>(commandLineArgs) : new ArrayList<>();
    }
    
    /**
     * Sets the command-line arguments using varargs.
     * @param commandLineArgs Command-line arguments as varargs
     */
    public void setCommandLineArgs(String... commandLineArgs) {
        this.commandLineArgs.clear();
        if (commandLineArgs != null) {
            for (String arg : commandLineArgs) {
                if (arg != null) {
                    this.commandLineArgs.add(arg);
                }
            }
        }
    }
    
    /**
     * Adds a command-line argument to the existing list.
     * @param arg Argument to add (null values are ignored)
     */
    public void addCommandLineArg(String arg) {
        if (arg != null) {
            this.commandLineArgs.add(arg);
        }
    }
    
    /**
     * Removes a command-line argument from the list.
     * @param arg Argument to remove
     * @return true if the argument was found and removed, false otherwise
     */
    public boolean removeCommandLineArg(String arg) {
        return this.commandLineArgs.remove(arg);
    }
    
    /**
     * Clears all command-line arguments.
     */
    public void clearCommandLineArgs() {
        this.commandLineArgs.clear();
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Checks if this editor profile has all required fields filled.
     * @return true if both name and executablePath are not empty
     */
    public boolean isValid() {
        return !name.trim().isEmpty() && !executablePath.trim().isEmpty();
    }
    
    /**
     * Creates a copy of this editor profile.
     * Useful for editing operations where you want to preserve the original.
     * 
     * @return A new EditorProfile instance with the same data
     */
    public EditorProfile copy() {
        return new EditorProfile(name, executablePath, commandLineArgs);
    }
    
    /**
     * Gets a display string for this editor profile.
     * Shows the name and executable path for easy identification.
     * 
     * @return Formatted string showing name and executable path
     */
    public String getDisplayString() {
        if (name.trim().isEmpty()) {
            return executablePath;
        }
        return name + " (" + executablePath + ")";
    }
    
    /**
     * Creates a command array suitable for ProcessBuilder.
     * Combines the executable path with command-line arguments.
     * 
     * @return Array containing executable path followed by arguments
     */
    public String[] createCommandArray() {
        List<String> command = new ArrayList<>();
        command.add(executablePath);
        command.addAll(commandLineArgs);
        return command.toArray(new String[0]);
    }
    
    // ==================== STATIC FACTORY METHODS ====================
    
    /**
     * Creates a VS Code editor profile with common settings.
     * @return Pre-configured VS Code profile
     */
    public static EditorProfile createVSCodeProfile() {
        return new EditorProfile("VS Code", "code", "--new-window", ".");
    }
    
    /**
     * Creates an IntelliJ IDEA editor profile with common settings.
     * @return Pre-configured IntelliJ IDEA profile
     */
    public static EditorProfile createIntelliJProfile() {
        return new EditorProfile("IntelliJ IDEA", "idea", ".");
    }
    
    /**
     * Creates a Sublime Text editor profile with common settings.
     * @return Pre-configured Sublime Text profile
     */
    public static EditorProfile createSublimeTextProfile() {
        return new EditorProfile("Sublime Text", "subl", ".");
    }
    
    /**
     * Creates a Vim editor profile with common settings.
     * @return Pre-configured Vim profile
     */
    public static EditorProfile createVimProfile() {
        return new EditorProfile("Vim", "vim", ".");
    }
    
    // ==================== OBJECT METHODS ====================
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EditorProfile that = (EditorProfile) obj;
        return Objects.equals(name, that.name) && 
               Objects.equals(executablePath, that.executablePath);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, executablePath);
    }
    
    @Override
    public String toString() {
        return "EditorProfile{" +
                "name='" + name + '\'' +
                ", executablePath='" + executablePath + '\'' +
                ", commandLineArgs=" + commandLineArgs +
                '}';
    }
} 