package com.example.logintest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver class that handles incoming alarms
 */
public class AlarmReceiver extends BroadcastReceiver {

    /**
     * Called when the alarm is received
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        //Starts the AlarmService Activity
        Intent intent3 = new Intent(context, AlarmService.class);
        context.startService(intent3);

    }
}
