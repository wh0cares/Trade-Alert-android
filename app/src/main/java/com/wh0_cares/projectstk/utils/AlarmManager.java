package com.wh0_cares.projectstk.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.wh0_cares.projectstk.R;
import com.wh0_cares.projectstk.activities.MainActivity;

import java.util.Calendar;

public class AlarmManager extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Notification notification;
        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            start(context);
             notification = new NotificationCompat.Builder(context)
                     .setSmallIcon(R.mipmap.ic_launcher)
                     .setContentTitle("Project STK")
                     .setContentText("Reboot alarm set")
                     .setContentIntent(pi)
                     .build();
        } else {
             notification = new NotificationCompat.Builder(context)
                     .setSmallIcon(R.mipmap.ic_launcher)
                     .setContentTitle("Project STK")
                     .setContentText("Hi")
                     .setContentIntent(pi)
                     .build();
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    public static void start(Context context) {
        android.app.AlarmManager manager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Intent i = new Intent(context, AlarmManager.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        manager.setRepeating(android.app.AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), android.app.AlarmManager.INTERVAL_DAY, pi);
        Toast.makeText(context, "Alarm set", Toast.LENGTH_SHORT).show();
    }

    public static void cancel(Context context) {
        Intent i = new Intent(context, AlarmManager.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        android.app.AlarmManager manager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pi);
        Toast.makeText(context, "Alarm Canceled", Toast.LENGTH_SHORT).show();
    }
}