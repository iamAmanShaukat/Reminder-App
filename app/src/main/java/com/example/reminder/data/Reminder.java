package com.example.reminder.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "reminders")
public class Reminder implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private long timeMillis;
    private boolean isAllDay;
    private String repeatMode; // NONE, DAILY, WEEKLY, MONTHLY
    private boolean isCompleted;
    private int color; // Color int

    // Empty constructor for Room
    public Reminder() {
    }

    @androidx.room.Ignore
    public Reminder(String title, String description, long timeMillis, boolean isAllDay, String repeatMode,
            long repeatInterval, int color) {
        this.title = title;
        this.description = description;
        this.timeMillis = timeMillis;
        this.isAllDay = isAllDay;
        this.repeatMode = repeatMode;
        this.repeatInterval = repeatInterval;
        this.color = color;
        this.isCompleted = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
    }

    public String getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(String repeatMode) {
        this.repeatMode = repeatMode;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    private long repeatInterval; // Custom interval in milliseconds
    private boolean hideFromWidget; // If true, this reminder won't appear on the widget

    public long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public boolean isHideFromWidget() {
        return hideFromWidget;
    }

    public void setHideFromWidget(boolean hideFromWidget) {
        this.hideFromWidget = hideFromWidget;
    }

    // Advanced Repeat Options
    private String repeatDays; // CSV of days (1=Sun, 2=Mon, etc.)
    private Integer windowStart; // Minutes from midnight (e.g. 480 = 8:00 AM)
    private Integer windowEnd; // Minutes from midnight (e.g. 1200 = 8:00 PM)

    public String getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(String repeatDays) {
        this.repeatDays = repeatDays;
    }

    public Integer getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(Integer windowStart) {
        this.windowStart = windowStart;
    }

    public Integer getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(Integer windowEnd) {
        this.windowEnd = windowEnd;
    }
}
