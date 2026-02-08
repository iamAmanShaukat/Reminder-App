package com.example.reminder.ui;

import com.example.reminder.data.Reminder;

public class ReminderItem implements ListItem {
    private final Reminder reminder;

    public ReminderItem(Reminder reminder) {
        this.reminder = reminder;
    }

    public Reminder getReminder() {
        return reminder;
    }

    @Override
    public int getType() {
        return TYPE_REMINDER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ReminderItem that = (ReminderItem) o;
        // Use unique ID for equality
        return reminder.getId() == that.reminder.getId() &&
                reminder.getTimeMillis() == that.reminder.getTimeMillis() &&
                reminder.isCompleted() == that.reminder.isCompleted();
    }

    // Helper to check identity separate from content
    public boolean isSameItem(ReminderItem other) {
        return reminder.getId() == other.reminder.getId();
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(reminder.getId(), reminder.getTimeMillis(), reminder.isCompleted());
    }
}
