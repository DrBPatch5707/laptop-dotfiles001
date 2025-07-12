package org.drbpatch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class Config {
    @JsonProperty("defaultIdeId")
    private String defaultIdeId;

    @JsonProperty("ides")
    private Map<String, IDE> ides;

    public Config() {
        this.ides = new HashMap<>();
    }

    public static void init() {

    }

    public String getDefaultIdeId() {
        return defaultIdeId;
    }

    public void setDefaultIdeId(String defaultIdeId) {
        this.defaultIdeId = defaultIdeId;
    }

    public Map<String, IDE> getIdes() {
        return ides;
    }

    public void setIdes(Map<String, IDE> ides) {
        this.ides = ides;
    }
    @JsonIgnore
    public IDE getIdeById(String id) {
        return ides.get(id);
    }

    @JsonIgnore
    public void addIde(String id, IDE entry) {
        ides.put(id, entry);
    }
}
