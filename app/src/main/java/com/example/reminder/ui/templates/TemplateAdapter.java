package com.example.reminder.ui.templates;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reminder.R;
import com.example.reminder.templates.ReminderTemplate;
import com.example.reminder.templates.TimeSlot;
import java.util.ArrayList;
import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder> {

    private List<ReminderTemplate> templates = new ArrayList<>();
    private OnTemplateClickListener listener;

    public interface OnTemplateClickListener {
        void onTemplateClick(ReminderTemplate template);
    }

    public TemplateAdapter(OnTemplateClickListener listener) {
        this.listener = listener;
    }

    public void setTemplates(List<ReminderTemplate> templates) {
        this.templates = templates;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_template_card, parent, false);
        return new TemplateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        ReminderTemplate template = templates.get(position);
        holder.bind(template);
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    class TemplateViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEmoji;
        private final TextView tvTitle;
        private final TextView tvFrequency;
        private final TextView tvTimes;

        public TemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
            tvTimes = itemView.findViewById(R.id.tvTimes);
        }

        public void bind(ReminderTemplate template) {
            tvEmoji.setText(template.getEmoji());
            tvTitle.setText(template.getTitle());
            tvFrequency.setText(template.getFrequencyText());

            // Format times
            List<TimeSlot> times = template.getSuggestedTimes();
            if (!times.isEmpty()) {
                StringBuilder timesText = new StringBuilder();
                for (int i = 0; i < times.size(); i++) {
                    if (i > 0)
                        timesText.append(" • ");
                    timesText.append(times.get(i).getFormattedTime());
                }
                tvTimes.setText(timesText.toString());
                tvTimes.setVisibility(View.VISIBLE);
            } else {
                tvTimes.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTemplateClick(template);
                }
            });
        }
    }
}
