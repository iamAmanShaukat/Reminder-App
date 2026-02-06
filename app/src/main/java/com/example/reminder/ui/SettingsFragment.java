package com.example.reminder.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.example.reminder.databinding.FragmentSettingsBinding;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.ArrayList;
import java.util.List;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SharedPreferences prefs;

    public static final String KEY_SNOOZE_DURATION = "snooze_duration";
    public static final String KEY_RINGTONE_URI = "ringtone_uri";
    public static final String KEY_RINGTONE_NAME = "ringtone_name";

    private final ActivityResultLauncher<Intent> ringtonePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (uri != null) {
                        saveRingtone(uri);
                    } else {
                        // User chose "Silent" or "Default" (null often implies silent in picker logic,
                        // checking intent)
                        saveRingtone(null);
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        setupSnoozeSpinner();
        setupRingtonePicker();
        setupBackupUI();
    }

    @javax.inject.Inject
    com.example.reminder.utils.GoogleSignInHelper googleSignInHelper;

    @javax.inject.Inject
    com.example.reminder.data.BackupRepository backupRepository;

    private void setupBackupUI() {
        updateBackupUIState();

        binding.btnBackupToggle.setOnClickListener(v -> {
            if (googleSignInHelper.isSignedIn(requireContext())) {
                // Sign out
                googleSignInHelper.signOut(task -> updateBackupUIState());
            } else {
                // Sign in
                signInLauncher.launch(googleSignInHelper.getSignInIntent());
            }
        });

        binding.btnBackupNow.setOnClickListener(v -> {
            com.google.android.gms.auth.api.signin.GoogleSignInAccount account = googleSignInHelper
                    .getLastSignedInAccount(requireContext());
            if (account == null)
                return;

            android.widget.Toast.makeText(requireContext(), "Starting Backup...", android.widget.Toast.LENGTH_SHORT)
                    .show();

            // Generate JSON on BG thread (Repo handles it but exportRemindersToJson uses
            // DAO which might need BG)
            // Ideally Repo should handle threading completely. My Repo export
            // implementation calls DAO sync, so needed background.
            new Thread(() -> {
                String json = backupRepository.exportRemindersToJson();
                if (json != null) {
                    backupRepository.uploadToDrive(requireContext(), account, json,
                            new com.example.reminder.data.BackupRepository.BackupCallback() {
                                @Override
                                public void onSuccess() {
                                    requireActivity()
                                            .runOnUiThread(() -> android.widget.Toast.makeText(requireContext(),
                                                    "Backup Successful! ✅", android.widget.Toast.LENGTH_LONG).show());
                                }

                                @Override
                                public void onError(Exception e) {
                                    requireActivity().runOnUiThread(() -> android.widget.Toast
                                            .makeText(requireContext(), "Backup Failed: " + e.getMessage(),
                                                    android.widget.Toast.LENGTH_LONG)
                                            .show());
                                    e.printStackTrace();
                                }
                            });
                } else {
                    requireActivity().runOnUiThread(() -> android.widget.Toast
                            .makeText(requireContext(), "Failed to export data", android.widget.Toast.LENGTH_SHORT)
                            .show());
                }
            }).start();
        });

        binding.btnRestoreNow.setOnClickListener(v -> {
            com.google.android.gms.auth.api.signin.GoogleSignInAccount account = googleSignInHelper
                    .getLastSignedInAccount(requireContext());
            if (account == null)
                return;

            android.widget.Toast.makeText(requireContext(), "Starting Restore...", android.widget.Toast.LENGTH_SHORT)
                    .show();

            backupRepository.restoreFromDrive(requireContext(), account,
                    new com.example.reminder.data.BackupRepository.BackupCallback() {
                        @Override
                        public void onSuccess() {
                            requireActivity().runOnUiThread(() -> android.widget.Toast.makeText(requireContext(),
                                    "Restore Successful! 🔄", android.widget.Toast.LENGTH_LONG).show());
                        }

                        @Override
                        public void onError(Exception e) {
                            requireActivity().runOnUiThread(() -> android.widget.Toast.makeText(requireContext(),
                                    "Restore Failed: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show());
                            e.printStackTrace();
                        }
                    });
        });
    }

    private void updateBackupUIState() {
        boolean isSignedIn = googleSignInHelper.isSignedIn(requireContext());
        if (isSignedIn) {
            com.google.android.gms.auth.api.signin.GoogleSignInAccount account = googleSignInHelper
                    .getLastSignedInAccount(requireContext());
            binding.tvBackupStatus.setText("Auto Backup Enabled (Managed by Android)");
            binding.btnBackupToggle.setVisibility(View.GONE);
            binding.btnBackupNow.setVisibility(View.GONE);
            binding.btnRestoreNow.setVisibility(View.GONE);
        } else {
            binding.tvBackupStatus.setText("Auto Backup Enabled (Managed by Android)");
            binding.btnBackupToggle.setVisibility(View.GONE);
            binding.btnBackupNow.setVisibility(View.GONE);
            binding.btnRestoreNow.setVisibility(View.GONE);
        }
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Successfully signed in
                    updateBackupUIState();
                } else {
                    android.widget.Toast.makeText(requireContext(), "Sign in failed", android.widget.Toast.LENGTH_SHORT)
                            .show();
                }
            });

    private void setupSnoozeSpinner() {
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        labels.add("5 Minutes");
        values.add(5);
        labels.add("10 Minutes");
        values.add(10);
        labels.add("15 Minutes");
        values.add(15);
        labels.add("30 Minutes");
        values.add(30);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSnooze.setAdapter(adapter);

        // Load saved value
        int savedValue = prefs.getInt(KEY_SNOOZE_DURATION, 10);
        int position = values.indexOf(savedValue);
        if (position >= 0) {
            binding.spinnerSnooze.setSelection(position);
        }

        binding.spinnerSnooze.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt(KEY_SNOOZE_DURATION, values.get(position)).apply();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupRingtonePicker() {
        String savedName = prefs.getString(KEY_RINGTONE_NAME, "Default");
        binding.tvSoundName.setText(savedName);

        binding.containerSound.setOnClickListener(v -> {
            String currentUriString = prefs.getString(KEY_RINGTONE_URI, null);
            Uri currentUri = currentUriString != null ? Uri.parse(currentUriString)
                    : android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;

            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Reminder Sound");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);

            ringtonePickerLauncher.launch(intent);
        });
    }

    private void saveRingtone(@Nullable Uri uri) {
        String uriString = uri != null ? uri.toString() : null;
        String name = "Default";

        if (uri != null) {
            android.media.Ringtone ringtone = RingtoneManager.getRingtone(requireContext(), uri);
            if (ringtone != null) {
                name = ringtone.getTitle(requireContext());
            }
        }

        prefs.edit()
                .putString(KEY_RINGTONE_URI, uriString)
                .putString(KEY_RINGTONE_NAME, name)
                .apply();

        binding.tvSoundName.setText(name);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
