package com.example.pushupalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView mainClockText;
    private TextView firstAlarmTime;
    private TextView firstAlarmAmPm;
    private TextView nextAlarmPill;

    private FrameLayout firstToggle;
    private View firstToggleDot;

    private boolean firstAlarmEnabled = true;

    private int alarmHour = 7;
    private int alarmMinute = 30;

    private TextView[] dayButtons;

    private final boolean[] selectedDays = {
            true, true, false, true, true, false, false
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.WHITE);
        window.setNavigationBarColor(Color.WHITE);

        setContentView(R.layout.activity_main);
        loadAlarmSettings();

        mainClockText = findViewById(R.id.mainClockText);
        firstAlarmTime = findViewById(R.id.firstAlarmTime);
        firstAlarmAmPm = findViewById(R.id.firstAlarmAmPm);
        nextAlarmPill = findViewById(R.id.nextAlarmPill);

        firstToggle = findViewById(R.id.firstToggle);
        firstToggleDot = findViewById(R.id.firstToggleDot);

        dayButtons = new TextView[]{
                findViewById(R.id.dayL),
                findViewById(R.id.dayMa),
                findViewById(R.id.dayMi),
                findViewById(R.id.dayJ),
                findViewById(R.id.dayV),
                findViewById(R.id.dayS),
                findViewById(R.id.dayD)
        };

        findViewById(R.id.addAlarmButton).setOnClickListener(v -> openTimePicker());
        mainClockText.setOnClickListener(v -> openTimePicker());
        firstAlarmTime.setOnClickListener(v -> openTimePicker());

        firstToggle.setOnClickListener(v -> {
            firstAlarmEnabled = !firstAlarmEnabled;

            saveAlarmSettings();
            updateToggle();

            if (firstAlarmEnabled) {
                scheduleAlarm();
            } else {
                cancelAlarm();
            }
        });

        for (int i = 0; i < dayButtons.length; i++) {
            final int index = i;

            dayButtons[i].setOnClickListener(v -> {
                selectedDays[index] = !selectedDays[index];

                saveAlarmSettings();
                updateDayButtons();

                if (firstAlarmEnabled) {
                    scheduleAlarm();
                }
            });
        }

        updateAlarmTime();
        updateToggle();
        updateDayButtons();
    }

    private void openTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    alarmHour = hourOfDay;
                    alarmMinute = minute;

                    firstAlarmEnabled = true;

                    saveAlarmSettings();

                    updateAlarmTime();
                    updateToggle();
                    scheduleAlarm();
                },
                alarmHour,
                alarmMinute,
                false
        );

        dialog.show();
    }

    private void updateAlarmTime() {
        int displayHour = alarmHour % 12;

        if (displayHour == 0) {
            displayHour = 12;
        }

        String amPm = alarmHour >= 12 ? "PM" : "AM";

        String time = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                displayHour,
                alarmMinute
        );

        mainClockText.setText(time);
        firstAlarmTime.setText(time);
        firstAlarmAmPm.setText(amPm);

        if (firstAlarmEnabled) {
            nextAlarmPill.setText("activă");
        } else {
            nextAlarmPill.setText("oprită");
        }
    }

    private void updateToggle() {
        if (firstAlarmEnabled) {
            firstToggle.setBackgroundResource(R.drawable.bg_toggle_on);
            firstToggleDot.setBackgroundResource(R.drawable.bg_toggle_dot_white);

            FrameLayout.LayoutParams params =
                    new FrameLayout.LayoutParams(dp(20), dp(20));
            params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
            params.setMargins(0, 0, dp(3), 0);
            firstToggleDot.setLayoutParams(params);

            firstAlarmTime.setTextColor(Color.parseColor("#111111"));
            firstAlarmAmPm.setTextColor(Color.parseColor("#BBBBBB"));
            nextAlarmPill.setText("activă");

        } else {
            firstToggle.setBackgroundResource(R.drawable.bg_toggle_off);
            firstToggleDot.setBackgroundResource(R.drawable.bg_toggle_dot_gray);

            FrameLayout.LayoutParams params =
                    new FrameLayout.LayoutParams(dp(20), dp(20));
            params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
            params.setMargins(dp(3), 0, 0, 0);
            firstToggleDot.setLayoutParams(params);

            firstAlarmTime.setTextColor(Color.parseColor("#CFCFCF"));
            firstAlarmAmPm.setTextColor(Color.parseColor("#DADADA"));
            nextAlarmPill.setText("oprită");
        }
    }

    private void updateDayButtons() {
        for (int i = 0; i < dayButtons.length; i++) {
            if (selectedDays[i]) {
                dayButtons[i].setBackgroundResource(R.drawable.bg_day_on);
                dayButtons[i].setTextColor(Color.WHITE);
            } else {
                dayButtons[i].setBackgroundResource(R.drawable.bg_day_off);
                dayButtons[i].setTextColor(Color.parseColor("#CCCCCC"));
            }
        }
    }

    private void scheduleAlarm() {
        AlarmScheduler.schedule(this);

        Toast.makeText(
                this,
                "Alarma a fost setata",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void cancelAlarm() {
        AlarmScheduler.cancel(this);

        Toast.makeText(
                this,
                "Alarma a fost oprita",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void saveAlarmSettings() {
        SharedPreferences prefs =
                getSharedPreferences(
                        AlarmScheduler.PREFS_NAME,
                        MODE_PRIVATE
                );

        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("enabled", firstAlarmEnabled);
        editor.putInt("hour", alarmHour);
        editor.putInt("minute", alarmMinute);

        for (int i = 0; i < selectedDays.length; i++) {
            editor.putBoolean("day_" + i, selectedDays[i]);
        }

        editor.apply();
    }

    private void loadAlarmSettings() {
        SharedPreferences prefs =
                getSharedPreferences(
                        AlarmScheduler.PREFS_NAME,
                        MODE_PRIVATE
                );

        firstAlarmEnabled =
                prefs.getBoolean("enabled", true);

        alarmHour =
                prefs.getInt("hour", 7);

        alarmMinute =
                prefs.getInt("minute", 30);

        for (int i = 0; i < selectedDays.length; i++) {
            selectedDays[i] =
                    prefs.getBoolean(
                            "day_" + i,
                            selectedDays[i]
                    );
        }
    }
    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}