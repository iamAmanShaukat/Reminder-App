package com.example.reminder.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reminder.R;
import com.example.reminder.data.Reminder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderAdapter extends ListAdapter<Reminder, ReminderAdapter.ReminderViewHolder> {

    private final OnItemClickListener listener;
    private boolean isSelectionMode = false;
    private final java.util.Set<Reminder> selectedItems = new java.util.HashSet<>();

    public interface OnItemClickListener {
        void onItemClick(Reminder reminder);

        void onCheckBoxClick(Reminder reminder, boolean isChecked);

        void onDeleteClick(Reminder reminder);

        void onSelectionModeChanged(boolean enabled, int selectedCount);
    }

    public ReminderAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void clearSelection() {
        selectedItems.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
        listener.onSelectionModeChanged(false, 0);
    }

    public java.util.List<Reminder> getSelectedItems() {
        return new java.util.ArrayList<>(selectedItems);
    }

    private static final DiffUtil.ItemCallback<Reminder> DIFF_CALLBACK = new DiffUtil.ItemCallback<Reminder>() {
        @Override
        public boolean areItemsTheSame(@NonNull Reminder oldItem, @NonNull Reminder newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Reminder oldItem, @NonNull Reminder newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getTimeMillis() == newItem.getTimeMillis() &&
                    oldItem.isCompleted() == newItem.isCompleted();
        }
    };

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = getItem(position);
        holder.bind(reminder, selectedItems.contains(reminder));
    }

    class ReminderViewHolder extends RecyclerView.ViewHolder {
        private final android.widget.LinearLayout containerHeader;
        private final TextView tvHeaderDate;
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvTime;
        private final CheckBox cbComplete;
        private final android.widget.ImageButton btnDelete;
        private final com.google.android.material.card.MaterialCardView cardView;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            // Root is now LinearLayout, find MaterialCardView inside
            cardView = itemView.findViewById(R.id.cardView);

            containerHeader = itemView.findViewById(R.id.containerHeader);
            tvHeaderDate = itemView.findViewById(R.id.tvHeaderDate);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            cbComplete = itemView.findViewById(R.id.cbComplete);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            // ... (Listeners implementation remains same, but need to reconstruct since I'm
            // replacing the constructor/class body)
            View contentContainer = itemView.findViewById(R.id.containerContent);

            contentContainer.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Reminder item = getItem(position);
                    if (isSelectionMode) {
                        toggleSelection(item);
                    } else if (listener != null) {
                        listener.onItemClick(item);
                    }
                }
            });

            contentContainer.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Reminder item = getItem(position);
                    if (!isSelectionMode) {
                        isSelectionMode = true;
                        selectedItems.add(item);
                        notifyDataSetChanged();
                        listener.onSelectionModeChanged(true, selectedItems.size());
                        return true;
                    }
                }
                return false;
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(getItem(position));
                }
            });

            cbComplete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Reminder item = getItem(position);
                    boolean isChecked = cbComplete.isChecked();
                    // Instant UI Update
                    if (isChecked) {
                        cardView.setStrokeColor(itemView.getContext().getColor(R.color.color_check));
                        cardView.setStrokeWidth((int) (2 * itemView.getResources().getDisplayMetrics().density));
                    } else {
                        cardView.setStrokeWidth(0);
                    }
                    listener.onCheckBoxClick(item, isChecked);
                }
            });
        }

        private void toggleSelection(Reminder item) {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item);
            } else {
                selectedItems.add(item);
            }
            if (selectedItems.isEmpty()) {
                isSelectionMode = false;
            }
            notifyItemChanged(getAdapterPosition());
            listener.onSelectionModeChanged(isSelectionMode, selectedItems.size());
        }

        public void bind(Reminder reminder, boolean isSelected) {
            tvTitle.setText(reminder.getTitle());
            if (reminder.getDescription() != null && !reminder.getDescription().isEmpty()) {
                tvDescription.setText(reminder.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            SimpleDateFormat dateFormat;
            String repeatMode = reminder.getRepeatMode();
            if (repeatMode != null && !repeatMode.equals("NONE")) {
                dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            } else {
                dateFormat = new SimpleDateFormat("EEE, d MMM - hh:mm a", Locale.getDefault());
            }
            tvTime.setText(dateFormat.format(new Date(reminder.getTimeMillis())));

            cbComplete.setOnCheckedChangeListener(null);
            cbComplete.setChecked(reminder.isCompleted());

            // Check if task is overdue (before today and not completed)
            java.util.Calendar today = java.util.Calendar.getInstance();
            today.set(java.util.Calendar.HOUR_OF_DAY, 0);
            today.set(java.util.Calendar.MINUTE, 0);
            today.set(java.util.Calendar.SECOND, 0);
            today.set(java.util.Calendar.MILLISECOND, 0);

            boolean isOverdue = reminder.getTimeMillis() < today.getTimeInMillis() && !reminder.isCompleted();

            // Styling Priority: Selected > Completed > Overdue > Normal
            if (isSelected) {
                // Selection mode - red border
                cardView.setStrokeWidth((int) (3 * itemView.getResources().getDisplayMetrics().density));
                cardView.setStrokeColor(itemView.getContext().getColor(R.color.color_delete));
                cardView.setChecked(true);
            } else if (reminder.isCompleted()) {
                // Completed - green border
                cardView.setStrokeColor(itemView.getContext().getColor(R.color.color_check));
                cardView.setStrokeWidth((int) (2 * itemView.getResources().getDisplayMetrics().density));
                cardView.setChecked(false);
            } else {
                // Normal or Overdue - no border
                cardView.setStrokeWidth(0);
                cardView.setChecked(false);
            }

            // Set time text color: red for overdue, default for normal
            if (isOverdue) {
                tvTime.setTextColor(itemView.getContext().getColor(R.color.color_delete));
            } else {
                tvTime.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
            }

            // Repeat Days Indicator
            TextView tvRepeatDays = itemView.findViewById(R.id.tvRepeatDays);
            String daysStr = reminder.getRepeatDays();
            String mode = reminder.getRepeatMode();

            if (mode != null && !mode.equals("NONE")) {
                tvRepeatDays.setVisibility(View.VISIBLE);

                // Determine active days
                java.util.Set<Integer> activeDays = new java.util.HashSet<>();
                if ("DAILY".equals(mode)) {
                    // All days
                    for (int i = 1; i <= 7; i++)
                        activeDays.add(i);
                } else if (daysStr != null && !daysStr.isEmpty()) {
                    for (String d : daysStr.split(",")) {
                        try {
                            activeDays.add(Integer.parseInt(d.trim()));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                } else if ("WEEKLY".equals(mode)) {
                    // Fallback to day of week of start time
                    java.util.Calendar c = java.util.Calendar.getInstance();
                    c.setTimeInMillis(reminder.getTimeMillis());
                    activeDays.add(c.get(java.util.Calendar.DAY_OF_WEEK));
                }

                // Build "S M T W T F S"
                // Calendar.SUNDAY = 1 -> S
                // ...
                // Calendar.SATURDAY = 7 -> S
                // Map: 1->S, 2->M, 3->T, 4->W, 5->T, 6->F, 7->S (order in string)

                // We want order: S M T W T F S (Sun to Sat) OR M T W T F S S?
                // US convention starts Sunday. Let's use S M T W T F S.
                String fullText = "S M T W T F S";
                android.text.SpannableString spannable = new android.text.SpannableString(fullText);

                // Indices in string "S M T W T F S"
                // S(0) M(2) T(4) W(6) T(8) F(10) S(12)
                int[] mapDayToCharIndex = new int[8]; // 1-based index
                mapDayToCharIndex[java.util.Calendar.SUNDAY] = 0;
                mapDayToCharIndex[java.util.Calendar.MONDAY] = 2;
                mapDayToCharIndex[java.util.Calendar.TUESDAY] = 4;
                mapDayToCharIndex[java.util.Calendar.WEDNESDAY] = 6;
                mapDayToCharIndex[java.util.Calendar.THURSDAY] = 8;
                mapDayToCharIndex[java.util.Calendar.FRIDAY] = 10;
                mapDayToCharIndex[java.util.Calendar.SATURDAY] = 12;

                int activeColor = itemView.getContext().getColor(R.color.brand_accent);
                int inactiveColor = itemView.getContext().getColor(R.color.text_tertiary);

                for (int day = java.util.Calendar.SUNDAY; day <= java.util.Calendar.SATURDAY; day++) {
                    int idx = mapDayToCharIndex[day];
                    int color = activeDays.contains(day) ? activeColor : inactiveColor;

                    spannable.setSpan(new android.text.style.ForegroundColorSpan(color),
                            idx, idx + 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Bold for active
                    if (activeDays.contains(day)) {
                        spannable.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                idx, idx + 1, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                tvRepeatDays.setText(spannable);

            } else {
                tvRepeatDays.setVisibility(View.GONE);
            }

            if (isSelectionMode) {
                btnDelete.setVisibility(View.GONE);
                cbComplete.setVisibility(View.GONE);
            } else {
                btnDelete.setVisibility(View.VISIBLE);
                cbComplete.setVisibility(View.VISIBLE);
            }

            // Header Logic
            int position = getAdapterPosition();
            boolean showHeader = false;
            if (position == 0) {
                showHeader = true; // First item always shows header
            } else if (position > 0) {
                Reminder previousStart = getItem(position - 1);
                // Compare dates (Day/Month/Year)
                java.util.Calendar cal1 = java.util.Calendar.getInstance();
                cal1.setTimeInMillis(reminder.getTimeMillis());

                java.util.Calendar cal2 = java.util.Calendar.getInstance();
                cal2.setTimeInMillis(previousStart.getTimeMillis());

                boolean sameDay = cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                        cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);

                if (!sameDay) {
                    showHeader = true;
                }
            }

            if (showHeader) {
                containerHeader.setVisibility(View.VISIBLE);
                // Format: "Today", "Yesterday", or "Fri, Feb 14"
                tvHeaderDate.setText(getDateTitle(reminder.getTimeMillis()));
            } else {
                containerHeader.setVisibility(View.GONE);
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

            now.add(java.util.Calendar.DAY_OF_YEAR, -2); // Go back to yesterday (today-1)
            if (target.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
                    target.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR)) {
                return "Yesterday";
            }

            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
            return sdf.format(new Date(timeMillis));
        }
    }
}
