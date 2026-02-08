package com.example.reminder.utils;

import android.app.AlarmManager;
import com.example.reminder.data.Reminder;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class SmartCompletionUtils {

    public static long getNextReminderTime(Reminder reminder) {
        long nextTime = 0;
        String repeatMode = reminder.getRepeatMode();

        if ("CUSTOM".equals(repeatMode)) {
            nextTime = calculateNextCustomTime(reminder);
        } else {
            long interval = 0;
            switch (repeatMode) {
                case "HOURLY":
                    interval = AlarmManager.INTERVAL_HOUR;
                    break;
                case "DAILY":
                    interval = AlarmManager.INTERVAL_DAY;
                    break;
                case "WEEKLY":
                    interval = AlarmManager.INTERVAL_DAY * 7;
                    break;
                case "MONTHLY":
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(reminder.getTimeMillis());
                    cal.add(Calendar.MONTH, 1);
                    interval = cal.getTimeInMillis() - reminder.getTimeMillis();
                    break;
            }
            if (interval > 0) {
                nextTime = reminder.getTimeMillis() + interval;
            }
        }
        return nextTime;
    }

    private static long calculateNextCustomTime(Reminder reminder) {
        long lastTime = reminder.getTimeMillis();
        long interval = reminder.getRepeatInterval();

        if (interval <= 0)
            return lastTime + AlarmManager.INTERVAL_DAY;

        long next = lastTime + interval;

        String daysStr = reminder.getRepeatDays();
        Integer winStart = reminder.getWindowStart();
        Integer winEnd = reminder.getWindowEnd();

        if (daysStr == null || daysStr.isEmpty())
            return next;

        Set<Integer> validDays = new HashSet<>();
        try {
            for (String d : daysStr.split(",")) {
                validDays.add(Integer.parseInt(d));
            }
        } catch (NumberFormatException e) {
            return next;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(next);

        int safeguards = 0;
        while (safeguards < 365) {
            int currentDay = cal.get(Calendar.DAY_OF_WEEK);
            int currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

            if (validDays.contains(currentDay)) {
                boolean windowValid = true;
                if (winStart != null && winEnd != null) {
                    if (currentMinutes < winStart) {
                        cal.set(Calendar.HOUR_OF_DAY, winStart / 60);
                        cal.set(Calendar.MINUTE, winStart % 60);
                        cal.set(Calendar.SECOND, 0);
                        return cal.getTimeInMillis();
                    } else if (currentMinutes > winEnd) {
                        windowValid = false;
                    }
                }
                if (windowValid) {
                    return cal.getTimeInMillis();
                }
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
            if (winStart != null) {
                cal.set(Calendar.HOUR_OF_DAY, winStart / 60);
                cal.set(Calendar.MINUTE, winStart % 60);
                cal.set(Calendar.SECOND, 0);
            }
            safeguards++;
        }

        return next;
    }
}
