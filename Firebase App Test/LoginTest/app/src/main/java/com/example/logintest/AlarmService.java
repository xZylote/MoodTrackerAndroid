package com.example.logintest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Random;

/**
 * This service class makes the alarms.
 * One main alarm is called once a day which makes the alarms for the day and a
 * new main alarm for the next day.
 */
public class AlarmService extends Service {

    AlarmManager alarmMgr;
    AlarmManager alarmMgr2;
    PendingIntent alarmIntent;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        //refreshs the main alarm for the next day
        makeAlarmTomorrow();

        // makes the alarms for the day
        makeAlarmsForToday();

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
    }

    /**
     * Parser that gives us the minute representation of time
     * @param clock
     * @return
     */
    public static double parseClock(String clock) {
        String[] stamps = clock.split(":");
        return Double.parseDouble(stamps[0]) * 60 + Double.parseDouble(stamps[1]);
    }

    /**
     * Gets the minutes and hours from an decimal time representation
     * @param time
     * @return
     */
    public static double[] formatClock(double time) {
        double minutes = time % 60;
        double hours = (time - minutes) / 60;
        return new double[]{hours, minutes};
    }

    /**
     * This method gets the times and number of notification from the db
     * and makes the alarms for today
     */
    private void makeAlarmsForToday() {

        FirebaseDatabase.getInstance().getReference("Settings/notification_times").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.v("notifications", (String) snapshot.getValue());
                String[] times = ((String) snapshot.getValue()).split(",\\s*");
                for(String formatted : times) {
                    String[] data = formatted.split("\\s*-\\s*");
                    double from = parseClock(data[0]), to = parseClock(data[1]);
                    int count = Integer.parseInt(data[2]);

                    for(double time : randomTimesBetween(from / 60, to / 60, count)) {
                        double[] format = formatClock(time * 60);
                        makeAlarmAtTime((int)format[0], (int)format[1]);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * Calculates random times and checks if they are all half an hour apart.
     * If not it recalculates
     * @param min
     * @param max
     * @param number
     * @return double array with the times as decimal
     */
    private double[] randomTimesBetween(double min, double max, int number) {

        double[] times = new double[number];
        for (int i = 0 ; i < number ; i++) {
            times[i] = randomTimeBetween(min, max);
        }

        Boolean is = true;

        for (int i = 0 ; i < times.length-1 ; i++) {
            for (int j = i+1 ; j < times.length ; j++) {
                if(Math.abs(times[i] - times[j]) < 0.5){
                    is = false;
                }
            }
        }

        if (is == false) {
            times = randomTimesBetween(min, max, number);
        }
        return times;
    }

    /**
     * Gives a random double between two doubles.
     * @param min
     * @param max
     * @return
     */
    private double randomTimeBetween(double min, double max) {
        Random r = new Random();
        double randomTime = min + (max - min) * r.nextDouble();
        return randomTime;
    }

    /**
     * Method to make an alarm at a given hour and minute of the day
     * @param hour
     * @param minute
     */
    private void makeAlarmAtTime(int hour, int minute) {
        Context context = getApplicationContext();
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DayAlarmReceiver.class);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        ComponentName receiver = new ComponentName(context, DayAlarmReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        //Different alarm management for api versions:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        }
        else {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        }
    }


    /**
     * Makes the next main alarm for the start of the next day.
     */
    public void makeAlarmTomorrow() {
        Calendar calendar = Calendar.getInstance();

        //the number of the next day
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int nextDay = dayOfYear + 1;

        //number of next day is set in calendar for alarm
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.DAY_OF_YEAR, nextDay);

        Context context = getApplicationContext();

        alarmMgr2 = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        //Different alarm management for api versions:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            alarmMgr2.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        }
        else {
            alarmMgr2.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        }
    }
}
