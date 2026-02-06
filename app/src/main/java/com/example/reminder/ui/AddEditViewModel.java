package com.example.reminder.ui;

import androidx.lifecycle.ViewModel;
import com.example.reminder.data.Reminder;
import com.example.reminder.data.ReminderRepository;
import com.example.reminder.receiver.AlarmReceiver;
import android.content.Context;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;

@HiltViewModel
public class AddEditViewModel extends ViewModel {

    private final ReminderRepository repository;
    private final Context context;

    @Inject
    public AddEditViewModel(ReminderRepository repository, @ApplicationContext Context context) {
        this.repository = repository;
        this.context = context;
    }

    public void saveReminder(Reminder reminder) {
        android.util.Log.d("AddEditViewModel", "Saving reminder: " + reminder.getTitle());
        if (reminder.getId() == 0) {
            repository.insert(reminder, id -> {
                reminder.setId((int) id);
                AlarmReceiver.scheduleAlarm(context, reminder);
                com.example.reminder.widget.StickyNoteWidgetProvider.sendRefreshBroadcast(context);
            });
        } else {
            repository.update(reminder);
            AlarmReceiver.scheduleAlarm(context, reminder);
            com.example.reminder.widget.StickyNoteWidgetProvider.sendRefreshBroadcast(context);
        }
    }

    public void deleteReminder(Reminder reminder) {
        android.util.Log.d("AddEditViewModel", "Deleting reminder: " + reminder.getId());
        AlarmReceiver.cancelAlarm(context, reminder);
        repository.delete(reminder);
        com.example.reminder.widget.StickyNoteWidgetProvider.sendRefreshBroadcast(context);
    }
}
