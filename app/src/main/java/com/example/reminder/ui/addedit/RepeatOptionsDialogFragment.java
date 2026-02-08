package com.example.reminder.ui.addedit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.reminder.R;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Dialog fragment for configuring repeat options.
 * Extracted from AddEditFragment to follow Single Responsibility Principle.
 */
public class RepeatOptionsDialogFragment extends DialogFragment {

    public interface OnRepeatOptionsSelectedListener {
        void onRepeatOptionsSelected(RepeatOptions options);
    }

    /**
     * Data class holding repeat configuration.
     */
    public static class RepeatOptions {
        public String repeatMode;
        public long intervalMillis;
        public String repeatDays;
        public int windowStart;
        public int windowEnd;

        public RepeatOptions(String repeatMode, long intervalMillis, String repeatDays,
                int windowStart, int windowEnd) {
            this.repeatMode = repeatMode;
            this.intervalMillis = intervalMillis;
            this.repeatDays = repeatDays;
            this.windowStart = windowStart;
            this.windowEnd = windowEnd;
        }
    }

    private static final String ARG_REPEAT_MODE = "repeat_mode";
    private static final String ARG_INTERVAL_MILLIS = "interval_millis";
    private static final String ARG_REPEAT_DAYS = "repeat_days";
    private static final String ARG_WINDOW_START = "window_start";
    private static final String ARG_WINDOW_END = "window_end";
    private static final String ARG_IS_DAILY_ROUTINE = "is_daily_routine";

    private OnRepeatOptionsSelectedListener listener;
    private int windowStart = 480; // 8:00 AM
    private int windowEnd = 1200; // 8:00 PM

    public static RepeatOptionsDialogFragment newInstance(
            String repeatMode,
            long intervalMillis,
            String repeatDays,
            int windowStart,
            int windowEnd,
            boolean isDailyRoutine) {
        RepeatOptionsDialogFragment fragment = new RepeatOptionsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REPEAT_MODE, repeatMode);
        args.putLong(ARG_INTERVAL_MILLIS, intervalMillis);
        args.putString(ARG_REPEAT_DAYS, repeatDays);
        args.putInt(ARG_WINDOW_START, windowStart);
        args.putInt(ARG_WINDOW_END, windowEnd);
        args.putBoolean(ARG_IS_DAILY_ROUTINE, isDailyRoutine);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnRepeatOptionsSelectedListener(OnRepeatOptionsSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_repeat_advanced, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String repeatMode = args != null ? args.getString(ARG_REPEAT_MODE, "NONE") : "NONE";
        long intervalMillis = args != null ? args.getLong(ARG_INTERVAL_MILLIS, 0) : 0;
        String repeatDays = args != null ? args.getString(ARG_REPEAT_DAYS, "") : "";
        windowStart = args != null ? args.getInt(ARG_WINDOW_START, 480) : 480;
        windowEnd = args != null ? args.getInt(ARG_WINDOW_END, 1200) : 1200;
        boolean isDailyRoutine = args != null && args.getBoolean(ARG_IS_DAILY_ROUTINE, false);

        setupUI(view, repeatMode, intervalMillis, repeatDays, isDailyRoutine);
    }

    private void setupUI(View view, String repeatMode, long intervalMillis,
            String repeatDays, boolean isDailyRoutine) {
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupDays);
        RadioGroup radioGroup = view.findViewById(R.id.radioGroupFrequency);
        LinearLayout layoutInterval = view.findViewById(R.id.layoutIntervalOptions);
        EditText etInterval = view.findViewById(R.id.etIntervalValue);
        MaterialButtonToggleGroup toggleGroupUnit = view.findViewById(R.id.toggleGroupUnit);
        TextView tvStart = view.findViewById(R.id.tvStartTime);
        TextView tvEnd = view.findViewById(R.id.tvEndTime);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        // Hide days if daily routine
        if (isDailyRoutine) {
            chipGroup.setVisibility(View.GONE);
            LinearLayout root = (LinearLayout) chipGroup.getParent();
            if (root.getChildCount() > 1) {
                root.getChildAt(1).setVisibility(View.GONE);
            }
        }

        // Load current state
        loadDaysState(chipGroup, repeatDays);
        loadFrequencyState(radioGroup, layoutInterval, etInterval, toggleGroupUnit, repeatMode, intervalMillis);
        updateTimeText(tvStart, windowStart);
        updateTimeText(tvEnd, windowEnd);

        // Listeners
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            layoutInterval.setVisibility(checkedId == R.id.radioInterval ? View.VISIBLE : View.GONE);
        });

        tvStart.setOnClickListener(v -> showWindowTimePicker(tvStart, true));
        tvEnd.setOnClickListener(v -> showWindowTimePicker(tvEnd, false));
        btnCancel.setOnClickListener(v -> dismiss());

        btnConfirm.setOnClickListener(v -> {
            RepeatOptions options = buildRepeatOptions(chipGroup, radioGroup, etInterval,
                    toggleGroupUnit, isDailyRoutine);
            if (options != null && listener != null) {
                listener.onRepeatOptionsSelected(options);
                dismiss();
            }
        });
    }

    private void loadDaysState(ChipGroup chipGroup, String repeatDays) {
        if (repeatDays == null || repeatDays.isEmpty()) {
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                ((Chip) chipGroup.getChildAt(i)).setChecked(true);
            }
        } else {
            int[] calendarDays = {
                    Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                    Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
            };
            String[] days = repeatDays.split(",");
            for (String day : days) {
                int dayId = Integer.parseInt(day);
                for (int i = 0; i < calendarDays.length; i++) {
                    if (calendarDays[i] == dayId) {
                        ((Chip) chipGroup.getChildAt(i)).setChecked(true);
                        break;
                    }
                }
            }
        }
    }

    private void loadFrequencyState(RadioGroup radioGroup, LinearLayout layoutInterval,
            EditText etInterval, MaterialButtonToggleGroup toggleGroupUnit,
            String repeatMode, long intervalMillis) {
        if ("CUSTOM".equals(repeatMode) && intervalMillis > 0) {
            radioGroup.check(R.id.radioInterval);
            layoutInterval.setVisibility(View.VISIBLE);

            if (intervalMillis >= 60 * 60 * 1000) {
                long hours = intervalMillis / (60 * 60 * 1000);
                etInterval.setText(String.valueOf(hours));
                toggleGroupUnit.check(R.id.btnUnitHours);
            } else {
                long mins = intervalMillis / (60 * 1000);
                etInterval.setText(String.valueOf(mins));
                toggleGroupUnit.check(R.id.btnUnitMins);
            }
        } else {
            radioGroup.check(R.id.radioOnceDaily);
            layoutInterval.setVisibility(View.GONE);
            toggleGroupUnit.check(R.id.btnUnitHours);
        }
    }

    private RepeatOptions buildRepeatOptions(ChipGroup chipGroup, RadioGroup radioGroup,
            EditText etInterval, MaterialButtonToggleGroup toggleGroupUnit,
            boolean isDailyRoutine) {
        String repeatDays;
        if (isDailyRoutine) {
            repeatDays = "1,2,3,4,5,6,7";
        } else {
            repeatDays = buildRepeatDaysString(chipGroup);
            if (repeatDays.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one day", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        String repeatMode;
        long intervalMillis;

        if (radioGroup.getCheckedRadioButtonId() == R.id.radioInterval) {
            repeatMode = "CUSTOM";
            try {
                long val = Long.parseLong(etInterval.getText().toString());
                if (toggleGroupUnit.getCheckedButtonId() == R.id.btnUnitHours) {
                    intervalMillis = val * 60 * 60 * 1000L;
                } else {
                    intervalMillis = val * 60 * 1000L;
                }
            } catch (NumberFormatException e) {
                intervalMillis = 60 * 60 * 1000L;
            }
        } else {
            repeatMode = "DAILY";
            intervalMillis = 0;
        }

        return new RepeatOptions(repeatMode, intervalMillis, repeatDays, windowStart, windowEnd);
    }

    private String buildRepeatDaysString(ChipGroup chipGroup) {
        int[] calendarDays = {
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        };
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (int i = 0; i < 7; i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                if (!first)
                    sb.append(",");
                sb.append(calendarDays[i]);
                first = false;
            }
        }
        return sb.toString();
    }

    private void showWindowTimePicker(TextView tv, boolean isStart) {
        int initialMinutes = isStart ? windowStart : windowEnd;
        int hour = initialMinutes / 60;
        int minute = initialMinutes % 60;

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(isStart ? "Window Start" : "Window End")
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int mins = picker.getHour() * 60 + picker.getMinute();
            if (isStart)
                windowStart = mins;
            else
                windowEnd = mins;
            updateTimeText(tv, mins);
        });

        picker.show(getParentFragmentManager(), "WINDOW_TIME_PICKER");
    }

    private void updateTimeText(TextView tv, int minutesFromMidnight) {
        int h = minutesFromMidnight / 60;
        int m = minutesFromMidnight % 60;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, h);
        c.set(Calendar.MINUTE, m);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        tv.setText(sdf.format(c.getTime()));
    }
}
