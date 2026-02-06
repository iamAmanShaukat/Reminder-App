package com.example.reminder;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class ReminderApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
