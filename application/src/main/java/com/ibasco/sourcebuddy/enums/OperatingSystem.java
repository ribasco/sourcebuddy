package com.ibasco.sourcebuddy.enums;

import java.util.Arrays;

public enum OperatingSystem {
    WINDOWS('w', "Windows"),
    MAC('m', "Mac OS"),
    LINUX('l', "Linux");

    private char code;

    private String name;

    OperatingSystem(char code, String name) {
        this.code = code;
        this.name = name;
    }

    public static OperatingSystem valueOf(char code) {
        return Arrays.stream(values()).filter(o -> o.code == code).findFirst().orElse(null);
    }

    public static OperatingSystem valueOfStr(String code) {
        return Arrays.stream(values()).filter(o -> String.valueOf(o.code).equalsIgnoreCase(code)).findFirst().orElse(null);
    }

    public char getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
