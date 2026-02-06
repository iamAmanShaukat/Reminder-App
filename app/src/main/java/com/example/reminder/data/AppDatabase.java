package com.example.reminder.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = { Reminder.class }, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ReminderDao reminderDao();
}
