package com.example.reminder.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.reminder.R;
import com.example.reminder.data.Reminder;
import com.example.reminder.receiver.AlarmReceiver;
import com.example.reminder.ui.MainActivity;
import androidx.preference.PreferenceManager;

public class NotificationHelper {

        public static final String CHANNEL_ID = "reminder_channel_v2"; // Changed ID to force update
        public static final String ACTION_SNOOZE = "com.example.reminder.ACTION_SNOOZE";
        public static final String ACTION_COMPLETE = "com.example.reminder.ACTION_COMPLETE";
        public static final String EXTRA_REMINDER_ID = "reminder_id";

        public static void createNotificationChannel(Context context) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        CharSequence name = "Reminders";
                        String description = "Channel for Reminder Notifications";
                        int importance = NotificationManager.IMPORTANCE_HIGH;
                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                        channel.setDescription(description);
                        channel.enableVibration(true);
                        channel.setVibrationPattern(new long[] { 0, 250, 250, 250 }); // Explicit pattern
                        channel.enableLights(true);

                        // Explicit sound attributes
                        android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                                        .build();

                        // Get custom sound from prefs
                        android.content.SharedPreferences prefs = androidx.preference.PreferenceManager
                                        .getDefaultSharedPreferences(context);
                        String soundUriString = prefs.getString("ringtone_uri", null);
                        android.net.Uri soundUri = soundUriString != null ? android.net.Uri.parse(soundUriString)
                                        : android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;

                        channel.setSound(soundUri, audioAttributes);

                        channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(channel);
                }
        }

        public static void showNotification(Context context, Reminder reminder) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ActivityCompat.checkSelfPermission(context,
                                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                android.util.Log.w("NotificationHelper", "Permission POST_NOTIFICATIONS not granted!");
                                return;
                        }
                }

                Intent openIntent = new Intent(context, MainActivity.class);
                openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingOpenIntent = PendingIntent.getActivity(context, reminder.getId(), openIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                // Snooze Action
                Intent snoozeIntent = new Intent(context, AlarmReceiver.class);
                snoozeIntent.setAction(ACTION_SNOOZE);
                snoozeIntent.putExtra(EXTRA_REMINDER_ID, reminder.getId());
                PendingIntent pendingSnoozeIntent = PendingIntent.getBroadcast(context, reminder.getId() + 1000,
                                snoozeIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                // Complete Action
                Intent completeIntent = new Intent(context, AlarmReceiver.class);
                completeIntent.setAction(ACTION_COMPLETE);
                completeIntent.putExtra(EXTRA_REMINDER_ID, reminder.getId());
                PendingIntent pendingCompleteIntent = PendingIntent.getBroadcast(context, reminder.getId() + 2000,
                                completeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                // Use a drawable resource for icon
                // Get snooze duration for label
                android.content.SharedPreferences prefs = androidx.preference.PreferenceManager
                                .getDefaultSharedPreferences(context);
                int snoozeMinutes = prefs.getInt("snooze_duration", 10);
                String snoozeLabel = "Snooze " + snoozeMinutes + "m";

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(android.R.drawable.ic_popup_reminder) // Standard icon as fallback
                                .setContentTitle(reminder.getTitle())
                                .setContentText(reminder.getDescription())
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setContentIntent(pendingOpenIntent)
                                .setAutoCancel(!reminder.isAllDay()) // Sticky if all-day
                                .setOngoing(reminder.isAllDay()) // Non-removable if all-day
                                .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound + Vibration
                                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .addAction(android.R.drawable.ic_popup_reminder, snoozeLabel, pendingSnoozeIntent)
                                .addAction(android.R.drawable.checkbox_on_background, "Complete",
                                                pendingCompleteIntent);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(reminder.getId(), builder.build());
        }
}
