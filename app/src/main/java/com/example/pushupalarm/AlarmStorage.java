package com.example.pushupalarm;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class AlarmStorage {

    private static final String PREFS_NAME = "alarm_settings";
    private static final String KEY_ALARMS = "alarms_data";
    private static final String KEY_NEXT_ID = "next_alarm_id";

    public static ArrayList<AlarmItem> load(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        // Prima rulare dupa update:
        // mutam alarma veche in noua lista.
        if (!prefs.contains(KEY_ALARMS)) {

            ArrayList<AlarmItem> migrated = new ArrayList<>();

            boolean[] oldDays = new boolean[7];

            oldDays[0] = prefs.getBoolean("day_0", true);
            oldDays[1] = prefs.getBoolean("day_1", true);
            oldDays[2] = prefs.getBoolean("day_2", false);
            oldDays[3] = prefs.getBoolean("day_3", true);
            oldDays[4] = prefs.getBoolean("day_4", true);
            oldDays[5] = prefs.getBoolean("day_5", false);
            oldDays[6] = prefs.getBoolean("day_6", false);

            AlarmItem oldAlarm = new AlarmItem(
                    1000,
                    prefs.getInt("hour", 7),
                    prefs.getInt("minute", 30),
                    prefs.getBoolean("enabled", true),
                    oldDays
            );

            migrated.add(oldAlarm);

            prefs.edit()
                    .putInt(KEY_NEXT_ID, 1001)
                    .apply();

            save(context, migrated);

            return migrated;
        }

        ArrayList<AlarmItem> alarms = new ArrayList<>();

        String data = prefs.getString(KEY_ALARMS, "");

        if (data == null || data.trim().isEmpty()) {
            return alarms;
        }

        String[] alarmStrings = data.split(";");

        for (String alarmString : alarmStrings) {

            try {
                String[] parts = alarmString.split(",");

                int id = Integer.parseInt(parts[0]);
                int hour = Integer.parseInt(parts[1]);
                int minute = Integer.parseInt(parts[2]);
                boolean enabled = Boolean.parseBoolean(parts[3]);
                int dayMask = Integer.parseInt(parts[4]);

                boolean[] days = new boolean[7];

                for (int i = 0; i < 7; i++) {
                    days[i] = (dayMask & (1 << i)) != 0;
                }

                alarms.add(
                        new AlarmItem(
                                id,
                                hour,
                                minute,
                                enabled,
                                days
                        )
                );

            } catch (Exception ignored) {
            }
        }

        return alarms;
    }

    public static void save(
            Context context,
            ArrayList<AlarmItem> alarms
    ) {

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < alarms.size(); i++) {

            AlarmItem alarm = alarms.get(i);

            int dayMask = 0;

            for (int d = 0; d < 7; d++) {
                if (alarm.days[d]) {
                    dayMask |= (1 << d);
                }
            }

            builder.append(alarm.id)
                    .append(",")
                    .append(alarm.hour)
                    .append(",")
                    .append(alarm.minute)
                    .append(",")
                    .append(alarm.enabled)
                    .append(",")
                    .append(dayMask);

            if (i < alarms.size() - 1) {
                builder.append(";");
            }
        }

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        prefs.edit()
                .putString(KEY_ALARMS, builder.toString())
                .apply();
    }

    public static int getNextId(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        int nextId = prefs.getInt(KEY_NEXT_ID, 1000);

        prefs.edit()
                .putInt(KEY_NEXT_ID, nextId + 1)
                .apply();

        return nextId;
    }

    public static AlarmItem findById(
            Context context,
            int id
    ) {

        ArrayList<AlarmItem> alarms = load(context);

        for (AlarmItem alarm : alarms) {
            if (alarm.id == id) {
                return alarm;
            }
        }

        return null;
    }

    public static void update(
            Context context,
            AlarmItem updatedAlarm
    ) {

        ArrayList<AlarmItem> alarms = load(context);

        for (int i = 0; i < alarms.size(); i++) {

            if (alarms.get(i).id == updatedAlarm.id) {
                alarms.set(i, updatedAlarm);
                save(context, alarms);
                return;
            }
        }
    }
}