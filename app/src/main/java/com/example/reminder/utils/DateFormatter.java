package com.example.reminder.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Centralized utility for date and time formatting.
 * Extracted to eliminate duplicate formatting logic across the codebase.
 */
@Singleton
public class DateFormatter {

    private static final String PATTERN_DATE = "MMM dd, yyyy";
    private static final String PATTERN_TIME = "hh:mm a";
    private static final String PATTERN_DATE_TIME = "MMM dd, yyyy hh:mm a";
    private static final String PATTERN_DAY_FULL = "EEEE, MMMM d";
    private static final String PATTERN_TIME_24H = "HH:mm";

    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat dateTimeFormat;
    private final SimpleDateFormat dayFullFormat;

    @Inject
    public DateFormatter() {
        Locale locale = Locale.getDefault();
        dateFormat = new SimpleDateFormat(PATTERN_DATE, locale);
        timeFormat = new SimpleDateFormat(PATTERN_TIME, locale);
        dateTimeFormat = new SimpleDateFormat(PATTERN_DATE_TIME, locale);
        dayFullFormat = new SimpleDateFormat(PATTERN_DAY_FULL, locale);
    }

    /**
     * Format a timestamp to date string (e.g., "Jan 15, 2024").
     */
    public String formatDate(long timeMillis) {
        return dateFormat.format(new Date(timeMillis));
    }

    /**
     * Format a timestamp to time string (e.g., "08:30 AM").
     */
    public String formatTime(long timeMillis) {
        return timeFormat.format(new Date(timeMillis));
    }

    /**
     * Format a timestamp to date and time string (e.g., "Jan 15, 2024 08:30 AM").
     */
    public String formatDateTime(long timeMillis) {
        return dateTimeFormat.format(new Date(timeMillis));
    }

    /**
     * Format a timestamp to full day format (e.g., "Monday, January 15").
     */
    public String formatDayFull(long timeMillis) {
        return dayFullFormat.format(new Date(timeMillis));
    }

    /**
     * Get a human-readable date title (Today, Tomorrow, Yesterday, or full date).
     */
    public String getDateTitle(long timeMillis) {
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(timeMillis);

        Calendar now = Calendar.getInstance();

        // Check for Today
        if (isSameDay(target, now)) {
            return "Today";
        }

        // Check for Tomorrow
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        if (isSameDay(target, tomorrow)) {
            return "Tomorrow";
        }

        // Check for Yesterday
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        if (isSameDay(target, yesterday)) {
            return "Yesterday";
        }

        return formatDayFull(timeMillis);
    }

    /**
     * Format minutes from midnight to time string (e.g., 480 -> "08:00 AM").
     */
    public String formatMinutesFromMidnight(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, h);
        c.set(Calendar.MINUTE, m);
        return timeFormat.format(c.getTime());
    }

    /**
     * Get a relative time description (e.g., "in 2 hours", "tomorrow at 9 AM").
     */
    public String getRelativeTime(long timeMillis) {
        long now = System.currentTimeMillis();
        long diff = timeMillis - now;

        if (diff < 0) {
            return "Overdue";
        }

        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);

        if (minutes < 60) {
            return "in " + minutes + " min";
        } else if (hours < 24) {
            return "in " + hours + " hr";
        } else if (days == 1) {
            return "Tomorrow";
        } else {
            return "in " + days + " days";
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
