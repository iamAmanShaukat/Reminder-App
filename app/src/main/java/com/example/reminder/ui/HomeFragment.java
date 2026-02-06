package com.example.reminder.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.reminder.R;
import com.example.reminder.data.Reminder;
import com.example.reminder.databinding.FragmentHomeBinding;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment implements ReminderAdapter.OnItemClickListener {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private ReminderAdapter adapter;
    private android.view.ActionMode actionMode;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true); // Enable Menu
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Setup Toolbar
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                Navigation.findNavController(requireView()).navigate(R.id.settingsFragment);
                return true;
            } else if (item.getItemId() == R.id.action_add_widget) {
                requestPinWidget();
                return true;
            } else if (item.getItemId() == R.id.action_calendar) {
                Navigation.findNavController(requireView()).navigate(R.id.calendarFragment);
                return true;
            }
            return false;
        });

        setupRecyclerView();

        viewModel.getAllReminders().observe(getViewLifecycleOwner(), reminders -> {
            adapter.submitList(reminders);
        });

        binding.fabAdd.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_homeFragment_to_addEditFragment);
        });
    }

    private void setupRecyclerView() {
        adapter = new ReminderAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(Reminder reminder) {
        if (actionMode != null)
            return;
        Bundle bundle = new Bundle();
        bundle.putSerializable("reminder", reminder);
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_addEditFragment, bundle);
    }

    @Override
    public void onCheckBoxClick(Reminder reminder, boolean isChecked) {
        if (isChecked) {
            viewModel.markComplete(reminder);
        }
    }

    @Override
    public void onDeleteClick(Reminder reminder) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(),
                R.style.ThemeOverlay_App_MaterialAlertDialog_Destructive)
                .setTitle("Delete Reminder")
                .setMessage("Are you sure you want to delete this reminder?")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.delete(reminder))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onSelectionModeChanged(boolean enabled, int selectedCount) {
        if (enabled) {
            if (actionMode == null) {
                actionMode = requireActivity().startActionMode(actionModeCallback);
            }
            if (actionMode != null) {
                actionMode.setTitle(selectedCount + " selected");
            }
        } else {
            if (actionMode != null) {
                actionMode.finish();
            }
        }
    }

    private final android.view.ActionMode.Callback actionModeCallback = new android.view.ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, android.view.Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, android.view.Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, android.view.MenuItem item) {
            if (item.getItemId() == R.id.action_delete_selected) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(),
                        R.style.ThemeOverlay_App_MaterialAlertDialog_Destructive)
                        .setTitle("Delete " + adapter.getSelectedItems().size() + " Reminders?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            viewModel.delete(adapter.getSelectedItems());
                            mode.finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {
            actionMode = null;
            adapter.clearSelection();
        }

    };

    private void requestPinWidget() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.appwidget.AppWidgetManager appWidgetManager = requireContext()
                    .getSystemService(android.appwidget.AppWidgetManager.class);
            android.content.ComponentName myProvider = new android.content.ComponentName(requireContext(),
                    com.example.reminder.widget.StickyNoteWidgetProvider.class);

            if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                android.content.Intent pinnedWidgetCallbackIntent = new android.content.Intent(requireContext(),
                        com.example.reminder.receiver.AlarmReceiver.class); // Using AlarmReceiver as dummy callback or
                                                                            // null
                android.app.PendingIntent successCallback = android.app.PendingIntent.getBroadcast(requireContext(), 0,
                        pinnedWidgetCallbackIntent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

                appWidgetManager.requestPinAppWidget(myProvider, null, successCallback);
            } else {
                com.google.android.material.snackbar.Snackbar
                        .make(binding.getRoot(), "Widget pinning not supported on this device/launcher",
                                com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                        .show();
            }
        } else {
            com.google.android.material.snackbar.Snackbar.make(binding.getRoot(),
                    "Widget pinning requires Android 8.0+", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
