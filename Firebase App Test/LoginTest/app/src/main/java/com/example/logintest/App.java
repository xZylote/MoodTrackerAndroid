package com.example.logintest;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

/**
 * App class that helps with notification channel
 */
public class App extends Application {

    public static final String CHANNEL_ID = "CHANNEL_ID";

    /**
     * When App is called the notification channel is created
     */
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Method that creates a notification channel (for notifications with api 26+)
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channelName";
            String description = "channelDescription";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            //Register the notification channel
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



}
