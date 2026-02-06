package com.example.reminder.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertReminder(Reminder reminder);

    @Update
    void updateReminder(Reminder reminder);

    @Delete
    void deleteReminder(Reminder reminder);

    @Delete
    void deleteReminders(List<Reminder> reminders);

    @Query("SELECT * FROM reminders ORDER BY timeMillis ASC")
    LiveData<List<Reminder>> getAllReminders();

    @Query("SELECT * FROM reminders ORDER BY timeMillis ASC")
    List<Reminder> getAllRemindersSync();

    @Query("SELECT * FROM reminders WHERE id = :id")
    LiveData<Reminder> getReminderById(int id);

    @Query("SELECT * FROM reminders WHERE id = :id")
    Reminder getReminderByIdSync(int id);

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY timeMillis ASC")
    LiveData<List<Reminder>> getActiveReminders();

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY timeMillis ASC")
    List<Reminder> getActiveRemindersSync();
}
