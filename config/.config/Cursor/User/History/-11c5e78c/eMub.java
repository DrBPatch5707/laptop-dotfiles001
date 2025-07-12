package org.drbpatch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IDE {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("path")
    private String path;
    @JsonProperty("args")
    private String args;
    
    public IDE(String id,String name, String path, String args) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.args = args;
    }



    public void setPath(String path) {
        this.path = path;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public IDE() {
        // Default constructor needed by Jackson for deserialization
    }
    @Override
    public String toString() {
        return ("IDE{" +
                "name='" + name + '\'' +
                ",path='" + path + '\'' +
                ",args='" + args + '\'' +
                "}");
    }
}
