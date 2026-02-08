package com.example.reminder.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.reminder.data.Reminder;
import com.example.reminder.data.ReminderRepository;
import com.example.reminder.domain.usecase.CompleteReminderUseCase;
import com.example.reminder.service.WidgetRefreshService;
import com.example.reminder.utils.DateFormatter;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final ReminderRepository repository;
    private final CompleteReminderUseCase completeReminderUseCase;
    private final WidgetRefreshService widgetRefreshService;
    private final DateFormatter dateFormatter;
    private final LiveData<List<Reminder>> allReminders;
    private final LiveData<List<ListItem>> reminderListItems;

    @Inject
    public HomeViewModel(
            ReminderRepository repository,
            CompleteReminderUseCase completeReminderUseCase,
            WidgetRefreshService widgetRefreshService,
            DateFormatter dateFormatter) {
        this.repository = repository;
        this.completeReminderUseCase = completeReminderUseCase;
        this.widgetRefreshService = widgetRefreshService;
        this.dateFormatter = dateFormatter;

        // 1. Raw Reminders (for Calendar and internal logic)
        this.allReminders = androidx.lifecycle.Transformations.map(repository.getAllReminders(), reminders -> {
            List<Reminder> standard = new java.util.ArrayList<>();
            List<Reminder> daily = new java.util.ArrayList<>();

            for (Reminder r : reminders) {
                if (r.isHideFromWidget()) {
                    daily.add(r);
                } else {
                    standard.add(r);
                }
            }
            standard.addAll(daily);
            return standard;
        });

        // 2. ListItems (for Home Fragment UI with Headers)
        this.reminderListItems = androidx.lifecycle.Transformations.map(this.allReminders, reminders -> {
            List<ListItem> items = new java.util.ArrayList<>();
            List<Reminder> standard = new java.util.ArrayList<>();
            List<Reminder> daily = new java.util.ArrayList<>();

            for (Reminder r : reminders) {
                if (r.isHideFromWidget()) {
                    daily.add(r);
                } else {
                    standard.add(r);
                }
            }

            // Standard Group Logic
            if (!standard.isEmpty()) {
                String lastHeader = "";
                for (Reminder r : standard) {
                    String currentHeader = dateFormatter.getDateTitle(r.getTimeMillis());
                    if (!currentHeader.equals(lastHeader)) {
                        items.add(new HeaderItem(currentHeader));
                        lastHeader = currentHeader;
                    }
                    items.add(new ReminderItem(r));
                }
            }

            // Everyday Group Logic
            if (!daily.isEmpty()) {
                items.add(new HeaderItem("Everyday"));
                for (Reminder r : daily) {
                    items.add(new ReminderItem(r));
                }
            }

            return items;
        });
    }

    public LiveData<List<Reminder>> getAllReminders() {
        return allReminders;
    }

    public LiveData<List<ListItem>> getReminderListItems() {
        return reminderListItems;
    }

    public void insert(Reminder reminder) {
        repository.insert(reminder);
        widgetRefreshService.refresh();
    }

    public void delete(Reminder reminder) {
        repository.delete(reminder);
        widgetRefreshService.refresh();
    }

    public void delete(List<Reminder> reminders) {
        repository.delete(reminders);
        widgetRefreshService.refresh();
    }

    public void updateCompletionStatus(Reminder reminder, boolean isCompleted) {
        completeReminderUseCase.execute(reminder, isCompleted);
    }

    // Legacy method for backward compatibility
    public void markComplete(Reminder reminder) {
        updateCompletionStatus(reminder, true);
    }
}
