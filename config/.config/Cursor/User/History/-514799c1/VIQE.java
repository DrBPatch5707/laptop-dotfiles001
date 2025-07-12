package org.drbpatch;

import java.util.Map;

/**
 * IDEManager handles IDE configuration and access for the application.
 * All methods are static.
 */
public class IDEManager {
    /**
     * Returns the configured IDEs.
     * @return Map of IDE id to IDE
     */
    public static Map<String, IDE> getIdes() {
        return Main.config.getIdes();
    }

    /**
     * Adds an IDE to the config.
     * @param ide The IDE to add
     * @return true if successful, false otherwise
     */
    public static boolean addIDE(IDE ide) {
        if (ide.getId() == null || ide.getId().trim().isEmpty()) {
            return false;
        }
        Main.config.addIde(ide.getId(), ide);
        return true;
    }

    /**
     * Removes an IDE from the config.
     * @param ideId The IDE id
     * @return true if removed, false otherwise
     */
    public static boolean removeIDE(String ideId) {
        if (ideId == null || ideId.trim().isEmpty()) {
            return false;
        }
        IDE removed = Main.config.getIdes().remove(ideId);
        return removed != null;
    }

    /**
     * Gets an IDE by id.
     * @param ideId The IDE id
     * @return The IDE, or null if not found
     */
    public static IDE getIDEById(String ideId) {
        return Main.config.getIdes().get(ideId);
    }
} 