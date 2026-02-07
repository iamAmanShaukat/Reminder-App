package com.example.reminder.templates;

public enum TemplateCategory {
    ALL("All"),
    HEALTH("Health & Wellness"),
    PRODUCTIVITY("Productivity"),
    PERSONAL("Personal Care");

    private final String displayName;

    TemplateCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
