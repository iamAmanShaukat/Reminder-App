package com.example.reminder.data;

import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ReminderRepository {

    private final ReminderDao reminderDao;
    private final ExecutorService executorService;

    @Inject
    public ReminderRepository(ReminderDao reminderDao) {
        this.reminderDao = reminderDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Reminder>> getAllReminders() {
        return reminderDao.getAllReminders();
    }

    public LiveData<Reminder> getReminder(int id) {
        return reminderDao.getReminderById(id);
    }

    public Reminder getReminderSync(int id) {
        return reminderDao.getReminderByIdSync(id);
    }

    public void insert(Reminder reminder) {
        executorService.execute(() -> {
            try {
                reminderDao.insertReminder(reminder);
                android.util.Log.d("ReminderRepository", "Inserted reminder: " + reminder.getTitle());
            } catch (Exception e) {
                android.util.Log.e("ReminderRepository", "Error inserting reminder", e);
            }
        });
    }

    public void insert(Reminder reminder, OnReminderInsertedListener listener) {
        executorService.execute(() -> {
            try {
                long id = reminderDao.insertReminder(reminder);
                if (listener != null) {
                    listener.onInserted(id);
                }
                android.util.Log.d("ReminderRepository", "Inserted reminder with ID: " + id);
            } catch (Exception e) {
                android.util.Log.e("ReminderRepository", "Error inserting reminder with listener", e);
            }
        });
    }

    public void update(Reminder reminder) {
        executorService.execute(() -> {
            try {
                reminderDao.updateReminder(reminder);
                android.util.Log.d("ReminderRepository", "Updated reminder: " + reminder.getId());
            } catch (Exception e) {
                android.util.Log.e("ReminderRepository", "Error updating reminder", e);
            }
        });
    }

    public void delete(Reminder reminder) {
        executorService.execute(() -> {
            try {
                reminderDao.deleteReminder(reminder);
                android.util.Log.d("ReminderRepository", "Deleted reminder: " + reminder.getId());
            } catch (Exception e) {
                android.util.Log.e("ReminderRepository", "Error deleting reminder", e);
            }
        });
    }

    public void delete(List<Reminder> reminders) {
        executorService.execute(() -> {
            try {
                reminderDao.deleteReminders(reminders);
                android.util.Log.d("ReminderRepository", "Deleted " + reminders.size() + " reminders");
            } catch (Exception e) {
                android.util.Log.e("ReminderRepository", "Error deleting reminders batch", e);
            }
        });
    }

    // Expose executor for Receiver use
    public void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    public interface OnReminderInsertedListener {
        void onInserted(long id);
    }
}
