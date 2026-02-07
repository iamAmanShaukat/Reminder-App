package com.example.reminder.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = { Reminder.class }, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ReminderDao reminderDao();

    public static final androidx.room.migration.Migration MIGRATION_2_3 = new androidx.room.migration.Migration(2, 3) {
        @Override
        public void migrate(androidx.sqlite.db.SupportSQLiteDatabase database) {
            // Add the new 'hideFromWidget' column with default value 0 (false)
            database.execSQL("ALTER TABLE reminders ADD COLUMN hideFromWidget INTEGER NOT NULL DEFAULT 0");
        }
    };
}
