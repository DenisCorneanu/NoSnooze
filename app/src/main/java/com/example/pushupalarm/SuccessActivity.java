package com.example.pushupalarm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        TextView currentTimeText =
                findViewById(R.id.successCurrentTime);

        TextView alarmDateText =
                findViewById(R.id.successAlarmDate);

        TextView alarmTimeText =
                findViewById(R.id.successAlarmTime);

        String currentTime =
                new SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                ).format(new Date());

        String currentDate =
                new SimpleDateFormat(
                        "EEEE, d MMMM",
                        new Locale("ro", "RO")
                ).format(new Date());

        currentTimeText.setText(currentTime);
        alarmDateText.setText(currentDate);

        int alarmId =
                getIntent().getIntExtra(
                        "alarm_id",
                        -1
                );

        AlarmItem alarm =
                AlarmStorage.findById(
                        this,
                        alarmId
                );

        if (alarm != null) {

            String alarmTime =
                    String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            alarm.hour,
                            alarm.minute
                    );

            alarmTimeText.setText(alarmTime);

        } else {

            alarmTimeText.setText(currentTime);
        }

        View finishButton =
                findViewById(R.id.finishButton);

        finishButton.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            this,
                            MainActivity.class
                    );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            );

            startActivity(intent);
            finish();
        });
    }
}