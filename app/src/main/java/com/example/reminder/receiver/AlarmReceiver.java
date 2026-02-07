package com.example.reminder.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.example.reminder.data.Reminder;
import com.example.reminder.data.ReminderRepository;
import com.example.reminder.utils.NotificationHelper;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class AlarmReceiver extends BroadcastReceiver {

    @Inject
    ReminderRepository repository;

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
            // Acquire WakeLock to ensure work completes
            android.os.PowerManager pm = (android.os.PowerManager) context.getSystemService(Context.POWER_SERVICE);
            android.os.PowerManager.WakeLock wakeLock = pm.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK,
                    "ReminderApp:AlarmReceiver");
            wakeLock.acquire(10 * 60 * 1000L /* 10 minutes */);

            repository.execute(() -> {
                try {
                    Reminder reminder = repository.getReminderSync(reminderId);
                    if (reminder != null && !reminder.isCompleted()) {
                        NotificationHelper.createNotificationChannel(context);
                        NotificationHelper.showNotification(context, reminder);

                        // Reschedule if repeating
                        rescheduleIfRepeating(context, reminder);
                    }
                } catch (Exception e) {
                    // Ignore
                } finally {
                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                    }
                }
            });
        } else if (NotificationHelper.ACTION_SNOOZE.equals(action)) {
            // Handle Snooze (e.g., reschedule for 10 mins later)
            // Cancel notification
            androidx.core.app.NotificationManagerCompat.from(context).cancel(reminderId);
            scheduleSnooze(context, reminderId);
        } else if (NotificationHelper.ACTION_COMPLETE.equals(action)) {
            // Mark as complete
            androidx.core.app.NotificationManagerCompat.from(context).cancel(reminderId);

            // Use background thread to update DB and refresh widget
            repository.execute(() -> {
                Reminder reminder = repository.getReminderSync(reminderId);
                if (reminder != null) {
                    reminder.setCompleted(true);
                    repository.update(reminder); // This schedules the update on the single thread executor

                    // Schedule the widget refresh to run AFTER the update
                    repository.execute(() -> {
                        com.example.reminder.widget.StickyNoteWidgetProvider.sendRefreshBroadcast(context);
                    });
                }
            });
        }
    }

    private void rescheduleIfRepeating(Context context, Reminder reminder) {
        long nextTime = 0;
        String repeatMode = reminder.getRepeatMode();

        if ("CUSTOM".equals(repeatMode)) {
            nextTime = calculateNextCustomTime(reminder);
        } else {
            // Standard modes
            long interval = 0;
            switch (repeatMode) {
                case "HOURLY":
                    interval = AlarmManager.INTERVAL_HOUR;
                    break;
                case "DAILY":
                    interval = AlarmManager.INTERVAL_DAY;
                    break;
                case "WEEKLY":
                    interval = AlarmManager.INTERVAL_DAY * 7;
                    break;
                case "MONTHLY":
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTimeInMillis(reminder.getTimeMillis());
                    cal.add(java.util.Calendar.MONTH, 1);
                    interval = cal.getTimeInMillis() - reminder.getTimeMillis();
                    break;
            }
            if (interval > 0) {
                nextTime = reminder.getTimeMillis() + interval;
            }
        }

        if (nextTime > System.currentTimeMillis()) {
            reminder.setTimeMillis(nextTime);
            // Updating the reminder in DB via repository executor
            repository.update(reminder);
            scheduleAlarm(context, reminder);
        }
    }

    private long calculateNextCustomTime(Reminder reminder) {
        long lastTime = reminder.getTimeMillis();
        long interval = reminder.getRepeatInterval();

        // If interval is 0, treat as daily (fallback)
        if (interval <= 0)
            return lastTime + AlarmManager.INTERVAL_DAY;

        // 1. Proposed next time based on interval
        long next = lastTime + interval;

        // 2. Load constraints
        String daysStr = reminder.getRepeatDays(); // "1,2,3,4,5"
        Integer winStart = reminder.getWindowStart(); // Minutes from midnight
        Integer winEnd = reminder.getWindowEnd(); // Minutes from midnight

        // If no constraints, just return next
        if (daysStr == null || daysStr.isEmpty())
            return next;

        // Parse days
        java.util.Set<Integer> validDays = new java.util.HashSet<>();
        try {
            for (String d : daysStr.split(",")) {
                validDays.add(Integer.parseInt(d));
            }
        } catch (NumberFormatException e) {
            return next; // Fail safe
        }

        // Logic to find next valid slot
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(next);

        // Safety break to prevent infinite loops (e.g. no days selected)
        int safeguards = 0;
        while (safeguards < 365) { // Check up to a year ahead
            int currentDay = cal.get(java.util.Calendar.DAY_OF_WEEK);
            int currentMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE);

            // Check Day Validity
            if (validDays.contains(currentDay)) {
                // Check Window Validity (if defined)
                boolean windowValid = true;
                if (winStart != null && winEnd != null) {
                    if (currentMinutes < winStart) {
                        // Too early: Move to window start
                        cal.set(java.util.Calendar.HOUR_OF_DAY, winStart / 60);
                        cal.set(java.util.Calendar.MINUTE, winStart % 60);
                        cal.set(java.util.Calendar.SECOND, 0);
                        return cal.getTimeInMillis();
                    } else if (currentMinutes > winEnd) {
                        // Too late: Window invalid for today
                        windowValid = false;
                    }
                    // Else: Valid
                }

                if (windowValid) {
                    return cal.getTimeInMillis();
                }
            }

            // If we are here, today/this time is invalid.
            // Move to start of NEXT day (or next validity check)
            // If we have a window, aim for window start of next day.
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
            if (winStart != null) {
                cal.set(java.util.Calendar.HOUR_OF_DAY, winStart / 60);
                cal.set(java.util.Calendar.MINUTE, winStart % 60);
                cal.set(java.util.Calendar.SECOND, 0);
            } else {
                // Keep same time if no window, just next day
                // Actually logic suggests resetting to 00:00 if day was invalid?
                // But simpler to just keep adding days until we hit a valid day, preserving
                // time
                // unless window is enforced.
            }
            safeguards++;
        }

        return next; // Fallback
    }

    private void scheduleSnooze(Context context, int reminderId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Get custom snooze duration
        android.content.SharedPreferences prefs = androidx.preference.PreferenceManager
                .getDefaultSharedPreferences(context);
        int snoozeMinutes = prefs.getInt("snooze_duration", 10);
        long triggerTime = System.currentTimeMillis() + snoozeMinutes * 60 * 1000L;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    public static void scheduleAlarm(Context context, Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminder.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminder.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (reminder.getTimeMillis() > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.getTimeMillis(),
                            pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.getTimeMillis(),
                        pendingIntent);
            }
        }
    }

    public static void cancelAlarm(Context context, Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminder.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }
}
