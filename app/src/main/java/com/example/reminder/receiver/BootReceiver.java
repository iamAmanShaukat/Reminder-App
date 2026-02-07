package com.example.reminder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.reminder.data.ReminderRepository;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class BootReceiver extends BroadcastReceiver {

    @Inject
    ReminderRepository repository;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            new Thread(() -> {
                // TODO: Query active reminders and reschedule them
            }).start();
        }
    }
}
