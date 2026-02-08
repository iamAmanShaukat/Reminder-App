package com.example.reminder.ui.addedit;

import com.example.reminder.data.Reminder;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Validator for reminder data.
 * Extracts validation logic from AddEditFragment.
 */
@Singleton
public class ReminderValidator {

    @Inject
    public ReminderValidator() {
    }

    /**
     * Result of validation with error message if invalid.
     */
    public static class ValidationResult {
        public final boolean isValid;
        public final String errorField;
        public final String errorMessage;

        private ValidationResult(boolean isValid, String errorField, String errorMessage) {
            this.isValid = isValid;
            this.errorField = errorField;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null, null);
        }

        public static ValidationResult invalid(String field, String message) {
            return new ValidationResult(false, field, message);
        }
    }

    /**
     * Validate reminder before saving.
     */
    public ValidationResult validate(String title, String description, long timeMillis, String repeatMode) {
        if (title == null || title.trim().isEmpty()) {
            return ValidationResult.invalid("title", "Title is required");
        }

        if (title.length() > 200) {
            return ValidationResult.invalid("title", "Title must be less than 200 characters");
        }

        // Allow past times only for repeating reminders
        if (timeMillis <= System.currentTimeMillis() && "NONE".equals(repeatMode)) {
            return ValidationResult.invalid("time", "Please select a future time");
        }

        return ValidationResult.valid();
    }

    /**
     * Calculate whether reminder should be hidden from widget (grouped as
     * "Everyday").
     * 
     * Rules:
     * - Daily Routine toggle = always hidden
     * - DAILY mode = hidden (7 days >= 5)
     * - WEEKLY/MONTHLY = visible (< 5 days)
     * - CUSTOM with >= 5 days = hidden
     * - CUSTOM with < 5 days = visible
     */
    public boolean shouldHideFromWidget(boolean isDailyRoutine, String repeatMode, String repeatDays) {
        if (isDailyRoutine) {
            return true;
        }

        if ("DAILY".equals(repeatMode)) {
            return true;
        }

        if ("WEEKLY".equals(repeatMode) || "MONTHLY".equals(repeatMode)) {
            return false;
        }

        if ("CUSTOM".equals(repeatMode)) {
            int count = 7; // Default to all days
            if (repeatDays != null && !repeatDays.isEmpty()) {
                count = repeatDays.split(",").length;
            }
            return count >= 5;
        }

        return false; // NONE or unknown
    }

    /**
     * Build a Reminder object from input parameters.
     * Applies all business rules for visibility and repeat configuration.
     */
    public Reminder buildReminder(
            Reminder existing,
            String title,
            String description,
            long timeMillis,
            boolean isDailyRoutine,
            String repeatMode,
            long intervalMillis,
            String repeatDays,
            int windowStart,
            int windowEnd) {

        Reminder reminder;
        if (existing != null) {
            reminder = existing;
            reminder.setTitle(title.trim());
            reminder.setDescription(description != null ? description.trim() : "");
            reminder.setTimeMillis(timeMillis);
        } else {
            reminder = new Reminder(
                    title.trim(),
                    description != null ? description.trim() : "",
                    timeMillis,
                    false,
                    repeatMode,
                    intervalMillis,
                    0);
        }

        reminder.setRepeatMode(repeatMode);
        reminder.setRepeatInterval(intervalMillis);
        reminder.setRepeatDays(repeatDays);
        reminder.setWindowStart(windowStart);
        reminder.setWindowEnd(windowEnd);
        reminder.setHideFromWidget(shouldHideFromWidget(isDailyRoutine, repeatMode, repeatDays));
        reminder.setAllDay(false); // We use specific times

        return reminder;
    }
}
