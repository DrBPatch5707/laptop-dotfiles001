# Project Manager

**Project Manager** is a modern, cross-platform desktop application for managing software projects, designed to help developers and teams organize, track, and access their projects efficiently. It features a user-friendly GUI, robust project detection, and seamless integration with your preferred IDEs.

---

## Features

- **Automatic Project Detection:** Scans your root directory and intelligently identifies projects, even across complex folder structures.
- **Project CRUD:** Easily add, edit, view, and delete projects with detailed metadata (name, description, status, priority, category, etc.).
- **IDE Integration:** Configure your favorite IDEs and open projects directly from the app.
- **Filesystem Sync:** Keeps your project list in sync with the actual filesystem, detecting new, missing, or mismatched projects.
- **Notifications:** Get notified about unregistered or orphaned projects and resolve discrepancies with guided prompts.
- **Settings & Customization:** Change your root directory, manage IDEs, and reset your environment with a single click.
- **Safe Reset:** Completely reset your database and configuration with built-in verification and first-time setup guidance.

---

## Getting Started

### Prerequisites

- **Java 11+** (JDK)
- **Gradle** (or use the included `gradlew.bat` on Windows)
- A supported operating system (Windows, Linux, macOS)

### Build & Run

1. **Clone the repository:**
   ```sh
   git clone https://github.com/yourusername/project-manager.git
   cd project-manager
   ```

2. **Build and run the application:**
   ```sh
   gradle run
   ```
   Or on Windows:
   ```sh
   gradlew.bat run
   ```

3. **First-Time Setup:**
   - On first launch (or after a reset), you’ll be prompted to set your root directory and review detected projects.
   - Follow the on-screen instructions to configure your environment.

---

## Usage

- **Add/Edit Projects:** Use the GUI to add new projects or edit existing ones. Specify details like name, description, status, priority, and category.
- **Access Projects:** Select a project and open it in your configured IDE. If no IDEs are configured, go to Settings to add one.
- **Sync & Notifications:** The app automatically syncs with your filesystem and notifies you of any discrepancies.
- **Settings:** Change your root directory, manage IDEs, or reset your environment from the Settings menu.

---

## Project Structure

- `src/main/java/org/drbpatch/` — Main application source code
- `build.gradle` — Build configuration
- `README.md` — This file

---

## Contributing

Contributions are welcome! Please open issues or submit pull requests for new features, bug fixes, or improvements.

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

## Acknowledgements

- Inspired by the needs of developers managing multiple projects across diverse environments.
- Built with Java Swing for a responsive, cross-platform GUI.

---

**Questions or feedback?**  
Open an issue or contact the maintainer via GitHub.


**Message from the developer**
A large portion of this project was vibe coded including this read me, and especially the front end.
Ps I know its absolute garbage.