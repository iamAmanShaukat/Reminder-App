package com.example.reminder.ui.calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.reminder.data.Reminder;
import com.example.reminder.data.ReminderRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CalendarViewModel extends ViewModel {

    private final ReminderRepository repository;
    private final MutableLiveData<YearMonth> currentMonth = new MutableLiveData<>();
    private final MutableLiveData<List<LocalDate>> calendarDays = new MutableLiveData<>();
    private final MutableLiveData<Map<LocalDate, List<Reminder>>> remindersForMonth = new MutableLiveData<>();

    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>();
    private final MutableLiveData<List<Reminder>> selectedDateReminders = new MutableLiveData<>();

    @Inject
    public CalendarViewModel(ReminderRepository repository) {
        this.repository = repository;
        currentMonth.setValue(YearMonth.now());
        selectedDate.setValue(LocalDate.now()); // Default select today
        updateCalendar(YearMonth.now());
    }

    public LiveData<YearMonth> getCurrentMonth() {
        return currentMonth;
    }

    public LiveData<List<LocalDate>> getCalendarDays() {
        return calendarDays;
    }

    public LiveData<Map<LocalDate, List<Reminder>>> getRemindersForMonth() {
        return remindersForMonth;
    }

    public LiveData<LocalDate> getSelectedDate() {
        return selectedDate;
    }

    public LiveData<List<Reminder>> getSelectedDateReminders() {
        return selectedDateReminders;
    }

    public void selectDate(LocalDate date) {
        selectedDate.setValue(date);
        updateSelectedReminders(date);
    }

    private void updateSelectedReminders(LocalDate date) {
        Map<LocalDate, List<Reminder>> map = remindersForMonth.getValue();
        if (map != null && map.containsKey(date)) {
            // Sort by time
            List<Reminder> list = new ArrayList<>(map.get(date));
            list.sort((r1, r2) -> Long.compare(r1.getTimeMillis(), r2.getTimeMillis()));
            selectedDateReminders.setValue(list);
        } else {
            selectedDateReminders.setValue(new ArrayList<>());
        }
    }

    public void nextMonth() {
        YearMonth next = currentMonth.getValue().plusMonths(1);
        currentMonth.setValue(next);
        updateCalendar(next);
    }

    public void previousMonth() {
        YearMonth prev = currentMonth.getValue().minusMonths(1);
        currentMonth.setValue(prev);
        updateCalendar(prev);
    }

    private void updateCalendar(YearMonth month) {
        // Generate Days
        List<LocalDate> days = new ArrayList<>();
        LocalDate firstOfMonth = month.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1=Mon, 7=Sun

        // Adjust for Sunday start if needed (assuming Sun=0/7 logic for grid)
        // If grid starts Mon: int startOffset = dayOfWeek - 1;
        // If grid starts Sun:
        int startOffset = dayOfWeek % 7;

        // Previous month padding
        for (int i = 0; i < startOffset; i++) {
            // Optional: Add null or actual dates
            days.add(firstOfMonth.minusDays(startOffset - i));
        }

        // Current month
        for (int i = 1; i <= month.lengthOfMonth(); i++) {
            days.add(month.atDay(i));
        }

        // Next month padding to fill grid (42 cells total for 6 rows, or dynamic)
        int remaining = 42 - days.size(); // Standard 6-row calendar
        for (int i = 1; i <= remaining; i++) {
            days.add(month.atEndOfMonth().plusDays(i));
        }

        calendarDays.setValue(days);

        // Fetch Reminders
        long start = toMillis(days.get(0));
        long end = toMillis(days.get(days.size() - 1)) + 86400000; // End of last day

        // Note: getAllReminders is simplest, but for optimization we should filter by
        // range.
        // For now, fetching all and filtering in memory is acceptable for small
        // datasets.
        // Ideally: repository.getRemindersBetween(start, end).

    }

    public void setReminders(List<Reminder> allReminders) {
        Map<LocalDate, List<Reminder>> map = new HashMap<>();
        List<LocalDate> currentDays = calendarDays.getValue();
        if (currentDays == null)
            return;

        for (Reminder r : allReminders) {
            LocalDate date = java.time.Instant.ofEpochMilli(r.getTimeMillis())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if (map.containsKey(date)) {
                map.get(date).add(r);
            } else {
                List<Reminder> list = new ArrayList<>();
                list.add(r);
                map.put(date, list);
            }
        }

        // Filter only for visible days? Or map all.
        // Mapping all is safer.
        // Be careful: map.putIfAbsent...

        // Actually, logic above is flawed: we want to map ALL reminders to dates.
        // Let's rebuild the map.

        Map<LocalDate, List<Reminder>> newMap = new HashMap<>();
        for (Reminder r : allReminders) {
            LocalDate date = java.time.Instant.ofEpochMilli(r.getTimeMillis())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            newMap.computeIfAbsent(date, k -> new ArrayList<>()).add(r);
        }
        remindersForMonth.setValue(newMap);

        // Refresh selected date
        if (selectedDate.getValue() != null) {
            updateSelectedReminders(selectedDate.getValue());
        }
    }

    private long toMillis(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
