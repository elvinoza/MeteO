package com.example.elvinas.meteo.services;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.elvinas.meteo.AlarmReceiver;
import com.example.elvinas.meteo.MainActivity;

import java.util.Calendar;

/**
 * Created by elvinas on 15.4.12.
 */
public class NotifyService {

    Context context;

    public NotifyService(Context cn){
        this.context = cn;
    }

    public void start() {
        Log.d("is", "working...");
        Calendar calendar = Calendar.getInstance();

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, 0);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10); // first time
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                10*1000,pendingIntent);
    }

    public void stop(){

        Intent intentStop = new Intent(context, AlarmReceiver.class);
        PendingIntent senderStop = PendingIntent.getBroadcast(context,
                0, intentStop, 0);
        AlarmManager alarmManagerstop = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManagerstop.cancel(senderStop);
    }
}
