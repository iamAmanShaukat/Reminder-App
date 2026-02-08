package com.example.reminder.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.reminder.data.Reminder;
import com.example.reminder.data.ReminderRepository;
import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DailyResetWorker extends Worker {

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface ReminderRepositoryEntryPoint {
        ReminderRepository getReminderRepository();
    }

    public DailyResetWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        ReminderRepositoryEntryPoint hiltEntryPoint = EntryPointAccessors.fromApplication(context,
                ReminderRepositoryEntryPoint.class);
        ReminderRepository repository = hiltEntryPoint.getReminderRepository();

        try {
            // 1. Get all reminders synchronously
            List<Reminder> reminders = repository.getAllRemindersSync();

            // 2. Filter and update
            for (Reminder r : reminders) {
                // Check if it's a repeating task (Daily, Weekly, Monthly, Custom) AND completed
                String mode = r.getRepeatMode();
                boolean isRepeating = (mode != null && !"NONE".equals(mode)) || r.isHideFromWidget();

                if (isRepeating && r.isCompleted()) {
                    r.setCompleted(false);
                    // Update in DB (Repo.update is async by default, but we are already in a
                    // background thread)
                    repository.update(r);
                }
            }

            // 3. Request Widget Refresh
            com.example.reminder.widget.StickyNoteWidgetProvider.sendRefreshBroadcast(context);

            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }
}
