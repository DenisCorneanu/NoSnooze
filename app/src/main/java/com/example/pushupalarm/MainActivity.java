package com.example.pushupalarm;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView dateText;
    private TextView mainClockText;
    private TextView nextAlarmPill;

    private ScrollView alarmListScroll;
    private LinearLayout alarmListContainer;

    private ArrayList<AlarmItem> alarms;

    private final String[] dayLabels = {
            "L", "M", "Mi", "J", "V", "S", "D"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        window.setStatusBarColor(Color.WHITE);
        window.setNavigationBarColor(Color.WHITE);

        setContentView(R.layout.activity_main);

        dateText =
                findViewById(R.id.dateText);

        mainClockText =
                findViewById(R.id.mainClockText);

        nextAlarmPill =
                findViewById(R.id.nextAlarmPill);

        alarmListScroll =
                findViewById(R.id.alarmListScroll);

        alarmListContainer =
                findViewById(R.id.alarmListContainer);

        findViewById(R.id.addAlarmButton)
                .setOnClickListener(
                        v -> openAddAlarmPicker()
                );

        updateDate();

        alarms = AlarmStorage.load(this);

        AlarmScheduler.scheduleAll(this);

        renderAlarms();
        updateHeader();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (alarmListContainer != null) {
            alarms = AlarmStorage.load(this);
            renderAlarms();
            updateHeader();
        }
    }

    private void updateDate() {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "EEEE, d MMMM",
                        new Locale("ro", "RO")
                );

        dateText.setText(
                format.format(new Date())
        );
    }

    private void openAddAlarmPicker() {

        Calendar now = Calendar.getInstance();

        TimePickerDialog dialog =
                new TimePickerDialog(
                        this,
                        (view, hourOfDay, minute) -> {

                            boolean[] days =
                                    new boolean[7];

                            AlarmItem alarm =
                                    new AlarmItem(
                                            AlarmStorage.getNextId(this),
                                            hourOfDay,
                                            minute,
                                            true,
                                            days
                                    );

                            alarms.add(alarm);

                            AlarmStorage.save(
                                    this,
                                    alarms
                            );

                            AlarmScheduler.schedule(
                                    this,
                                    alarm
                            );

                            renderAlarms();
                            updateHeader();

                            Toast.makeText(
                                    this,
                                    "Alarma adaugata",
                                    Toast.LENGTH_SHORT
                            ).show();
                        },
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        false
                );

        dialog.show();
    }

    private void openEditAlarmPicker(
            AlarmItem alarm
    ) {

        TimePickerDialog dialog =
                new TimePickerDialog(
                        this,
                        (view, hourOfDay, minute) -> {

                            AlarmScheduler.cancel(
                                    this,
                                    alarm.id
                            );

                            alarm.hour = hourOfDay;
                            alarm.minute = minute;

                            AlarmStorage.save(
                                    this,
                                    alarms
                            );

                            if (alarm.enabled) {
                                AlarmScheduler.schedule(
                                        this,
                                        alarm
                                );
                            }

                            renderAlarms();
                            updateHeader();
                        },
                        alarm.hour,
                        alarm.minute,
                        false
                );

        dialog.show();
    }

    private void renderAlarms() {

        alarmListContainer.removeAllViews();

        if (alarms.isEmpty()) {

            TextView emptyText =
                    new TextView(this);

            emptyText.setText(
                    "Nu sunt alarme setate momentan."
            );

            emptyText.setTextColor(
                    Color.parseColor("#AAAAAA")
            );

            emptyText.setTextSize(12);

            emptyText.setPadding(
                    0,
                    dp(18),
                    0,
                    dp(18)
            );

            alarmListContainer.addView(
                    emptyText
            );

        } else {

            for (int i = 0; i < alarms.size(); i++) {

                AlarmItem alarm =
                        alarms.get(i);

                alarmListContainer.addView(
                        createAlarmRow(alarm)
                );

                if (i < alarms.size() - 1) {

                    View divider =
                            new View(this);

                    divider.setBackgroundColor(
                            Color.parseColor("#F1F1F1")
                    );

                    LinearLayout.LayoutParams dividerParams =
                            new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    dp(1)
                            );

                    divider.setLayoutParams(
                            dividerParams
                    );

                    alarmListContainer.addView(
                            divider
                    );
                }
            }
        }

        ViewGroup.LayoutParams params =
                alarmListScroll.getLayoutParams();

        if (alarms.size() > 3) {
            params.height = dp(255);
            alarmListScroll.setVerticalScrollBarEnabled(true);
        } else {
            params.height =
                    ViewGroup.LayoutParams.WRAP_CONTENT;

            alarmListScroll.setVerticalScrollBarEnabled(false);
        }

        alarmListScroll.setLayoutParams(params);
    }

    private View createAlarmRow(
            AlarmItem alarm
    ) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                0,
                dp(14),
                0,
                dp(14)
        );

        LinearLayout left =
                new LinearLayout(this);

        left.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams leftParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );

        left.setLayoutParams(leftParams);

        LinearLayout timeRow =
                new LinearLayout(this);

        timeRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        timeRow.setGravity(Gravity.BOTTOM);

        TextView timeText =
                new TextView(this);

        timeText.setText(
                formatTime12(
                        alarm.hour,
                        alarm.minute
                )
        );

        timeText.setTextSize(31);
        timeText.setIncludeFontPadding(false);
        timeText.setLetterSpacing(-0.04f);

        TextView amPmText =
                new TextView(this);

        amPmText.setText(
                alarm.hour >= 12
                        ? "PM"
                        : "AM"
        );

        amPmText.setTextSize(12);

        LinearLayout.LayoutParams amParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        amParams.setMargins(
                dp(4),
                0,
                0,
                dp(3)
        );

        amPmText.setLayoutParams(amParams);

        updateAlarmTextColors(
                alarm,
                timeText,
                amPmText
        );

        timeText.setOnClickListener(
                v -> openEditAlarmPicker(alarm)
        );

        amPmText.setOnClickListener(
                v -> openEditAlarmPicker(alarm)
        );

        timeRow.addView(timeText);
        timeRow.addView(amPmText);

        LinearLayout daysRow =
                new LinearLayout(this);

        daysRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        LinearLayout.LayoutParams daysParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        daysParams.setMargins(
                0,
                dp(7),
                0,
                0
        );

        daysRow.setLayoutParams(daysParams);

        for (int i = 0; i < 7; i++) {

            final int dayIndex = i;

            TextView dayButton =
                    createDayButton(
                            alarm,
                            dayIndex
                    );

            dayButton.setOnClickListener(v -> {

                alarm.days[dayIndex] =
                        !alarm.days[dayIndex];

                updateDayButton(
                        dayButton,
                        alarm.days[dayIndex]
                );

                AlarmStorage.save(
                        this,
                        alarms
                );

                if (alarm.enabled) {
                    AlarmScheduler.schedule(
                            this,
                            alarm
                    );
                }

                updateHeader();
            });

            daysRow.addView(dayButton);
        }

        left.addView(timeRow);
        left.addView(daysRow);

        LinearLayout right =
                new LinearLayout(this);

        right.setOrientation(
                LinearLayout.HORIZONTAL
        );

        right.setGravity(
                Gravity.CENTER_VERTICAL
        );

        FrameLayout toggle =
                new FrameLayout(this);

        LinearLayout.LayoutParams toggleParams =
                new LinearLayout.LayoutParams(
                        dp(42),
                        dp(24)
                );

        toggle.setLayoutParams(
                toggleParams
        );

        View toggleDot =
                new View(this);

        toggle.addView(toggleDot);

        updateToggle(
                toggle,
                toggleDot,
                alarm.enabled
        );

        toggle.setOnClickListener(v -> {

            alarm.enabled =
                    !alarm.enabled;

            AlarmStorage.save(
                    this,
                    alarms
            );

            if (alarm.enabled) {
                AlarmScheduler.schedule(
                        this,
                        alarm
                );
            } else {
                AlarmScheduler.cancel(
                        this,
                        alarm.id
                );
            }

            updateToggle(
                    toggle,
                    toggleDot,
                    alarm.enabled
            );

            updateAlarmTextColors(
                    alarm,
                    timeText,
                    amPmText
            );

            updateHeader();
        });

        TextView deleteButton =
                new TextView(this);

        deleteButton.setText("×");
        deleteButton.setTextSize(24);
        deleteButton.setGravity(
                Gravity.CENTER
        );

        deleteButton.setTextColor(
                Color.parseColor("#C7C7C7")
        );

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(
                        dp(30),
                        dp(40)
                );

        deleteParams.setMargins(
                dp(7),
                0,
                0,
                0
        );

        deleteButton.setLayoutParams(
                deleteParams
        );

        deleteButton.setOnClickListener(v -> {

            AlarmScheduler.cancel(
                    this,
                    alarm.id
            );

            alarms.remove(alarm);

            AlarmStorage.save(
                    this,
                    alarms
            );

            renderAlarms();
            updateHeader();

            Toast.makeText(
                    this,
                    "Alarma stearsa",
                    Toast.LENGTH_SHORT
            ).show();
        });

        right.addView(toggle);
        right.addView(deleteButton);

        row.addView(left);
        row.addView(right);

        return row;
    }

    private TextView createDayButton(
            AlarmItem alarm,
            int index
    ) {

        TextView button =
                new TextView(this);

        button.setText(
                dayLabels[index]
        );

        button.setTextSize(8);

        button.setGravity(
                Gravity.CENTER
        );

        button.setTypeface(
                button.getTypeface(),
                android.graphics.Typeface.BOLD
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        dp(20),
                        dp(20)
                );

        params.setMargins(
                0,
                0,
                dp(4),
                0
        );

        button.setLayoutParams(params);

        updateDayButton(
                button,
                alarm.days[index]
        );

        return button;
    }

    private void updateDayButton(
            TextView button,
            boolean selected
    ) {

        if (selected) {

            button.setBackgroundResource(
                    R.drawable.bg_day_on
            );

            button.setTextColor(
                    Color.WHITE
            );

        } else {

            button.setBackgroundResource(
                    R.drawable.bg_day_off
            );

            button.setTextColor(
                    Color.parseColor("#CCCCCC")
            );
        }
    }

    private void updateToggle(
            FrameLayout toggle,
            View dot,
            boolean enabled
    ) {

        FrameLayout.LayoutParams dotParams =
                new FrameLayout.LayoutParams(
                        dp(20),
                        dp(20)
                );

        if (enabled) {

            toggle.setBackgroundResource(
                    R.drawable.bg_toggle_on
            );

            dot.setBackgroundResource(
                    R.drawable.bg_toggle_dot_white
            );

            dotParams.gravity =
                    Gravity.END
                            | Gravity.CENTER_VERTICAL;

            dotParams.setMargins(
                    0,
                    0,
                    dp(3),
                    0
            );

        } else {

            toggle.setBackgroundResource(
                    R.drawable.bg_toggle_off
            );

            dot.setBackgroundResource(
                    R.drawable.bg_toggle_dot_gray
            );

            dotParams.gravity =
                    Gravity.START
                            | Gravity.CENTER_VERTICAL;

            dotParams.setMargins(
                    dp(3),
                    0,
                    0,
                    0
            );
        }

        dot.setLayoutParams(dotParams);
    }

    private void updateAlarmTextColors(
            AlarmItem alarm,
            TextView time,
            TextView amPm
    ) {

        if (alarm.enabled) {

            time.setTextColor(
                    Color.parseColor("#111111")
            );

            amPm.setTextColor(
                    Color.parseColor("#BBBBBB")
            );

        } else {

            time.setTextColor(
                    Color.parseColor("#CFCFCF")
            );

            amPm.setTextColor(
                    Color.parseColor("#DADADA")
            );
        }
    }

    private void updateHeader() {

        AlarmItem nextAlarm = null;
        long nextTime = Long.MAX_VALUE;

        for (AlarmItem alarm : alarms) {

            if (!alarm.enabled) {
                continue;
            }

            long trigger =
                    AlarmScheduler
                            .getNextTriggerMillis(alarm);

            if (trigger < nextTime) {
                nextTime = trigger;
                nextAlarm = alarm;
            }
        }

        if (nextAlarm == null) {

            mainClockText.setText("--:--");
            nextAlarmPill.setText("fără alarmă");

            return;
        }

        String time =
                String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        nextAlarm.hour,
                        nextAlarm.minute
                );

        mainClockText.setText(time);
        nextAlarmPill.setText("activă");
    }

    private String formatTime12(
            int hour,
            int minute
    ) {

        int displayHour = hour % 12;

        if (displayHour == 0) {
            displayHour = 12;
        }

        return String.format(
                Locale.getDefault(),
                "%02d:%02d",
                displayHour,
                minute
        );
    }

    private int dp(int value) {

        return (int) (
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}