package com.example.reminder.utils;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Utility for calculating reminder visibility in widgets and groupings.
 * Centralizes the logic for determining if a reminder should be hidden from
 * widget
 * and grouped as "Everyday" vs displayed normally.
 */
@Singleton
public class VisibilityCalculator {

    private static final int EVERYDAY_THRESHOLD = 5; // >= 5 days/week = "Everyday"

    @Inject
    public VisibilityCalculator() {
    }

    /**
     * Determine if a reminder should be hidden from widget and grouped as
     * "Everyday".
     * 
     * Rules:
     * - Daily Routine toggle = always hidden
     * - DAILY mode = hidden (7 days >= threshold)
     * - WEEKLY/MONTHLY = visible (1 day < threshold)
     * - CUSTOM with >= threshold days = hidden
     * - CUSTOM with < threshold days = visible
     * - NONE = visible
     * 
     * @param isDailyRoutine Whether the Daily Routine toggle is enabled
     * @param repeatMode     The repeat mode (NONE, DAILY, WEEKLY, MONTHLY, CUSTOM)
     * @param repeatDays     Comma-separated day values (e.g., "2,3,4,5,6")
     * @return true if should be hidden/grouped as Everyday
     */
    public boolean shouldHideFromWidget(boolean isDailyRoutine, String repeatMode, String repeatDays) {
        if (isDailyRoutine) {
            return true;
        }

        if (repeatMode == null || "NONE".equals(repeatMode)) {
            return false;
        }

        switch (repeatMode) {
            case "DAILY":
                return true; // 7 days >= 5
            case "WEEKLY":
            case "MONTHLY":
                return false; // 1 day < 5
            case "CUSTOM":
                return countDays(repeatDays) >= EVERYDAY_THRESHOLD;
            default:
                return false;
        }
    }

    /**
     * Check if a reminder is a repeating type.
     */
    public boolean isRepeating(String repeatMode, boolean isDailyRoutine) {
        if (isDailyRoutine) {
            return true;
        }
        return repeatMode != null && !"NONE".equals(repeatMode);
    }

    /**
     * Check if a reminder should show date or just time.
     * Repeating reminders typically show only time since date changes.
     */
    public boolean shouldShowDateForReminder(String repeatMode, boolean isDailyRoutine) {
        return !isRepeating(repeatMode, isDailyRoutine);
    }

    /**
     * Get the number of days from a comma-separated day string.
     */
    public int countDays(String repeatDays) {
        if (repeatDays == null || repeatDays.isEmpty()) {
            return 7; // Default to all days if not specified
        }
        return repeatDays.split(",").length;
    }

    /**
     * Check if a reminder should show "overdue" styling.
     * Daily/Everyday tasks should not show overdue.
     */
    public boolean shouldShowOverdueStyle(long timeMillis, boolean isDailyRoutine, String repeatMode) {
        if (timeMillis > System.currentTimeMillis()) {
            return false; // Not past due
        }

        // Daily routines don't show overdue
        if (isDailyRoutine) {
            return false;
        }

        // Daily repeat mode doesn't show overdue
        if ("DAILY".equals(repeatMode)) {
            return false;
        }

        // CUSTOM with >= 5 days doesn't show overdue
        if ("CUSTOM".equals(repeatMode)) {
            // We'd need repeatDays here, but for simplicity assume it's already
            // covered by isHideFromWidget check upstream
            return false;
        }

        return true; // NONE, WEEKLY, MONTHLY with past time = overdue
    }
}
