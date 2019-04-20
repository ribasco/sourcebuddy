package com.ibasco.sourcebuddy.util;

import org.apache.commons.lang3.StringUtils;

public class Predicates {

    public static boolean isValidPort(String s) {
        if (!StringUtils.isBlank(s) && StringUtils.isNumeric(s)) {
            int port = Integer.valueOf(s);
            return port > 0 && port < (Short.MAX_VALUE * 2);

        }
        return false;
    }
}
