package com.example.reminder.templates;

public class TimeSlot {
    private final int hour;
    private final int minute;
    private final String label;

    public TimeSlot(int hour, int minute, String label) {
        this.hour = hour;
        this.minute = minute;
        this.label = label;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getLabel() {
        return label;
    }

    public String getFormattedTime() {
        int displayHour = hour;
        String amPm = "AM";

        if (hour >= 12) {
            amPm = "PM";
            if (hour > 12) {
                displayHour = hour - 12;
            }
        }
        if (displayHour == 0) {
            displayHour = 12;
        }

        return String.format("%d:%02d %s", displayHour, minute, amPm);
    }
}
