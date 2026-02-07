package com.example.reminder.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.reminder.data.Reminder;
import com.example.reminder.databinding.FragmentAddEditBinding;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

@AndroidEntryPoint
public class AddEditFragment extends Fragment {

    private FragmentAddEditBinding binding;
    private AddEditViewModel viewModel;
    private Calendar calendar;
    private Reminder existingReminder;
    private String selectedRepeatMode = "NONE";
    private long customIntervalMillis = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAddEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddEditViewModel.class);
        calendar = Calendar.getInstance();

        if (getArguments() != null && getArguments().containsKey("reminder")) {
            existingReminder = (Reminder) getArguments().getSerializable("reminder");
            populateFields(existingReminder);
        }

        updateDateButton();
        updateTimeButton();
        updateRepeatText();

        binding.btnDateContainer.setOnClickListener(v -> showDatePicker());
        binding.btnTimeContainer.setOnClickListener(v -> showTimePicker());
        binding.tvRepeat.setOnClickListener(v -> showRepeatOptions());
        binding.containerRepeat.setOnClickListener(v -> showRepeatOptions());

        binding.btnSave.setOnClickListener(v -> saveReminder());
    }

    // Advanced Repeat State
    private String repeatDays = null; // "1,2,3,4,5,6,7"
    private int windowStartC = 480; // 8:00 AM
    private int windowEndC = 1200; // 8:00 PM
    private boolean isIntervalMode = false;

    private void showRepeatOptions() {
        android.view.View dialogView = LayoutInflater.from(requireContext())
                .inflate(com.example.reminder.R.layout.dialog_repeat_advanced, null);

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Initialize UI Elements
        com.google.android.material.chip.ChipGroup chipGroup = dialogView
                .findViewById(com.example.reminder.R.id.chipGroupDays);
        android.widget.RadioGroup radioGroup = dialogView.findViewById(com.example.reminder.R.id.radioGroupFrequency);
        android.widget.LinearLayout layoutInterval = dialogView
                .findViewById(com.example.reminder.R.id.layoutIntervalOptions);
        android.widget.EditText etInterval = dialogView.findViewById(com.example.reminder.R.id.etIntervalValue);
        com.google.android.material.button.MaterialButtonToggleGroup toggleGroupUnit = dialogView
                .findViewById(com.example.reminder.R.id.toggleGroupUnit);
        android.widget.TextView tvStart = dialogView.findViewById(com.example.reminder.R.id.tvStartTime);
        android.widget.TextView tvEnd = dialogView.findViewById(com.example.reminder.R.id.tvEndTime);
        android.widget.Button btnCancel = dialogView.findViewById(com.example.reminder.R.id.btnCancel);
        android.widget.Button btnConfirm = dialogView.findViewById(com.example.reminder.R.id.btnConfirm);

        // Load Current State
        // Days
        if (repeatDays == null || repeatDays.isEmpty()) {
            // Default select all
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                ((com.google.android.material.chip.Chip) chipGroup.getChildAt(i)).setChecked(true);
            }
        } else {
            String[] days = repeatDays.split(",");
            for (String day : days) {
                int dayId = Integer.parseInt(day);
                int chipIndex = -1;
                if (dayId == java.util.Calendar.MONDAY)
                    chipIndex = 0;
                else if (dayId == java.util.Calendar.TUESDAY)
                    chipIndex = 1;
                else if (dayId == java.util.Calendar.WEDNESDAY)
                    chipIndex = 2;
                else if (dayId == java.util.Calendar.THURSDAY)
                    chipIndex = 3;
                else if (dayId == java.util.Calendar.FRIDAY)
                    chipIndex = 4;
                else if (dayId == java.util.Calendar.SATURDAY)
                    chipIndex = 5;
                else if (dayId == java.util.Calendar.SUNDAY)
                    chipIndex = 6;

                if (chipIndex != -1) {
                    ((com.google.android.material.chip.Chip) chipGroup.getChildAt(chipIndex)).setChecked(true);
                }
            }
        }

        // Frequency
        if ("CUSTOM".equals(selectedRepeatMode) && customIntervalMillis > 0) {
            radioGroup.check(com.example.reminder.R.id.radioInterval);
            layoutInterval.setVisibility(View.VISIBLE);

            // Calc interval
            if (customIntervalMillis >= 60 * 60 * 1000) {
                long hours = customIntervalMillis / (60 * 60 * 1000);
                etInterval.setText(String.valueOf(hours));
                toggleGroupUnit.check(com.example.reminder.R.id.btnUnitHours);
            } else {
                long mins = customIntervalMillis / (60 * 1000);
                etInterval.setText(String.valueOf(mins));
                toggleGroupUnit.check(com.example.reminder.R.id.btnUnitMins);
            }
        } else {
            radioGroup.check(com.example.reminder.R.id.radioOnceDaily);
            layoutInterval.setVisibility(View.GONE);
            // Default to hours
            toggleGroupUnit.check(com.example.reminder.R.id.btnUnitHours);
        }

        // Window Time
        updateTimeText(tvStart, windowStartC);
        updateTimeText(tvEnd, windowEndC);

        // Listeners
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == com.example.reminder.R.id.radioInterval) {
                layoutInterval.setVisibility(View.VISIBLE);
            } else {
                layoutInterval.setVisibility(View.GONE);
            }
        });

        tvStart.setOnClickListener(v -> showWindowTimePicker(tvStart, true));
        tvEnd.setOnClickListener(v -> showWindowTimePicker(tvEnd, false));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            // Save State
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            // Iterate chips (0=Mon(2)...6=Sun(1))
            int[] calendarDays = {
                    java.util.Calendar.MONDAY, java.util.Calendar.TUESDAY, java.util.Calendar.WEDNESDAY,
                    java.util.Calendar.THURSDAY, java.util.Calendar.FRIDAY, java.util.Calendar.SATURDAY,
                    java.util.Calendar.SUNDAY
            };

            int checkedCount = 0;
            for (int i = 0; i < 7; i++) {
                com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) chipGroup
                        .getChildAt(i);
                if (chip.isChecked()) {
                    if (!first)
                        sb.append(",");
                    sb.append(calendarDays[i]);
                    first = false;
                    checkedCount++;
                }
            }
            repeatDays = sb.toString();

            if (checkedCount == 0) {
                Toast.makeText(requireContext(), "Please select at least one day", Toast.LENGTH_SHORT).show();
                return;
            }

            if (radioGroup.getCheckedRadioButtonId() == com.example.reminder.R.id.radioInterval) {
                selectedRepeatMode = "CUSTOM"; // Using "CUSTOM" to denote detailed interval
                try {
                    long val = Long.parseLong(etInterval.getText().toString());
                    if (toggleGroupUnit.getCheckedButtonId() == com.example.reminder.R.id.btnUnitHours) { // Hours
                        customIntervalMillis = val * 60 * 60 * 1000L;
                    } else { // Minutes
                        customIntervalMillis = val * 60 * 1000L;
                    }
                } catch (NumberFormatException e) {
                    customIntervalMillis = 60 * 60 * 1000L; // Default 1 hour
                }
            } else {
                selectedRepeatMode = "DAILY"; // Standard daily check
                customIntervalMillis = 0;
            }

            updateRepeatText();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showWindowTimePicker(android.widget.TextView tv, boolean isStart) {
        int initialMinutes = isStart ? windowStartC : windowEndC;
        int hour = initialMinutes / 60;
        int minute = initialMinutes % 60;

        com.google.android.material.timepicker.MaterialTimePicker picker = new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_12H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(isStart ? "Window Start" : "Window End")
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int mins = picker.getHour() * 60 + picker.getMinute();
            if (isStart)
                windowStartC = mins;
            else
                windowEndC = mins;
            updateTimeText(tv, mins);
        });

        picker.show(getParentFragmentManager(), "WINDOW_TIME_PICKER");
    }

    private void updateTimeText(android.widget.TextView tv, int minutesFromMidnight) {
        int h = minutesFromMidnight / 60;
        int m = minutesFromMidnight % 60;
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(java.util.Calendar.HOUR_OF_DAY, h);
        c.set(java.util.Calendar.MINUTE, m);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        tv.setText(sdf.format(c.getTime()));
    }

    private void updateRepeatText() {
        if (repeatDays == null || repeatDays.isEmpty()) {
            binding.tvRepeat.setText("Does not repeat");
            return;
        }

        String[] days = repeatDays.split(",");
        String dayText = (days.length == 7) ? "Every Day" : days.length + " Days/Week";

        if ("CUSTOM".equals(selectedRepeatMode)) {
            long min = customIntervalMillis / (60 * 1000);
            String intervalText = (min >= 60) ? (min / 60) + " Hours" : min + " Mins";
            binding.tvRepeat.setText(dayText + ", Every " + intervalText);
        } else if ("DAILY".equals(selectedRepeatMode)) {
            binding.tvRepeat.setText(dayText + " (Once)");
        } else {
            binding.tvRepeat.setText("Does not repeat");
        }
    }

    private void populateFields(Reminder reminder) {
        binding.etTitle.setText(reminder.getTitle());
        binding.etDescription.setText(reminder.getDescription());
        calendar.setTimeInMillis(reminder.getTimeMillis());
        binding.switchAllDay.setChecked(reminder.isAllDay());
        selectedRepeatMode = reminder.getRepeatMode();
        if (selectedRepeatMode == null)
            selectedRepeatMode = "NONE";

        // Load Advanced Options
        repeatDays = reminder.getRepeatDays();
        if ((repeatDays == null || repeatDays.isEmpty()) && "DAILY".equals(selectedRepeatMode)) {
            repeatDays = "1,2,3,4,5,6,7";
        }

        if (reminder.getWindowStart() != null)
            windowStartC = reminder.getWindowStart();
        if (reminder.getWindowEnd() != null)
            windowEndC = reminder.getWindowEnd();

        if ("CUSTOM".equals(selectedRepeatMode)) {
            customIntervalMillis = reminder.getRepeatInterval();
        }
        updateRepeatText();
    }

    private void showDatePicker() {
        com.google.android.material.datepicker.MaterialDatePicker<Long> datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Select Date")
                .setSelection(calendar.getTimeInMillis())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            calendar.setTimeInMillis(selection);
            updateDateButton();
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void showTimePicker() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        com.google.android.material.timepicker.MaterialTimePicker timePicker = new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                .setTimeFormat(android.text.format.DateFormat.is24HourFormat(requireContext())
                        ? com.google.android.material.timepicker.TimeFormat.CLOCK_24H
                        : com.google.android.material.timepicker.TimeFormat.CLOCK_12H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("Select Time")
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            calendar.set(Calendar.MINUTE, timePicker.getMinute());
            calendar.set(Calendar.SECOND, 0);
            updateTimeButton();
        });

        timePicker.show(getParentFragmentManager(), "TIME_PICKER");
    }

    private void updateDateButton() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        binding.btnDate.setText(sdf.format(calendar.getTime()));
    }

    private void updateTimeButton() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        binding.btnTime.setText(sdf.format(calendar.getTime()));
    }

    private void saveReminder() {
        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            binding.etTitle.setError("Title is required");
            return;
        }

        // Allow saving past times if it's a repeating reminder (it will confirm next
        // occurrence)
        if (calendar.getTimeInMillis() <= System.currentTimeMillis() && "NONE".equals(selectedRepeatMode)) {
            // Optional: warn user
        }

        Reminder reminder;
        if (existingReminder != null) {
            reminder = existingReminder;
            reminder.setTitle(title);
            reminder.setDescription(description);
            reminder.setTimeMillis(calendar.getTimeInMillis());
            reminder.setAllDay(binding.switchAllDay.isChecked());
            reminder.setRepeatMode(selectedRepeatMode);
            reminder.setRepeatInterval(customIntervalMillis);
            // Advanced
            reminder.setRepeatDays(repeatDays);
            reminder.setWindowStart(windowStartC);
            reminder.setWindowEnd(windowEndC);
        } else {
            reminder = new Reminder(title, description, calendar.getTimeInMillis(),
                    binding.switchAllDay.isChecked(), selectedRepeatMode, customIntervalMillis, 0);
            // Advanced setters
            reminder.setRepeatDays(repeatDays);
            reminder.setWindowStart(windowStartC);
            reminder.setWindowEnd(windowEndC);
            // Default custom reminders to show on widget
            reminder.setHideFromWidget(false);
        }

        viewModel.saveReminder(reminder);

        // Check for widgets and prompt
        checkAndPromptForWidget();
    }

    private void checkAndPromptForWidget() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.appwidget.AppWidgetManager appWidgetManager = android.appwidget.AppWidgetManager
                    .getInstance(requireContext());
            android.content.ComponentName myProvider = new android.content.ComponentName(requireContext(),
                    com.example.reminder.widget.StickyNoteWidgetProvider.class);

            if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                // Check if we already have widgets pinned
                int[] existingIds = appWidgetManager.getAppWidgetIds(myProvider);
                if (existingIds != null && existingIds.length > 0) {
                    // Widget exists, don't pester user
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    // No widget, offer to add one
                    showAddWidgetDialog();
                }
            } else {
                Navigation.findNavController(requireView()).navigateUp();
            }
        } else {
            Navigation.findNavController(requireView()).navigateUp();
        }
    }

    private void showAddWidgetDialog() {
        android.view.View dialogView = LayoutInflater.from(requireContext())
                .inflate(com.example.reminder.R.layout.dialog_add_widget, null);

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(com.example.reminder.R.id.btnAddWidget).setOnClickListener(v -> {
            requestPinWidget();
            Navigation.findNavController(requireView()).navigateUp();
            dialog.dismiss();
        });

        dialogView.findViewById(com.example.reminder.R.id.btnNoThanks).setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void requestPinWidget() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.appwidget.AppWidgetManager appWidgetManager = requireContext()
                    .getSystemService(android.appwidget.AppWidgetManager.class);
            android.content.ComponentName myProvider = new android.content.ComponentName(requireContext(),
                    com.example.reminder.widget.StickyNoteWidgetProvider.class);

            if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                appWidgetManager.requestPinAppWidget(myProvider, null, null);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
