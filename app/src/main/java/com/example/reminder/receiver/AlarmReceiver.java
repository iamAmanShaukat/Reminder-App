package com.example.reminder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.reminder.data.Reminder;
import com.example.reminder.data.ReminderRepository;
import com.example.reminder.service.AlarmScheduler;
import com.example.reminder.service.WidgetRefreshService;
import com.example.reminder.utils.NotificationHelper;
import com.example.reminder.utils.SmartCompletionUtils;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

/**
 * Broadcast receiver for alarm events.
 * Handles alarm triggers, snooze, and complete actions from notifications.
 * 
 * Note: Alarm scheduling logic has been extracted to AlarmScheduler service.
 */
@AndroidEntryPoint
public class AlarmReceiver extends BroadcastReceiver {

    @Inject
    ReminderRepository repository;

    @Inject
    AlarmScheduler alarmScheduler;

    @Inject
    WidgetRefreshService widgetRefreshService;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;

        String action = intent.getAction();
        int reminderId = intent.getIntExtra(NotificationHelper.EXTRA_REMINDER_ID, -1);

        if (reminderId == -1)
            return;

        if (action == null) {
            // Triggered by AlarmManager - Show Notification
            handleAlarmTrigger(context, reminderId);
        } else if (NotificationHelper.ACTION_SNOOZE.equals(action)) {
            handleSnooze(context, reminderId);
        } else if (NotificationHelper.ACTION_COMPLETE.equals(action)) {
            handleComplete(context, reminderId);
        }
    }

    private void handleAlarmTrigger(Context context, int reminderId) {
        // Acquire WakeLock to ensure work completes
        android.os.PowerManager pm = (android.os.PowerManager) context.getSystemService(Context.POWER_SERVICE);
        android.os.PowerManager.WakeLock wakeLock = pm.newWakeLock(
                android.os.PowerManager.PARTIAL_WAKE_LOCK,
                "ReminderApp:AlarmReceiver");
        wakeLock.acquire(10 * 60 * 1000L /* 10 minutes */);

        repository.execute(() -> {
            try {
                Reminder reminder = repository.getReminderSync(reminderId);
                if (reminder != null && !reminder.isCompleted()) {
                    NotificationHelper.createNotificationChannel(context);
                    NotificationHelper.showNotification(context, reminder);

                    // Reschedule if repeating
                    rescheduleIfRepeating(reminder);
                }
            } catch (Exception e) {
                // Ignore
            } finally {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
        });
    }

    private void handleSnooze(Context context, int reminderId) {
        androidx.core.app.NotificationManagerCompat.from(context).cancel(reminderId);
        alarmScheduler.scheduleSnooze(reminderId);
    }

    private void handleComplete(Context context, int reminderId) {
        androidx.core.app.NotificationManagerCompat.from(context).cancel(reminderId);

        repository.execute(() -> {
            Reminder reminder = repository.getReminderSync(reminderId);
            if (reminder != null) {
                // Smart Completion: If repeating, DON'T mark complete
                String mode = reminder.getRepeatMode();
                boolean isRepeating = (mode != null && !"NONE".equals(mode)) || reminder.isHideFromWidget();

                if (!isRepeating) {
                    reminder.setCompleted(true);
                    repository.update(reminder);
                }

                widgetRefreshService.refresh();
            }
        });
    }

    private void rescheduleIfRepeating(Reminder reminder) {
        long nextTime = SmartCompletionUtils.getNextReminderTime(reminder);

        if (nextTime > System.currentTimeMillis()) {
            reminder.setTimeMillis(nextTime);
            repository.update(reminder);
            alarmScheduler.schedule(reminder);
        }
    }

    // Static facade methods for backward compatibility during migration
    public static void scheduleAlarm(Context context, Reminder reminder) {
        // Create a temporary scheduler for static calls
        // This will be removed once all callers are migrated to use injected
        // AlarmScheduler
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminder.getId());
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                context, reminder.getId(), intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

        if (reminder.getTimeMillis() > System.currentTimeMillis()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            android.app.AlarmManager.RTC_WAKEUP,
                            reminder.getTimeMillis(),
                            pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        reminder.getTimeMillis(),
                        pendingIntent);
            }
        }
    }

    public static void cancelAlarm(Context context, Reminder reminder) {
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                context, reminder.getId(), intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }
}
