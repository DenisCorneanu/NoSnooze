package com.example.pushupalarm;

public class AlarmItem {

    public int id;
    public int hour;
    public int minute;
    public boolean enabled;
    public boolean[] days;

    public AlarmItem(
            int id,
            int hour,
            int minute,
            boolean enabled,
            boolean[] days
    ) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
        this.days = days;
    }
}