package com.capstone.potlatch.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class SyncManager {

    public static final String DATA_SHOULD_BE_REFRESHED_ACTION = SyncManager.class.getPackage().getName() + "DATA_SHOULD_BE_REFRESHED_ACTION";

    public static void setAlarm(Context context)
    {
        AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(DATA_SHOULD_BE_REFRESHED_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        //After after 5 seconds
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000 * 5 , pendingIntent);
    }

    public static void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, SyncManager.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}