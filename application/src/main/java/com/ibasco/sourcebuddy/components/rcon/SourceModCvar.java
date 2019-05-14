package com.ibasco.sourcebuddy.components.rcon;

import java.util.List;

public class SourceModCvar {

    private String name;

    private String value;

    private List<String> types;

    private String description;

    private boolean command;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCommand() {
        if (value != null)
            return "cmd".equalsIgnoreCase(value.trim());
        return false;
    }

    @Override
    public String toString() {
        return "Cvar{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", types=" + types +
                ", description='" + description + '\'' +
                '}';
    }
}
