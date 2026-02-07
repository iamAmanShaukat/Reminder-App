package com.example.reminder.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.example.reminder.R;
import com.example.reminder.data.AppDatabase;
import com.example.reminder.data.Reminder;
import com.example.reminder.di.AppModule;
import dagger.hilt.android.AndroidEntryPoint;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import androidx.room.Room;
import androidx.room.RoomDatabase;

public class WidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ReminderRemoteViewsFactory(this.getApplicationContext());
    }
}

class ReminderRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private List<Reminder> reminders = new ArrayList<>();
    private AppDatabase database;

    public ReminderRemoteViewsFactory(Context context) {
        this.context = context;
        // Manually build DB since Service injection is tricky in
        // RemoteViewsServiceFactory
        // Enable WAL to prevent locking with Main App
        database = Room.databaseBuilder(context, AppDatabase.class, "reminder_database")
                .addMigrations(AppDatabase.MIGRATION_2_3)
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .build();
    }

    @Override
    public void onCreate() {
        android.util.Log.d("WidgetService", "onCreate");
    }

    @Override
    public void onDataSetChanged() {
        android.util.Log.d("WidgetService", "onDataSetChanged START");
        long identityToken = android.os.Binder.clearCallingIdentity();
        try {
            if (database == null) {
                android.util.Log.e("WidgetService", "Database is null in onDataSetChanged! Recreating...");
                database = Room.databaseBuilder(context, AppDatabase.class, "reminder_database")
                        .addMigrations(AppDatabase.MIGRATION_2_3)
                        .fallbackToDestructiveMigration() // Handle version update
                        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                        .build();
            }

            reminders = database.reminderDao().getWidgetRemindersSync();
            android.util.Log.d("WidgetService",
                    "onDataSetChanged: Data fetched. Count: " + (reminders != null ? reminders.size() : "null"));

            if (reminders != null) {
                for (Reminder r : reminders) {
                    android.util.Log.v("WidgetService", " - Reminder: " + r.getTitle() + " (ID: " + r.getId() + ")");
                }
            }
        } catch (Exception e) {
            android.util.Log.e("WidgetService", "CRITICAL ERROR in onDataSetChanged", e);
        } finally {
            android.os.Binder.restoreCallingIdentity(identityToken);
        }
        android.util.Log.d("WidgetService", "onDataSetChanged END");
    }

    @Override
    public void onDestroy() {
        android.util.Log.d("WidgetService", "onDestroy");
        if (database != null)
            database.close();
    }

    @Override
    public int getCount() {
        int count = reminders == null ? 0 : reminders.size();
        android.util.Log.v("WidgetService", "getCount returning: " + count);
        return count;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        android.util.Log.v("WidgetService", "getViewAt: " + position);
        if (position == -1 || reminders == null || position >= reminders.size()) {
            android.util.Log.w("WidgetService", "getViewAt: Invalid position/data. pos=" + position + ", size="
                    + (reminders == null ? "null" : reminders.size()));
            return null;
        }

        try {
            Reminder reminder = reminders.get(position);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_item);

            // Header Logic Removed per user request
            // (Reverted to simple list)

            rv.setTextViewText(R.id.widget_item_title, reminder.getTitle());

            // Date Logic: Today = time only, Other days = date only
            java.util.Calendar now = java.util.Calendar.getInstance();
            java.util.Calendar target = java.util.Calendar.getInstance();
            target.setTimeInMillis(reminder.getTimeMillis());

            boolean isToday = now.get(java.util.Calendar.YEAR) == target.get(java.util.Calendar.YEAR) &&
                    now.get(java.util.Calendar.DAY_OF_YEAR) == target.get(java.util.Calendar.DAY_OF_YEAR);

            String timeText;
            if (isToday) {
                // Today: Show only time
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                timeText = sdf.format(new Date(reminder.getTimeMillis()));
            } else {
                // Other days: Show only date
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
                timeText = sdf.format(new Date(reminder.getTimeMillis()));
            }

            rv.setTextViewText(R.id.widget_item_time, timeText);

            // Overdue Logic: Check if task is from a previous day
            java.util.Calendar today = java.util.Calendar.getInstance();
            // Reset to beginning of day
            today.set(java.util.Calendar.HOUR_OF_DAY, 0);
            today.set(java.util.Calendar.MINUTE, 0);
            today.set(java.util.Calendar.SECOND, 0);
            today.set(java.util.Calendar.MILLISECOND, 0);

            if (reminder.getTimeMillis() < today.getTimeInMillis() && !reminder.isCompleted()) {
                // Overdue - red time text only
                rv.setInt(R.id.widget_item_container, "setBackgroundResource", R.drawable.bg_card_glass);
                rv.setTextColor(R.id.widget_item_time, context.getColor(R.color.color_delete)); // Red for overdue
            } else {
                // Normal
                rv.setInt(R.id.widget_item_container, "setBackgroundResource", R.drawable.bg_card_glass);
                rv.setTextColor(R.id.widget_item_time, context.getColor(R.color.brand_accent));
            }

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra("reminder_id", reminder.getId());
            rv.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent);

            return rv;
        } catch (Exception e) {
            String errorMsg = "CRITICAL ERROR in getViewAt position " + position + ": " + e.getMessage();
            android.util.Log.e("WidgetService", errorMsg, e);
            // Optionally create an error view here if possible, but returning null shows
            // loading
            return null;
        } finally {
            android.util.Log.v("WidgetService", "getViewAt: FINISHED position " + position);
        }
    }

    private String getDateTitle(long timeMillis) {
        java.util.Calendar target = java.util.Calendar.getInstance();
        target.setTimeInMillis(timeMillis);

        java.util.Calendar now = java.util.Calendar.getInstance();

        if (target.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
                target.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR)) {
            return "Today";
        }

        now.add(java.util.Calendar.DAY_OF_YEAR, 1);
        if (target.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
                target.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR)) {
            return "Tomorrow";
        }

        now.add(java.util.Calendar.DAY_OF_YEAR, -2); // Go back to yesterday
        if (target.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
                target.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR)) {
            return "Yesterday";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return reminders.get(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
