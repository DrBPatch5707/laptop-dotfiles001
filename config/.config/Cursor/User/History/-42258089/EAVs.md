# Project Manager 02

A Java desktop application for managing software projects, with database-backed storage, filesystem sync, and IDE integration.

## Project Structure

```
project-manager02/
├── build.gradle           # Gradle build file
├── settings.gradle        # Gradle settings
├── src/
│   ├── main/java/org/drbpatch/   # Main Java source code
│   ├── main/resources/           # Application resources (empty by default)
│   ├── test/java/                # Unit tests (add your tests here)
│   └── test/resources/           # Test resources
├── projects.db            # SQLite database file
├── ide_config.json        # IDE configuration (JSON)
├── root.txt               # Stores root directory path
└── ...
```

## Build & Run

1. **Build the project:**
   ```sh
   ./gradlew build
   ```
2. **Run the application:**
   ```sh
   ./gradlew run
   ```
   or run the `org.drbpatch.Main` class from your IDE.

## Features
- Add, edit, delete, and view projects
- Projects stored in SQLite database and synced with filesystem
- Open projects in your configured IDEs
- GUI built with Swing

## How to Add an IDE
- Edit `ide_config.json` or use the Settings dialog in the app

## How to Set the Root Directory
- Use the Settings dialog in the app, or edit `root.txt`

## Testing
- Add your unit tests under `src/test/java/org/drbpatch/`
- Run tests with:
  ```sh
  ./gradlew test
  ```

## Contributing
- Please document your code with Javadoc and inline comments
- Keep classes focused and methods short
- See `BackendAPI.java` and `init.java` for documentation style 