package com.example.reminder.domain.usecase;

import com.example.reminder.data.Reminder;
import com.example.reminder.data.ReminderRepository;
import com.example.reminder.service.AlarmScheduler;
import com.example.reminder.service.WidgetRefreshService;
import com.example.reminder.utils.SmartCompletionUtils;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Use case for completing a reminder.
 * Encapsulates the business logic for smart completion of reminders,
 * including rescheduling for repeating tasks.
 */
@Singleton
public class CompleteReminderUseCase {

    private final ReminderRepository repository;
    private final AlarmScheduler alarmScheduler;
    private final WidgetRefreshService widgetRefreshService;

    @Inject
    public CompleteReminderUseCase(
            ReminderRepository repository,
            AlarmScheduler alarmScheduler,
            WidgetRefreshService widgetRefreshService) {
        this.repository = repository;
        this.alarmScheduler = alarmScheduler;
        this.widgetRefreshService = widgetRefreshService;
    }

    /**
     * Execute the completion action on a reminder.
     * 
     * @param reminder    The reminder to complete
     * @param isCompleted true to mark complete, false to mark incomplete
     */
    public void execute(Reminder reminder, boolean isCompleted) {
        if (isCompleted) {
            handleCompletion(reminder);
        } else {
            handleUncompletion(reminder);
        }
        widgetRefreshService.refresh();
    }

    private void handleCompletion(Reminder originalReminder) {
        // Create a copy to avoid mutating the object displayed in the UI
        Reminder reminder = originalReminder.copy();

        if (isRepeating(reminder)) {
            // Smart Completion: Reschedule instead of marking complete
            long nextTime = SmartCompletionUtils.getNextReminderTime(reminder);
            if (nextTime > System.currentTimeMillis()) {
                reminder.setTimeMillis(nextTime);
                reminder.setCompleted(false); // Keep incomplete for next instance
                repository.update(reminder);
                alarmScheduler.schedule(reminder);
            } else {
                // Fallback: If no future time found, mark complete
                reminder.setCompleted(true);
                repository.update(reminder);
            }
        } else {
            // Standard completion
            reminder.setCompleted(true);
            repository.update(reminder);
        }
    }

    private void handleUncompletion(Reminder originalReminder) {
        Reminder reminder = originalReminder.copy();
        reminder.setCompleted(false);
        repository.update(reminder);
        // Ensure alarm is active if the time is in the future
        alarmScheduler.schedule(reminder);
    }

    private boolean isRepeating(Reminder reminder) {
        String mode = reminder.getRepeatMode();
        return (mode != null && !"NONE".equals(mode)) || reminder.isHideFromWidget();
    }
}
