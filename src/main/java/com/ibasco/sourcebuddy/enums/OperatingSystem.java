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

    public char getCode() {
        return code;
    }

    public static OperatingSystem valueOf(char code) {
        return Arrays.stream(values()).filter(o -> o.code == code).findFirst().orElse(null);
    }
}
