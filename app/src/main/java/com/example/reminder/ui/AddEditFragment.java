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

    private void showRepeatOptions() {
        String[] options = { "Does not repeat", "Every Hour", "Every Day", "Every Week", "Every Month",
                "Custom (Minutes)" };
        final String[] modes = { "NONE", "HOURLY", "DAILY", "WEEKLY", "MONTHLY", "CUSTOM" };

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Repeat")
                .setItems(options, (dialog, which) -> {
                    String mode = modes[which];
                    if ("CUSTOM".equals(mode)) {
                        showCustomRepeatDialog();
                    } else {
                        selectedRepeatMode = mode;
                        updateRepeatText();
                    }
                })
                .show();
    }

    private void showCustomRepeatDialog() {
        final android.widget.Spinner spinner = new android.widget.Spinner(requireContext());

        final java.util.List<String> labels = new java.util.ArrayList<>();
        final java.util.List<Long> values = new java.util.ArrayList<>();

        // 1 to 5 minutes
        for (int i = 1; i <= 5; i++) {
            labels.add(i + " Minute" + (i > 1 ? "s" : ""));
            values.add(i * 60 * 1000L);
        }

        // 15 minute intervals up to 2 hours
        labels.add("15 Minutes");
        values.add(15 * 60 * 1000L);
        labels.add("30 Minutes");
        values.add(30 * 60 * 1000L);
        labels.add("45 Minutes");
        values.add(45 * 60 * 1000L);
        labels.add("1 Hour");
        values.add(60 * 60 * 1000L);

        // Larger intervals
        labels.add("1.5 Hours");
        values.add(90 * 60 * 1000L);
        labels.add("2 Hours");
        values.add(120 * 60 * 1000L);
        labels.add("3 Hours");
        values.add(180 * 60 * 1000L);
        labels.add("4 Hours");
        values.add(240 * 60 * 1000L);
        labels.add("6 Hours");
        values.add(360 * 60 * 1000L);
        labels.add("8 Hours");
        values.add(480 * 60 * 1000L);
        labels.add("12 Hours");
        values.add(720 * 60 * 1000L);
        labels.add("24 Hours");
        values.add(24 * 60 * 60 * 1000L);

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Add some padding
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        spinner.setPadding(padding, padding, padding, padding);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Interval")
                .setView(spinner)
                .setPositiveButton("Set", (dialog, which) -> {
                    int selectedPosition = spinner.getSelectedItemPosition();
                    if (selectedPosition >= 0 && selectedPosition < values.size()) {
                        selectedRepeatMode = "CUSTOM";
                        customIntervalMillis = values.get(selectedPosition);
                        updateRepeatText();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateRepeatText() {
        switch (selectedRepeatMode) {
            case "HOURLY":
                binding.tvRepeat.setText("Every Hour");
                break;
            case "DAILY":
                binding.tvRepeat.setText("Every Day");
                break;
            case "WEEKLY":
                binding.tvRepeat.setText("Every Week");
                break;
            case "MONTHLY":
                binding.tvRepeat.setText("Every Month");
                break;
            case "CUSTOM":
                long minutes = customIntervalMillis / (60 * 1000);
                binding.tvRepeat.setText("Every " + minutes + " minutes");
                break;
            default:
                binding.tvRepeat.setText("Does not repeat");
                break;
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

        if ("CUSTOM".equals(selectedRepeatMode)) {
            customIntervalMillis = reminder.getRepeatInterval();
        }
        updateRepeatText();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateButton();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    updateTimeButton();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(requireContext()));
        timePickerDialog.show();
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

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(getContext(), "Please select a future time", Toast.LENGTH_SHORT).show();
            // Allow saving anyway
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
        } else {
            reminder = new Reminder(title, description, calendar.getTimeInMillis(),
                    binding.switchAllDay.isChecked(), selectedRepeatMode, customIntervalMillis, 0);
        }

        viewModel.saveReminder(reminder);

        // Check for widgets and prompt
        checkAndPromptForWidget();
    }

    private void checkAndPromptForWidget() {
        android.appwidget.AppWidgetManager appWidgetManager = android.appwidget.AppWidgetManager
                .getInstance(requireContext());
        android.content.ComponentName myProvider = new android.content.ComponentName(requireContext(),
                com.example.reminder.widget.StickyNoteWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(myProvider);

        if (appWidgetIds.length == 0 && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O
                && appWidgetManager.isRequestPinAppWidgetSupported()) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Add Widget?")
                    .setMessage("Keep your reminders visible! Would you like to add a widget to your home screen?")
                    .setPositiveButton("Add Widget", (dialog, which) -> {
                        requestPinWidget();
                        Navigation.findNavController(requireView()).navigateUp();
                    })
                    .setNegativeButton("No Thanks", (dialog, which) -> {
                        Navigation.findNavController(requireView()).navigateUp();
                    })
                    .setCancelable(false)
                    .show();
        } else {
            Navigation.findNavController(requireView()).navigateUp();
        }
    }

    private void requestPinWidget() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.appwidget.AppWidgetManager appWidgetManager = requireContext()
                    .getSystemService(android.appwidget.AppWidgetManager.class);
            android.content.ComponentName myProvider = new android.content.ComponentName(requireContext(),
                    com.example.reminder.widget.StickyNoteWidgetProvider.class);

            if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                // Using null intent for simplicity as we don't need a specific callback action
                // yet
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
