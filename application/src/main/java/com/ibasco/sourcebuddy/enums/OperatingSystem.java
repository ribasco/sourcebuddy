package com.ibasco.sourcebuddy.enums;

import java.util.Arrays;

public enum OperatingSystem {
    WINDOWS('w'),
    MAC('m'),
    LINUX('l');

    private char code;

    OperatingSystem(char code) {
        this.code = code;
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
}