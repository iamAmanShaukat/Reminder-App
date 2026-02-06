package com.example.reminder.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.reminder.R;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[] { android.Manifest.permission.POST_NOTIFICATIONS }, 101);
            }
        }

        // Request Exact Alarm permission if needed (Android 12+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(
                    android.content.Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                android.content.Intent intent = new android.content.Intent(
                        android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
        // Check for navigation intent
        if (getIntent().getBooleanExtra("NAVIGATE_TO_ADD", false)) {
            // Need to wait for NavHost/Graph to be ready or handle in onResume if needed
            // For simplicity, we can do it after layout binding
            // Note: In real app, might need to ensure FragmentContainer is ready
            // Using a post helper or just verifying controller

            // Since we use parsing in create, we might need to find controller dynamically
            // Ideally we'd use viewBinding but here using standard

            findViewById(android.R.id.content).post(() -> {
                try {
                    androidx.navigation.NavController navController = androidx.navigation.Navigation
                            .findNavController(this, R.id.nav_host_fragment);
                    navController.navigate(R.id.action_homeFragment_to_addEditFragment);
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Error navigating to add", e);
                }
            });
        }
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.getBooleanExtra("NAVIGATE_TO_ADD", false)) {
            try {
                androidx.navigation.NavController navController = androidx.navigation.Navigation.findNavController(this,
                        R.id.nav_host_fragment);
                navController.navigate(R.id.action_homeFragment_to_addEditFragment);
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error navigating to add from newIntent", e);
            }
        }
    }
}
