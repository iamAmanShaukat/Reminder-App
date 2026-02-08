package com.example.reminder.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.preference.PreferenceManager;
import com.example.reminder.data.Reminder;
import com.example.reminder.receiver.AlarmReceiver;
import com.example.reminder.utils.NotificationHelper;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Centralized service for scheduling, canceling, and snoozing alarms.
 * Extracted from AlarmReceiver to follow Single Responsibility Principle.
 */
@Singleton
public class AlarmScheduler {

    private final Context context;
    private final AlarmManager alarmManager;

    @Inject
    public AlarmScheduler(@ApplicationContext Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Schedule an alarm for the given reminder.
     */
    public void schedule(Reminder reminder) {
        if (reminder.getTimeMillis() <= System.currentTimeMillis()) {
            return; // Don't schedule past alarms
        }

        PendingIntent pendingIntent = createPendingIntent(reminder.getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.getTimeMillis(),
                        pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.getTimeMillis(),
                    pendingIntent);
        }
    }

    /**
     * Cancel the alarm for the given reminder.
     */
    public void cancel(Reminder reminder) {
        PendingIntent pendingIntent = createPendingIntent(reminder.getId());
        alarmManager.cancel(pendingIntent);
    }

    /**
     * Schedule a snooze alarm for the given reminder ID.
     * Uses the user's configured snooze duration from preferences.
     */
    public void scheduleSnooze(int reminderId) {
        int snoozeMinutes = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt("snooze_duration", 10);

        long triggerTime = System.currentTimeMillis() + snoozeMinutes * 60 * 1000L;
        PendingIntent pendingIntent = createPendingIntent(reminderId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private PendingIntent createPendingIntent(int reminderId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId);
        return PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
