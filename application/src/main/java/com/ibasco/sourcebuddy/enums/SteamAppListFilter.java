package com.ibasco.sourcebuddy.enums;

public enum SteamAppListFilter {
    SHOW_ALL("All"),
    SHOW_BOOKMARKED("Bookmarked");

    private String description;

    SteamAppListFilter(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
