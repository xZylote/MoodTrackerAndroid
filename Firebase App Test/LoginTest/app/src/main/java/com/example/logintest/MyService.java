package com.example.logintest;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class that makes the notifications
 */
public class MyService extends Service {

    Notification.Builder notificationBuilder;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * On start of the service the notificationMaker method is called
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try{
            notificationMaker();
        }
        catch(Exception e) {
            Log.v("trycatch2", e.toString());
        }


        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
    }

    /**
     * Makes the nofication and prompts it to the user.
     * Also checks if the last notification was ignored by the user.
     */
    public void notificationMaker() {

        NotificationHelper notificationHelper = new NotificationHelper(this);

        SharedPreferences prefs = getSharedPreferences("blubb", MODE_PRIVATE);
        if(prefs.getBoolean("notifshown", false)) {
            String allData = "-1;-1;;;;" + prefs.getLong("notiftime", -1) + ";false";
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String currentUserID = user.getUid();

            Date currentTime = Calendar.getInstance().getTime();
            String date = String.valueOf(currentTime);

            Map<String, Object> data = new HashMap();
            data.put("text", allData);
            data.put("userid", currentUserID);
            data.put("date", date);

            FirebaseFunctions.getInstance().getHttpsCallable("addMessage").call(data);
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("notifshown", true);
        editor.putLong("notiftime", System.currentTimeMillis());

        long prevnotif = prefs.getLong("notiftime", -1);
        if(prevnotif != -1) {
            long lastday = prevnotif - prevnotif % 86400000;
            editor.putBoolean("showlast", System.currentTimeMillis() - lastday >= 86400000);
        }

        editor.apply();


        // notification making for different Api's
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = notificationHelper.getChannelNotification();
        }
        else {

            Intent intentRecord = new Intent(this, RecordMoodActivity.class);
            intentRecord.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentRecord, 0);

            notificationBuilder = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Mood recording notification")
                    .setContentText("Please click and record your mood :)")
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentIntent(pendingIntent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, notificationBuilder.build());
        } else {
            notificationHelper.getManager().notify(1, notificationBuilder.build());
        }
    }
}
