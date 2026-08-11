package com.example.pushupalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class AlarmScheduler {

    public static final String PREFS_NAME = "alarm_settings";

    public static void schedule(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (!prefs.getBoolean("enabled", true)) {
            return;
        }

        int hour = prefs.getInt("hour", 7);
        int minute = prefs.getInt("minute", 30);

        boolean[] selectedDays = loadSelectedDays(prefs);

        Calendar nextAlarm;

        if (hasSelectedDays(selectedDays)) {
            nextAlarm = findNextSelectedDay(hour, minute, selectedDays);
        } else {
            nextAlarm = Calendar.getInstance();
            nextAlarm.set(Calendar.HOUR_OF_DAY, hour);
            nextAlarm.set(Calendar.MINUTE, minute);
            nextAlarm.set(Calendar.SECOND, 0);
            nextAlarm.set(Calendar.MILLISECOND, 0);

            if (nextAlarm.getTimeInMillis() <= System.currentTimeMillis()) {
                nextAlarm.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        setAlarm(context, nextAlarm);
    }

    public static void scheduleNextRepeat(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (!prefs.getBoolean("enabled", true)) {
            return;
        }

        boolean[] selectedDays = loadSelectedDays(prefs);

        if (!hasSelectedDays(selectedDays)) {
            return;
        }

        int hour = prefs.getInt("hour", 7);
        int minute = prefs.getInt("minute", 30);

        Calendar nextAlarm =
                findNextSelectedDay(hour, minute, selectedDays);

        setAlarm(context, nextAlarm);
    }

    public static void cancel(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                101,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private static Calendar findNextSelectedDay(
            int hour,
            int minute,
            boolean[] selectedDays
    ) {
        Calendar now = Calendar.getInstance();

        for (int offset = 0; offset <= 7; offset++) {
            Calendar candidate = (Calendar) now.clone();
            candidate.add(Calendar.DAY_OF_MONTH, offset);

            candidate.set(Calendar.HOUR_OF_DAY, hour);
            candidate.set(Calendar.MINUTE, minute);
            candidate.set(Calendar.SECOND, 0);
            candidate.set(Calendar.MILLISECOND, 0);

            int dayIndex =
                    (candidate.get(Calendar.DAY_OF_WEEK) + 5) % 7;

            if (selectedDays[dayIndex]
                    && candidate.getTimeInMillis() > now.getTimeInMillis()) {
                return candidate;
            }
        }

        return now;
    }

    private static boolean[] loadSelectedDays(SharedPreferences prefs) {
        boolean[] days = new boolean[7];

        days[0] = prefs.getBoolean("day_0", true);
        days[1] = prefs.getBoolean("day_1", true);
        days[2] = prefs.getBoolean("day_2", false);
        days[3] = prefs.getBoolean("day_3", true);
        days[4] = prefs.getBoolean("day_4", true);
        days[5] = prefs.getBoolean("day_5", false);
        days[6] = prefs.getBoolean("day_6", false);

        return days;
    }

    private static boolean hasSelectedDays(boolean[] days) {
        for (boolean day : days) {
            if (day) {
                return true;
            }
        }

        return false;
    }

    private static void setAlarm(Context context, Calendar calendar) {
        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                101,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }
}