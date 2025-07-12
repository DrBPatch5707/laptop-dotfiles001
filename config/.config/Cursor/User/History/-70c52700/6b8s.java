package org.drbpatch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Config holds IDE configuration and default IDE selection for the application.
 * Used for serialization/deserialization with Jackson.
 */
public class Config {
    //region Fields
    /** Default IDE id for opening projects. */
    @JsonProperty("defaultIdeId")
    private String defaultIdeId;
    /** Map of IDE id to IDE configuration. */
    @JsonProperty("ides")
    private Map<String, IDE> ides;
    //endregion

    /**
     * Constructs a new Config with an empty IDE map.
     */
    public Config() {
        this.ides = new HashMap<>();
    }

    /**
     * Gets the default IDE id.
     * @return Default IDE id
     */
    public String getDefaultIdeId() {
        return defaultIdeId;
    }
    /**
     * Sets the default IDE id.
     * @param defaultIdeId The id to set
     */
    public void setDefaultIdeId(String defaultIdeId) {
        this.defaultIdeId = defaultIdeId;
    }
    /**
     * Gets the map of IDEs.
     * @return Map of IDE id to IDE
     */
    public Map<String, IDE> getIdes() {
        return ides;
    }
    /**
     * Sets the map of IDEs.
     * @param ides The map to set
     */
    public void setIdes(Map<String, IDE> ides) {
        this.ides = ides;
    }
    /**
     * Gets an IDE by id (ignored by Jackson).
     * @param id The IDE id
     * @return The IDE, or null if not found
     */
    @JsonIgnore
    public IDE getIdeById(String id) {
        return ides.get(id);
    }
    /**
     * Adds an IDE to the map (ignored by Jackson).
     * @param id The IDE id
     * @param entry The IDE to add
     */
    @JsonIgnore
    public void addIde(String id, IDE entry) {
        ides.put(id, entry);
    }
}
