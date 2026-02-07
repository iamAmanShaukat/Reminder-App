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
        android.view.LayoutInflater inflater = LayoutInflater.from(requireContext());
        android.view.View dialogView = inflater.inflate(com.example.reminder.R.layout.dialog_repeat_options, null);

        final String[] modes = { "NONE", "HOURLY", "DAILY", "WEEKLY", "MONTHLY", "CUSTOM" };

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        // Make background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Set click listeners
        dialogView.findViewById(com.example.reminder.R.id.option_none).setOnClickListener(v -> {
            selectedRepeatMode = modes[0];
            updateRepeatText();
            dialog.dismiss();
        });

        dialogView.findViewById(com.example.reminder.R.id.option_hourly).setOnClickListener(v -> {
            selectedRepeatMode = modes[1];
            updateRepeatText();
            dialog.dismiss();
        });

        dialogView.findViewById(com.example.reminder.R.id.option_daily).setOnClickListener(v -> {
            selectedRepeatMode = modes[2];
            updateRepeatText();
            dialog.dismiss();
        });

        dialogView.findViewById(com.example.reminder.R.id.option_weekly).setOnClickListener(v -> {
            selectedRepeatMode = modes[3];
            updateRepeatText();
            dialog.dismiss();
        });

        dialogView.findViewById(com.example.reminder.R.id.option_monthly).setOnClickListener(v -> {
            selectedRepeatMode = modes[4];
            updateRepeatText();
            dialog.dismiss();
        });

        dialogView.findViewById(com.example.reminder.R.id.option_custom).setOnClickListener(v -> {
            dialog.dismiss();
            showCustomRepeatDialog();
        });

        dialog.show();
    }

    private void showCustomRepeatDialog() {
        android.view.LayoutInflater inflater = LayoutInflater.from(requireContext());
        android.view.View dialogView = inflater.inflate(com.example.reminder.R.layout.dialog_interval_options, null);
        android.widget.LinearLayout container = dialogView
                .findViewById(com.example.reminder.R.id.interval_options_container);

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

        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                requireContext());
        dialog.setContentView(dialogView);

        // Make background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Configure bottom sheet behavior for swipe-to-dismiss
        dialog.getBehavior().setDraggable(true);
        dialog.getBehavior().setPeekHeight(350 * (int) getResources().getDisplayMetrics().density);
        dialog.getBehavior().setHideable(true);

        // Dynamically add options
        for (int i = 0; i < labels.size(); i++) {
            final int index = i;
            android.widget.TextView option = new android.widget.TextView(requireContext());
            option.setText(labels.get(i));
            option.setTextColor(getResources().getColor(com.example.reminder.R.color.text_primary, null));
            option.setTextSize(16);
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            option.setPadding(padding, padding, padding, padding);

            // Set ripple background using TypedValue
            android.util.TypedValue outValue = new android.util.TypedValue();
            requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            option.setBackgroundResource(outValue.resourceId);

            option.setClickable(true);
            option.setFocusable(true);

            option.setOnClickListener(v -> {
                selectedRepeatMode = "CUSTOM";
                customIntervalMillis = values.get(index);
                updateRepeatText();
                dialog.dismiss();
            });

            container.addView(option);
        }

        dialog.show();
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
                .setTimeFormat(DateFormat.is24HourFormat(requireContext())
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

            android.view.LayoutInflater inflater = LayoutInflater.from(requireContext());
            android.view.View dialogView = inflater.inflate(com.example.reminder.R.layout.dialog_add_widget, null);

            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            // Make background transparent
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            // Set button click listeners
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
