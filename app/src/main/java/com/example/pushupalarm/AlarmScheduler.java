package com.example.pushupalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;

public class AlarmScheduler {

    public static void schedule(
            Context context,
            AlarmItem alarm
    ) {

        if (!alarm.enabled) {
            return;
        }

        long triggerTime = getNextTriggerMillis(alarm);

        Intent intent =
                new Intent(context, AlarmReceiver.class);

        intent.putExtra("alarm_id", alarm.id);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        alarm.id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
        );
    }

    public static void cancel(
            Context context,
            int alarmId
    ) {

        Intent intent =
                new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        alarmId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    public static void scheduleAll(Context context) {

        cancelLegacyAlarm(context);

        ArrayList<AlarmItem> alarms =
                AlarmStorage.load(context);

        for (AlarmItem alarm : alarms) {
            if (alarm.enabled) {
                schedule(context, alarm);
            }
        }
    }

    public static void scheduleNextRepeat(
            Context context,
            int alarmId
    ) {

        AlarmItem alarm =
                AlarmStorage.findById(context, alarmId);

        if (alarm == null || !alarm.enabled) {
            return;
        }

        if (!hasSelectedDays(alarm.days)) {

            // Alarma fara zile selectate este one-time.
            alarm.enabled = false;
            AlarmStorage.update(context, alarm);

            return;
        }

        schedule(context, alarm);
    }

    public static long getNextTriggerMillis(
            AlarmItem alarm
    ) {

        Calendar now = Calendar.getInstance();

        if (hasSelectedDays(alarm.days)) {

            for (int offset = 0; offset <= 7; offset++) {

                Calendar candidate =
                        (Calendar) now.clone();

                candidate.add(
                        Calendar.DAY_OF_MONTH,
                        offset
                );

                candidate.set(
                        Calendar.HOUR_OF_DAY,
                        alarm.hour
                );

                candidate.set(
                        Calendar.MINUTE,
                        alarm.minute
                );

                candidate.set(
                        Calendar.SECOND,
                        0
                );

                candidate.set(
                        Calendar.MILLISECOND,
                        0
                );

                int dayIndex =
                        (candidate.get(Calendar.DAY_OF_WEEK) + 5) % 7;

                if (
                        alarm.days[dayIndex]
                                &&
                                candidate.getTimeInMillis()
                                        > now.getTimeInMillis()
                ) {
                    return candidate.getTimeInMillis();
                }
            }
        }

        Calendar candidate =
                Calendar.getInstance();

        candidate.set(
                Calendar.HOUR_OF_DAY,
                alarm.hour
        );

        candidate.set(
                Calendar.MINUTE,
                alarm.minute
        );

        candidate.set(
                Calendar.SECOND,
                0
        );

        candidate.set(
                Calendar.MILLISECOND,
                0
        );

        if (
                candidate.getTimeInMillis()
                        <= System.currentTimeMillis()
        ) {
            candidate.add(
                    Calendar.DAY_OF_MONTH,
                    1
            );
        }

        return candidate.getTimeInMillis();
    }

    private static boolean hasSelectedDays(
            boolean[] days
    ) {

        for (boolean day : days) {
            if (day) {
                return true;
            }
        }

        return false;
    }

    private static void cancelLegacyAlarm(
            Context context
    ) {

        Intent intent =
                new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        101,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}