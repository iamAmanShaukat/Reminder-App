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
            } else if (item.getItemId() == R.id.action_calendar) {
                Navigation.findNavController(requireView()).navigate(R.id.calendarFragment);
                return true;
            } else if (item.getItemId() == R.id.action_more_custom) {
                View menuItemView = requireActivity().findViewById(R.id.action_more_custom);
                showCustomMenu(menuItemView != null ? menuItemView : binding.toolbar);
                return true;
            }
            return false;
        });

        setupRecyclerView();
        setupSpeedDialFab();

        // Observe reminders
        viewModel.getAllReminders().observe(getViewLifecycleOwner(), reminders -> {
            adapter.submitList(reminders);
            if (reminders.isEmpty()) {
                binding.recyclerView.setVisibility(android.view.View.GONE);
            } else {
                binding.recyclerView.setVisibility(android.view.View.VISIBLE);
            }
        });

        checkFirstRun();
    }

    private void checkFirstRun() {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("com.example.reminder",
                android.content.Context.MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        if (isFirstRun) {
            // Show custom onboarding card with animation
            android.view.View card = binding.getRoot().findViewById(R.id.cardOnboarding);
            android.view.View scrim = binding.getRoot().findViewById(R.id.onboardingScrim);

            if (card != null && scrim != null) {
                card.setTranslationY(100f);
                card.setVisibility(android.view.View.VISIBLE);
                scrim.setVisibility(android.view.View.VISIBLE);

                card.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(500)
                        .setStartDelay(500) // Wait for app load
                        .start();

                // Define dismiss action
                Runnable dismissAction = () -> {
                    card.animate()
                            .alpha(0f)
                            .translationY(100f)
                            .setDuration(300)
                            .withEndAction(() -> {
                                card.setVisibility(android.view.View.GONE);
                                scrim.setVisibility(android.view.View.GONE);
                            })
                            .start();
                    prefs.edit().putBoolean("isFirstRun", false).apply();
                };

                // Setup Click Listeners
                card.findViewById(R.id.btnViewHelp).setOnClickListener(v -> {
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_helpFragment);
                    // Dismiss card after navigating
                    prefs.edit().putBoolean("isFirstRun", false).apply();
                    card.setVisibility(android.view.View.GONE);
                    scrim.setVisibility(android.view.View.GONE);
                });

                card.findViewById(R.id.btnDismissOnboarding).setOnClickListener(v -> dismissAction.run());

                // Dismiss when touching outside
                scrim.setOnClickListener(v -> dismissAction.run());
            }
        }
    }

    private void setupSpeedDialFab() {
        final boolean[] isFabOpen = { false };

        // Main FAB click - toggle speed dial
        binding.fabAdd.setOnClickListener(v -> {
            if (isFabOpen[0]) {
                collapseFab();
            } else {
                expandFab();
            }
            isFabOpen[0] = !isFabOpen[0];
        });

        // Scrim click - collapse speed dial
        binding.fabScrim.setOnClickListener(v -> {
            collapseFab();
            isFabOpen[0] = false;
        });

        // Templates mini FAB
        binding.fabTemplates.setOnClickListener(v -> {
            showTemplatesDialog();
            collapseFab();
            isFabOpen[0] = false;
        });

        // Custom reminder mini FAB
        binding.fabCustom.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.addEditFragment);
            collapseFab();
            isFabOpen[0] = false;
        });
    }

    private void expandFab() {
        // Show scrim
        binding.fabScrim.setVisibility(android.view.View.VISIBLE);
        binding.fabScrim.setAlpha(0f);
        binding.fabScrim.animate().alpha(1f).setDuration(200).start();

        // Rotate main FAB
        binding.fabAdd.animate().rotation(45f).setDuration(200).start();

        // Show and animate mini FABs
        binding.fabTemplatesLayout.setVisibility(android.view.View.VISIBLE);
        binding.fabTemplatesLayout.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(200)
                .setStartDelay(50)
                .start();

        binding.fabCustomLayout.setVisibility(android.view.View.VISIBLE);
        binding.fabCustomLayout.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(200)
                .setStartDelay(100)
                .start();
    }

    private void collapseFab() {
        if (binding == null)
            return;

        // Hide scrim
        binding.fabScrim.animate().alpha(0f).setDuration(200)
                .withEndAction(() -> {
                    if (binding != null)
                        binding.fabScrim.setVisibility(android.view.View.GONE);
                })
                .start();

        // Rotate main FAB back
        binding.fabAdd.animate().rotation(0f).setDuration(200).start();

        // Hide mini FABs
        binding.fabTemplatesLayout.animate()
                .alpha(0f)
                .translationY(20f)
                .setDuration(150)
                .withEndAction(() -> {
                    if (binding != null)
                        binding.fabTemplatesLayout.setVisibility(android.view.View.GONE);
                })
                .start();

        binding.fabCustomLayout.animate()
                .alpha(0f)
                .translationY(20f)
                .setDuration(150)
                .withEndAction(() -> {
                    if (binding != null)
                        binding.fabCustomLayout.setVisibility(android.view.View.GONE);
                })
                .start();
    }

    private void setupRecyclerView() {
        adapter = new ReminderAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        androidx.recyclerview.widget.ItemTouchHelper itemTouchHelper = new androidx.recyclerview.widget.ItemTouchHelper(
                new SwipeCallback(requireContext()) {
                    @Override
                    public void onSwiped(@NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder,
                            int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Reminder reminder = adapter.getCurrentList().get(position);

                        if (direction == androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
                            // Delete
                            viewModel.delete(reminder);
                            com.google.android.material.snackbar.Snackbar
                                    .make(binding.getRoot(), "Reminder deleted",
                                            com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                                    .setAction("Undo", v -> viewModel.insert(reminder))
                                    // can restore or we
                                    // accept simple delete
                                    // for now.
                                    // Actually HomeViewModel doesn't expose save, it exposes delete.
                                    // Let's just use delete for now, implementing undo properly requires insert in
                                    // VM.
                                    // Ideally we should ask for confirmation or allow undo.
                                    .show();

                        } else if (direction == androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
                            // Complete
                            viewModel.updateCompletionStatus(reminder, !reminder.isCompleted());
                            // Refresh to show update (or relies on LiveData)
                            // If we toggle, and it's already completed, it becomes incomplete. Logic is
                            // fine.
                            // But typically swipe right is "Mark Complete".
                            if (!reminder.isCompleted()) {
                                // Only show snackbar if we actually completed it (positive action)
                                com.google.android.material.snackbar.Snackbar
                                        .make(binding.getRoot(), "Reminder completed",
                                                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                            // Reset the swipe state so the item snaps back
                            adapter.notifyItemChanged(position);
                        }
                    }
                });
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
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
        viewModel.updateCompletionStatus(reminder, isChecked);
    }

    @Override
    public void onDeleteClick(Reminder reminder) {
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_delete_confirmation, null);

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        // Make dialog background transparent so our custom background shows
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            viewModel.delete(reminder);
            dialog.dismiss();
        });

        dialog.show();
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
                int count = adapter.getSelectedItems().size();

                android.view.LayoutInflater inflater = getLayoutInflater();
                android.view.View dialogView = inflater.inflate(R.layout.dialog_delete_confirmation, null);

                // Update title for bulk delete
                android.widget.TextView titleView = dialogView.findViewById(R.id.dialog_title);
                titleView.setText("Delete " + count + (count == 1 ? " Reminder?" : " Reminders?"));

                android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                        .setView(dialogView).create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }

                dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
                dialogView.findViewById(R.id.btn_delete).setOnClickListener(v -> {
                    viewModel.delete(adapter.getSelectedItems());
                    mode.finish();
                    dialog.dismiss();
                });

                dialog.show();
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

    // Custom Popup Menu for "More" options
    private void showCustomMenu(View anchor) {
        android.view.LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.popup_menu_home, null);

        // Create PopupWindow
        final android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(
                popupView,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // Set elevation
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(10);
        }

        // Background for outside touch dismissal (needed for older APIs or consistency)
        popupWindow
                .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        // Setup Item Clicks
        popupView.findViewById(R.id.menu_add_widget).setOnClickListener(v -> {
            requestPinWidget();
            popupWindow.dismiss();
        });

        // Show
        popupWindow.showAsDropDown(anchor, 0, -20); // Offset slightly
    }

    private void requestPinWidget() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.appwidget.AppWidgetManager appWidgetManager = requireContext()
                    .getSystemService(android.appwidget.AppWidgetManager.class);
            android.content.ComponentName myProvider = new android.content.ComponentName(requireContext(),
                    com.example.reminder.widget.StickyNoteWidgetProvider.class);

            if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                // strict check: if widget already exists, do not ask
                int[] existingIds = appWidgetManager.getAppWidgetIds(myProvider);
                if (existingIds != null && existingIds.length > 0) {
                    com.google.android.material.snackbar.Snackbar.make(binding.getRoot(),
                            "Widget is already added to your home screen",
                            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                    return;
                }

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

    private void showTemplatesDialog() {
        new com.example.reminder.ui.templates.TemplateDialogFragment()
                .show(getParentFragmentManager(), "TEMPLATES");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
