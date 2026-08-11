package com.example.pushupalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {

        int alarmId =
                intent.getIntExtra(
                        "alarm_id",
                        -1
                );

        Toast.makeText(
                context,
                "Alarma a pornit!",
                Toast.LENGTH_SHORT
        ).show();

        Intent alarmIntent =
                new Intent(
                        context,
                        AlarmActivity.class
                );

        alarmIntent.putExtra(
                "alarm_id",
                alarmId
        );

        alarmIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
        );

        alarmIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        context.startActivity(alarmIntent);

        if (alarmId != -1) {
            AlarmScheduler.scheduleNextRepeat(
                    context,
                    alarmId
            );
        }
    }
}