package com.example.reminder.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.reminder.widget.StickyNoteWidgetProvider;
import java.util.Calendar;

public class MidnightWidgetUpdateReceiver extends BroadcastReceiver {

    private static final String ACTION_MIDNIGHT_UPDATE = "com.example.reminder.MIDNIGHT_WIDGET_UPDATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        android.util.Log.d("MidnightUpdate", "onReceive: " + intent.getAction());

        if (ACTION_MIDNIGHT_UPDATE.equals(intent.getAction())
                || Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {
            // Refresh widget to update date/time display
            android.util.Log.d("MidnightUpdate", "Triggering widget refresh");
            StickyNoteWidgetProvider.sendRefreshBroadcast(context);

            // Reschedule for next midnight
            scheduleMidnightUpdate(context);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Reschedule after device reboot
            android.util.Log.d("MidnightUpdate", "Boot completed, scheduling midnight update");
            scheduleMidnightUpdate(context);
        }
    }

    public static void scheduleMidnightUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            android.util.Log.e("MidnightUpdate", "AlarmManager is null");
            return;
        }

        Intent intent = new Intent(context, MidnightWidgetUpdateReceiver.class);
        intent.setAction(ACTION_MIDNIGHT_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                9999, // Unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Calculate next midnight
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        midnight.add(Calendar.DAY_OF_YEAR, 1); // Next midnight

        long triggerTime = midnight.getTimeInMillis();

        android.util.Log.d("MidnightUpdate", "Scheduling widget update at: " + midnight.getTime());

        // Use setExactAndAllowWhileIdle for precise midnight updates
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    public static void cancelMidnightUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)
            return;

        Intent intent = new Intent(context, MidnightWidgetUpdateReceiver.class);
        intent.setAction(ACTION_MIDNIGHT_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                9999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);
        android.util.Log.d("MidnightUpdate", "Cancelled midnight update alarm");
    }
}
