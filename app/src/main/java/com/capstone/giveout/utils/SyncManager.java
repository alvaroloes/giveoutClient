package com.capstone.giveout.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class SyncManager {
    private static final String pkg = SyncManager.class.getPackage().getName();
    public static final String UPDATE_DATA_ACTION = pkg + "UPDATE_DATA_ACTION";
    public static final String RELOAD_DATA_ACTION = pkg + "RELOAD_DATA_ACTION";

    public static void sendBroadcast(Context context, String action) {
        Intent intent =  new Intent(action);
        context.sendBroadcast(intent);
    }

    public static void setAlarm(Context context, String action, long millis)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), millis , pendingIntent);
    }

    public static void cancelAlarm(Context context, String action)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
    }
}