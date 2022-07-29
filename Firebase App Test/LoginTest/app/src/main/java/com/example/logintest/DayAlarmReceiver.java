package com.example.logintest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * BroadcastReceiver that receives the regular daily alarms to notify the user.
 */
public class DayAlarmReceiver extends BroadcastReceiver {

    /**
     * is called when the alarm is received
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        //calls the myService which makes the notification
        Intent intent2 = new Intent(context, MyService.class);

        //different starting of the service for different API's
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent2);
        } else {
            context.startService(intent2);
        }
    }
}
