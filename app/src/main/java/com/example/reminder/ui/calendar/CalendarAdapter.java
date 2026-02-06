package com.example.reminder.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reminder.R;
import com.example.reminder.data.Reminder;
import com.example.reminder.databinding.ItemCalendarDayBinding;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    private final List<LocalDate> days = new ArrayList<>();
    private final Map<LocalDate, List<Reminder>> reminders;
    private LocalDate selectedDate;
    private final OnDateClickListener listener;

    public interface OnDateClickListener {
        void onDateClick(LocalDate date);
    }

    public CalendarAdapter(Map<LocalDate, List<Reminder>> reminders, OnDateClickListener listener) {
        this.reminders = reminders;
        this.listener = listener;
        this.selectedDate = LocalDate.now();
    }

    public void setDays(List<LocalDate> newDays) {
        days.clear();
        days.addAll(newDays);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCalendarDayBinding binding = ItemCalendarDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        LocalDate date = days.get(position);
        if (date == null) {
            holder.bindEmpty();
        } else {
            List<Reminder> dailyReminders = reminders.get(date);
            holder.bind(date, dailyReminders, selectedDate != null && selectedDate.equals(date));
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private final ItemCalendarDayBinding binding;

        public DayViewHolder(ItemCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindEmpty() {
            binding.tvDayNumber.setText("");
            binding.containerDots.removeAllViews();
            binding.getRoot().setOnClickListener(null);
            binding.tvDayNumber.setBackground(null);
        }

        public void bind(LocalDate date, List<Reminder> dailyReminders, boolean isSelected) {
            binding.tvDayNumber.setText(String.valueOf(date.getDayOfMonth()));

            // Selection State
            if (isSelected) {
                binding.tvDayNumber.setBackgroundResource(R.drawable.bg_calendar_day_selected);
                binding.tvDayNumber.setTextColor(itemView.getContext().getColor(R.color.bg_primary));
            } else if (LocalDate.now().equals(date)) {
                binding.tvDayNumber.setBackgroundResource(R.drawable.bg_calendar_today); // Need this drawable
                binding.tvDayNumber.setTextColor(itemView.getContext().getColor(R.color.primary));
            } else {
                binding.tvDayNumber.setBackground(null);
                binding.tvDayNumber.setTextColor(itemView.getContext().getColor(R.color.text_primary));
            }

            // Dots
            binding.containerDots.removeAllViews();
            if (dailyReminders != null && !dailyReminders.isEmpty()) {
                int maxDots = 4;
                for (int i = 0; i < Math.min(dailyReminders.size(), maxDots); i++) {
                    Reminder r = dailyReminders.get(i);
                    // Add dot view
                    View dot = new View(itemView.getContext());
                    int size = (int) (4 * itemView.getContext().getResources().getDisplayMetrics().density);
                    int margin = (int) (1 * itemView.getContext().getResources().getDisplayMetrics().density);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                    params.setMargins(margin, 0, margin, 0);
                    dot.setLayoutParams(params);

                    // Circular shape using shape drawable programmatically or XML
                    android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
                    shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                    shape.setColor(r.getColor() != 0 ? r.getColor() : itemView.getContext().getColor(R.color.primary));
                    dot.setBackground(shape);

                    binding.containerDots.addView(dot);
                }
            }

            binding.getRoot().setOnClickListener(v -> {
                selectedDate = date;
                notifyDataSetChanged();
                listener.onDateClick(date);
            });
        }
    }
}
