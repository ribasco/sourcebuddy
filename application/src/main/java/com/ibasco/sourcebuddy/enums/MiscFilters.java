package com.ibasco.sourcebuddy.enums;

import com.ibasco.sourcebuddy.domain.ServerDetails;

import java.util.function.Function;

public enum MiscFilters {
    DEDICATED("Dedicated", ServerDetails::isDedicated),
    NON_DEDICATED("Non-dedicated", ServerDetails::isNonDedicated),
    NON_EMPTY_SERVERS("Non-empty servers", ServerDetails::isNotEmpty),
    EMPTY_SERVERS("Empty servers", ServerDetails::isEmpty),
    SECURED("Secured", ServerDetails::isSecure),
    INSECURE("Insecure", ServerDetails::isNotSecure);

    private String description;

    private final Function<ServerDetails, Boolean> mapper;

    MiscFilters(String description, Function<ServerDetails, Boolean> mapper) {
        this.description = description;
        this.mapper = mapper;
    }

    public String getDescription() {
        return description;
    }

    public Function<ServerDetails, Boolean> getMapper() {
        return mapper;
    }
}
