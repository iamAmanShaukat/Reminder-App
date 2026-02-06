package com.example.reminder.di;

import android.content.Context;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.reminder.data.AppDatabase;
import com.example.reminder.data.ReminderDao;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "reminder_database")
                .fallbackToDestructiveMigration()
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // Enable WAL for concurrency
                .build();
    }

    @Provides
    public ReminderDao provideReminderDao(AppDatabase database) {
        return database.reminderDao();
    }
}
