package com.example.reminder.templates;

import java.util.ArrayList;
import java.util.List;

public class ReminderTemplate {
    private final String id;
    private final String emoji;
    private final String title;
    private final String description;
    private final String repeatMode;
    private final long repeatInterval;
    private final List<TimeSlot> suggestedTimes;
    private final TemplateCategory category;

    private ReminderTemplate(Builder builder) {
        this.id = builder.id;
        this.emoji = builder.emoji;
        this.title = builder.title;
        this.description = builder.description;
        this.repeatMode = builder.repeatMode;
        this.repeatInterval = builder.repeatInterval;
        this.suggestedTimes = builder.suggestedTimes;
        this.category = builder.category;
    }

    public String getId() {
        return id;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRepeatMode() {
        return repeatMode;
    }

    public long getRepeatInterval() {
        return repeatInterval;
    }

    public List<TimeSlot> getSuggestedTimes() {
        return new ArrayList<>(suggestedTimes);
    }

    public TemplateCategory getCategory() {
        return category;
    }

    public String getFrequencyText() {
        if ("DAILY".equals(repeatMode)) {
            int count = suggestedTimes.size();
            return count + " time" + (count > 1 ? "s" : "") + " daily";
        } else if ("CUSTOM".equals(repeatMode)) {
            long hours = repeatInterval / (60 * 60 * 1000);
            if (hours > 0) {
                return "Every " + hours + " hour" + (hours > 1 ? "s" : "");
            }
            long minutes = repeatInterval / (60 * 1000);
            return "Every " + minutes + " minute" + (minutes > 1 ? "s" : "");
        }
        return "Once daily";
    }

    public static class Builder {
        private String id;
        private String emoji;
        private String title;
        private String description;
        private String repeatMode = "DAILY";
        private long repeatInterval = 0;
        private List<TimeSlot> suggestedTimes = new ArrayList<>();
        private TemplateCategory category = TemplateCategory.ALL;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder emoji(String emoji) {
            this.emoji = emoji;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder repeatMode(String repeatMode) {
            this.repeatMode = repeatMode;
            return this;
        }

        public Builder repeatInterval(long repeatInterval) {
            this.repeatInterval = repeatInterval;
            return this;
        }

        public Builder addTime(int hour, int minute, String label) {
            this.suggestedTimes.add(new TimeSlot(hour, minute, label));
            return this;
        }

        public Builder category(TemplateCategory category) {
            this.category = category;
            return this;
        }

        public ReminderTemplate build() {
            return new ReminderTemplate(this);
        }
    }
}
