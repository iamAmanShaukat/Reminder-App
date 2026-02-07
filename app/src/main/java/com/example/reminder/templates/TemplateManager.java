package com.example.reminder.templates;

import java.util.ArrayList;
import java.util.List;

public class TemplateManager {

    private static TemplateManager instance;
    private final List<ReminderTemplate> templates;

    private TemplateManager() {
        templates = new ArrayList<>();
        initializeTemplates();
    }

    public static synchronized TemplateManager getInstance() {
        if (instance == null) {
            instance = new TemplateManager();
        }
        return instance;
    }

    private void initializeTemplates() {
        // Health & Wellness Templates
        templates.add(new ReminderTemplate.Builder()
                .id("medicine")
                .emoji("💊")
                .title("Take Medicine")
                .description("Remember to take your medication")
                .repeatMode("DAILY")
                .addTime(8, 0, "Morning")
                .addTime(14, 0, "Afternoon")
                .addTime(20, 0, "Evening")
                .category(TemplateCategory.HEALTH)
                .build());

        templates.add(new ReminderTemplate.Builder()
                .id("water")
                .emoji("💧")
                .title("Drink Water")
                .description("Stay hydrated! Drink a glass of water")
                .repeatMode("CUSTOM")
                .repeatInterval(2 * 60 * 60 * 1000L) // 2 hours
                .addTime(8, 0, "Start")
                .category(TemplateCategory.HEALTH)
                .build());

        templates.add(new ReminderTemplate.Builder()
                .id("exercise")
                .emoji("🏃")
                .title("Exercise")
                .description("Time for your daily workout")
                .repeatMode("DAILY")
                .addTime(7, 0, "Morning")
                .category(TemplateCategory.HEALTH)
                .build());

        templates.add(new ReminderTemplate.Builder()
                .id("sleep")
                .emoji("😴")
                .title("Sleep Reminder")
                .description("Wind down and prepare for bed")
                .repeatMode("DAILY")
                .addTime(22, 0, "Night")
                .category(TemplateCategory.HEALTH)
                .build());

        // Productivity Templates
        templates.add(new ReminderTemplate.Builder()
                .id("study")
                .emoji("📚")
                .title("Study Time")
                .description("Focus time for learning")
                .repeatMode("DAILY")
                .addTime(18, 0, "Evening")
                .category(TemplateCategory.PRODUCTIVITY)
                .build());

        templates.add(new ReminderTemplate.Builder()
                .id("break")
                .emoji("☕")
                .title("Break Time")
                .description("Take a short break")
                .repeatMode("CUSTOM")
                .repeatInterval(90 * 60 * 1000L) // 90 minutes
                .addTime(9, 0, "Start")
                .category(TemplateCategory.PRODUCTIVITY)
                .build());

        // Personal Care Templates
        templates.add(new ReminderTemplate.Builder()
                .id("brush_teeth")
                .emoji("🪥")
                .title("Brush Teeth")
                .description("Maintain dental hygiene")
                .repeatMode("DAILY")
                .addTime(8, 0, "Morning")
                .addTime(22, 0, "Night")
                .category(TemplateCategory.PERSONAL)
                .build());

        templates.add(new ReminderTemplate.Builder()
                .id("meditation")
                .emoji("🧘")
                .title("Meditation")
                .description("Take time to meditate and relax")
                .repeatMode("DAILY")
                .addTime(9, 0, "Morning")
                .category(TemplateCategory.PERSONAL)
                .build());
    }

    public List<ReminderTemplate> getAllTemplates() {
        return new ArrayList<>(templates);
    }

    public List<ReminderTemplate> getTemplatesByCategory(TemplateCategory category) {
        if (category == TemplateCategory.ALL) {
            return getAllTemplates();
        }

        List<ReminderTemplate> filtered = new ArrayList<>();
        for (ReminderTemplate template : templates) {
            if (template.getCategory() == category) {
                filtered.add(template);
            }
        }
        return filtered;
    }

    public ReminderTemplate getTemplateById(String id) {
        for (ReminderTemplate template : templates) {
            if (template.getId().equals(id)) {
                return template;
            }
        }
        return null;
    }
}
