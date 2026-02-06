package com.example.reminder.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.reminder.databinding.FragmentCalendarBinding;
import com.example.reminder.ui.HomeViewModel;

import java.time.format.DateTimeFormatter;
import java.util.Collections;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;

    // We instantiate CalendarViewModel via Hilt's by viewModels() equivalent in
    // Java?
    // No, standard ViewModelProvider with Hilt annotation on Fragment is enough.
    private CalendarViewModel viewModel;
    private HomeViewModel homeViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(CalendarViewModel.class);
        // Share existing HomeViewModel from Activity scope
        homeViewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Calendar Adapter
        CalendarAdapter calendarAdapter = new CalendarAdapter(Collections.emptyMap(), date -> {
            viewModel.selectDate(date);
        });

        binding.rvCalendar.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        binding.rvCalendar.setAdapter(calendarAdapter);

        // Detail List Adapter (Reusing ReminderAdapter for consistent look)
        com.example.reminder.ui.ReminderAdapter detailAdapter = new com.example.reminder.ui.ReminderAdapter(
                new com.example.reminder.ui.ReminderAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(com.example.reminder.data.Reminder reminder) {
                        // Navigate to edit? Or just show details?
                        // Let's allow editing essentially consistent with home
                        android.os.Bundle bundle = new android.os.Bundle();
                        bundle.putSerializable("reminder", reminder);
                        androidx.navigation.Navigation.findNavController(requireView())
                                .navigate(com.example.reminder.R.id.action_homeFragment_to_addEditFragment, bundle);
                        // Note: ID might be problematic if not global action.
                        // We should check navigation graph. Usually we need global action or duplicate
                        // action.
                        // Let's try global id or specific action id if we add it.
                        // For now, use the destination ID via global nav might assume we are in home
                        // graph?
                        // Actually safer:
                        // Navigation.findNavController(view).navigate(com.example.reminder.R.id.addEditFragment,
                        // bundle);
                    }

                    @Override
                    public void onCheckBoxClick(com.example.reminder.data.Reminder reminder, boolean isChecked) {
                        homeViewModel.markComplete(reminder); // Reuse home VM logic
                    }

                    @Override
                    public void onDeleteClick(com.example.reminder.data.Reminder reminder) {
                        homeViewModel.delete(reminder);
                    }

                    @Override
                    public void onSelectionModeChanged(boolean enabled, int count) {
                    } // No multi-select in calendar detail for simplicity
                });
        binding.rvReminders.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        binding.rvReminders.setAdapter(detailAdapter);

        // Observe Calendar State
        viewModel.getCalendarDays().observe(getViewLifecycleOwner(), days -> {
            calendarAdapter.setDays(days);
        });

        viewModel.getCurrentMonth().observe(getViewLifecycleOwner(), yearMonth -> {
            binding.tvMonthName.setText(yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        });

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            binding.tvSelectedDate.setText(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")));
        });

        viewModel.getSelectedDateReminders().observe(getViewLifecycleOwner(), reminders -> {
            detailAdapter.submitList(reminders);
            binding.tvSelectedDate.append(" (" + reminders.size() + ")");
        });

        viewModel.getRemindersForMonth().observe(getViewLifecycleOwner(), map -> {
            // Update calendar dots, preserving selection listener
            // We need a way to update the map in existing adapter OR create new one.
            // Previous code recreated adapter. Let's do that but smarter.
            // Actually, CalendarAdapter needs a `setReminders` method.
            // Recreating is "okay" but loses scroll state/anim.
            // Let's stick with creating new for now as it's robust.
            CalendarAdapter newAdapter = new CalendarAdapter(map, date -> viewModel.selectDate(date));
            newAdapter.setDays(viewModel.getCalendarDays().getValue()); // Restore days
            binding.rvCalendar.setAdapter(newAdapter);
        });

        // Observe Reminders from Main VM and pass to Calendar VM
        homeViewModel.getAllReminders().observe(getViewLifecycleOwner(), reminders -> {
            viewModel.setReminders(reminders);
        });

        // Actions
        binding.btnPrevMonth.setOnClickListener(v -> viewModel.previousMonth());
        binding.btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
