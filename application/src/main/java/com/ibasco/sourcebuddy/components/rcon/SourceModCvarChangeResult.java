package com.ibasco.sourcebuddy.components.rcon;

public class SourceModCvarChangeResult {

    private String name;

    private String value;

    public SourceModCvarChangeResult() {
    }

    public SourceModCvarChangeResult(String name, String value) {
        this.name = name;
        this.value = value;
    }

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

    @Override
    public String toString() {
        return "SourceModCvarChangeResult{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
