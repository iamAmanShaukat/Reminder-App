package com.example.reminder.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.reminder.data.Reminder;
import com.example.reminder.data.ReminderRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final ReminderRepository repository;
    private final LiveData<List<Reminder>> allReminders;

    @Inject
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context context;

    @Inject
    public HomeViewModel(ReminderRepository repository) {
        this.repository = repository;
        this.allReminders = repository.getAllReminders();
    }

    public LiveData<List<Reminder>> getAllReminders() {
        return allReminders;
    }

    public void delete(Reminder reminder) {
        android.util.Log.d("HomeViewModel", "Deleting reminder: " + reminder.getId());
        repository.delete(reminder);
        com.example.reminder.widget.StickyNoteWidgetProvider.sendRefreshBroadcast(context);
    }

    public void delete(List<Reminder> reminders) {
        android.util.Log.d("HomeViewModel", "Deleting batch: " + reminders.size());
        repository.delete(reminders);
        com.example.reminder.widget.StickyNoteWidgetProvider.sendRefreshBroadcast(context);
    }

    public void markComplete(Reminder reminder) {
        android.util.Log.d("HomeViewModel", "Marking complete: " + reminder.getId());
        reminder.setCompleted(true);
        repository.update(reminder);
        com.example.reminder.widget.StickyNoteWidgetProvider.sendRefreshBroadcast(context);
    }
}
