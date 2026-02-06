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
            // We need to fetch the reminder from DB to get latest details
            // Since onReceive is main thread and DB is async, we use a workaround or
            // executor
            // For simplicity in this structure, using repository executor indirectly
            // Note: In production, consider using GoAsync or WorkManager for reliably
            // ensuring DB completes
            // Acquire WakeLock to ensure work completes
            android.os.PowerManager pm = (android.os.PowerManager) context.getSystemService(Context.POWER_SERVICE);
            android.os.PowerManager.WakeLock wakeLock = pm.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK,
                    "ReminderApp:AlarmReceiver");
            wakeLock.acquire(10 * 60 * 1000L /* 10 minutes */);

            repository.execute(() -> {
                try {
                    Reminder reminder = repository.getReminderSync(reminderId);
                    if (reminder != null && !reminder.isCompleted()) {
                        Log.d("AlarmReceiver", "Showing notification for: " + reminder.getTitle());
                        NotificationHelper.createNotificationChannel(context);
                        NotificationHelper.showNotification(context, reminder);

                        // Reschedule if repeating
                        rescheduleIfRepeating(context, reminder);
                    } else {
                        Log.w("AlarmReceiver", "Reminder not found or completed for ID: " + reminderId);
                    }
                } catch (Exception e) {
                    Log.e("AlarmReceiver", "Error handling alarm receive", e);
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
        switch (reminder.getRepeatMode()) {
            case "HOURLY":
                nextTime = reminder.getTimeMillis() + AlarmManager.INTERVAL_HOUR;
                break;
            case "DAILY":
                nextTime = reminder.getTimeMillis() + AlarmManager.INTERVAL_DAY;
                break;
            case "WEEKLY":
                nextTime = reminder.getTimeMillis() + AlarmManager.INTERVAL_DAY * 7;
                break;
            case "MONTHLY":
                // Approximate monthly (30 days) for simplicity, or use Calendar for accuracy
                // Using Calendar is better
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTimeInMillis(reminder.getTimeMillis());
                cal.add(java.util.Calendar.MONTH, 1);
                nextTime = cal.getTimeInMillis();
                break;
            case "CUSTOM":
                if (reminder.getRepeatInterval() > 0) {
                    nextTime = reminder.getTimeMillis() + reminder.getRepeatInterval();
                }
                break;
            case "NONE":
            default:
                return;
        }

        if (nextTime > System.currentTimeMillis()) {
            reminder.setTimeMillis(nextTime);
            repository.update(reminder);
            scheduleAlarm(context, reminder);
            Log.d("AlarmReceiver", "Rescheduled recurring reminder: " + reminder.getTitle() + " to " + nextTime);
        }
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

        Log.d("AlarmReceiver", "Snoozed reminder " + reminderId + " for " + snoozeMinutes + " minutes");
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
