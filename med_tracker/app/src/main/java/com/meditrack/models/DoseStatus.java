package com.meditrack.models;

public enum DoseStatus {
    TAKEN("Taken"),
    MISSED("Missed"),
    SKIPPED("Skipped");

    private final String displayName;

    DoseStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DoseStatus fromString(String status) {
        for (DoseStatus ds : DoseStatus.values()) {
            if (ds.name().equalsIgnoreCase(status)) {
                return ds;
            }
        }
        return MISSED; // Default
    }
}
