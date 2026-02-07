package com.example.reminder.ui.templates;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reminder.R;
import com.example.reminder.data.Reminder;
import com.example.reminder.data.ReminderRepository;
import com.example.reminder.templates.ReminderTemplate;
import com.example.reminder.templates.TemplateCategory;
import com.example.reminder.templates.TemplateManager;
import com.example.reminder.templates.TimeSlot;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import java.util.Calendar;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TemplateDialogFragment extends BottomSheetDialogFragment
        implements TemplateAdapter.OnTemplateClickListener {

    @Inject
    ReminderRepository repository;

    @Inject
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context appContext;

    private RecyclerView rvTemplates;
    private ChipGroup chipGroupCategories;
    private TemplateAdapter adapter;
    private TemplateManager templateManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), getTheme());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_template_selector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Make background transparent
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        templateManager = TemplateManager.getInstance();

        rvTemplates = view.findViewById(R.id.rvTemplates);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);

        setupRecyclerView();
        setupCategoryFilters();

        // Load all templates initially
        loadTemplates(TemplateCategory.ALL);
    }

    private void setupRecyclerView() {
        adapter = new TemplateAdapter(this);
        rvTemplates.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTemplates.setAdapter(adapter);
    }

    private void setupCategoryFilters() {
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty())
                return;

            int checkedId = checkedIds.get(0);
            TemplateCategory category = TemplateCategory.ALL;

            if (checkedId == R.id.chipHealth) {
                category = TemplateCategory.HEALTH;
            } else if (checkedId == R.id.chipProductivity) {
                category = TemplateCategory.PRODUCTIVITY;
            } else if (checkedId == R.id.chipPersonal) {
                category = TemplateCategory.PERSONAL;
            }

            loadTemplates(category);
        });
    }

    private void loadTemplates(TemplateCategory category) {
        List<ReminderTemplate> templates = templateManager.getTemplatesByCategory(category);
        adapter.setTemplates(templates);
    }

    @Override
    public void onTemplateClick(ReminderTemplate template) {
        // Create reminders from template
        createRemindersFromTemplate(template);
        dismiss();
    }

    private void createRemindersFromTemplate(ReminderTemplate template) {
        List<TimeSlot> times = template.getSuggestedTimes();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        for (TimeSlot timeSlot : times) {
            // Set time for today
            calendar.set(Calendar.HOUR_OF_DAY, timeSlot.getHour());
            calendar.set(Calendar.MINUTE, timeSlot.getMinute());

            // If time has passed today, set for tomorrow
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            Reminder reminder = new Reminder();
            reminder.setTitle(template.getTitle());
            reminder.setDescription(template.getDescription());
            reminder.setTimeMillis(calendar.getTimeInMillis());
            reminder.setAllDay(false);
            reminder.setCompleted(false);
            reminder.setRepeatMode(template.getRepeatMode());

            if ("CUSTOM".equals(template.getRepeatMode())) {
                reminder.setRepeatInterval(template.getRepeatInterval());
                reminder.setRepeatDays(template.getRepeatDays());
                reminder.setWindowStart(template.getWindowStart());
                reminder.setWindowEnd(template.getWindowEnd());
            }

            // Hide from widget per user request
            reminder.setHideFromWidget(true);

            repository.insert(reminder);
        }

        // Refresh widgets
        com.example.reminder.widget.StickyNoteWidgetProvider.sendRefreshBroadcast(appContext);

        // Show success message
        if (getActivity() != null) {
            String message = times.size() > 1
                    ? times.size() + " reminders created from template"
                    : "Reminder created from template";
            android.widget.Toast.makeText(getActivity(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
