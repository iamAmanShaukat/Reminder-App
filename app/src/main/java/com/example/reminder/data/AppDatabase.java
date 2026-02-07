package com.example.reminder.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = { Reminder.class }, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ReminderDao reminderDao();

    public static final androidx.room.migration.Migration MIGRATION_2_3 = new androidx.room.migration.Migration(2, 3) {
        @Override
        public void migrate(androidx.sqlite.db.SupportSQLiteDatabase database) {
            // Add the new 'hideFromWidget' column with default value 0 (false)
            database.execSQL("ALTER TABLE reminders ADD COLUMN hideFromWidget INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final androidx.room.migration.Migration MIGRATION_3_4 = new androidx.room.migration.Migration(3, 4) {
        @Override
        public void migrate(androidx.sqlite.db.SupportSQLiteDatabase database) {
            // Add new columns for advanced repeat options
            database.execSQL("ALTER TABLE reminders ADD COLUMN repeatDays TEXT");
            database.execSQL("ALTER TABLE reminders ADD COLUMN windowStart INTEGER");
            database.execSQL("ALTER TABLE reminders ADD COLUMN windowEnd INTEGER");
        }
    };
}
