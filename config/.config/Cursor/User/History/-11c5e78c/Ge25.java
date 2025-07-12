package org.drbpatch;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class IDE {
    private String name;
    private String path;
    private String args;
    @JsonIgnore
    String id;
    public IDE(String id,String name, String path, String args) {
        this.name = name;
        this.path = path;
        this.args = args;
        this.id = id;
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
