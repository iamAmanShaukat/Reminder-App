package com.example.reminder;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class ReminderApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setupDailyResetWorker();
    }

    private void setupDailyResetWorker() {
        androidx.work.PeriodicWorkRequest resetRequest = new androidx.work.PeriodicWorkRequest.Builder(
                com.example.reminder.worker.DailyResetWorker.class, 24, java.util.concurrent.TimeUnit.HOURS)
                .setInitialDelay(calculateInitialDelayForMidnight(), java.util.concurrent.TimeUnit.MILLISECONDS)
                .build();

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DailyResetWork",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
                resetRequest);
    }

    private long calculateInitialDelayForMidnight() {
        java.util.Calendar currentDate = java.util.Calendar.getInstance();
        java.util.Calendar dueDate = java.util.Calendar.getInstance();

        // Set to tomorrow at 00:00:00
        dueDate.add(java.util.Calendar.HOUR_OF_DAY, 24);
        dueDate.set(java.util.Calendar.HOUR_OF_DAY, 0);
        dueDate.set(java.util.Calendar.MINUTE, 0);
        dueDate.set(java.util.Calendar.SECOND, 0);
        dueDate.set(java.util.Calendar.MILLISECOND, 0);

        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
        return timeDiff > 0 ? timeDiff : 0;
    }
}
