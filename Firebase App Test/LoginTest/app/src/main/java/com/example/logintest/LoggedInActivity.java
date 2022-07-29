package com.example.logintest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Homescreen of the application.
 * From here the user can start mood recordings, visualize moods, manage companions
 * and make changes on his account like password changes.
 */
public class LoggedInActivity extends AppCompatActivity {

    private NotificationManagerCompat notificationManager;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    SharedPreferences prefs = null;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        Intent intent = getIntent();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //notification
        notificationManager = NotificationManagerCompat.from(this);

        prefs = getSharedPreferences("blubb", MODE_PRIVATE);

        // listens for calendar change to wait wor user input
        // if the user clicks the calendar the updateUI method is called with the current date
        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {

                String curDay = String.valueOf(dayOfMonth);
                if (curDay.length() == 1) {
                    curDay = "0" + curDay;
                }
                String curMonth = String.valueOf(month + 1);
                if (curMonth.length() == 1) {
                    curMonth = "0" + curMonth;
                }
                String curYear = String.valueOf(year);
                String curDate = curYear + "/" + curMonth + "/" + curDay;
                updateUI(curDate);
            }
        });

        // gets the currently signed in user and displays his email on the homescreen
        if (user != null) {
            String email = user.getEmail();
            boolean emailVerified = user.isEmailVerified();
            String uid = user.getUid();

            final TextView textViewToChange = (TextView) findViewById(R.id.textView);
            textViewToChange.setText(email);

        } else {
            // No user is signed in
        }
    }


    /**
     * Method that is only called once when the user is logged in for the very first time.
     * The method makes an alarm which is then called to start the alarm iteration process
     * which makes the daily alarms and the main alarm.
     * @param context
     */
    public void makeAlarm(Context context) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);

        // different alarm management for different API versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        }
        else {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        }
    }

    /**
     * At first run the makeAlarm method is called.
     * The boolean firstrun in the sharedPreferences is then set to false and this
     * method is never runned again.
     */
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("firstrun", true)) {
            makeAlarm(this);
            prefs.edit().putBoolean("firstrun", false).apply();
        }
    }

    /**
     * Starts the visualizeDayActvity with a specific day to visualize
     * @param curDate
     */
    private void updateUI(String curDate) {
        Intent intent = new Intent(this, VisualizeDayActivity.class);
        intent.putExtra("DATE", curDate);
        startActivity(intent);
    }

    /**
     * Method for the log out button.
     * Signs the user out and gets back to the MainActivity (Log in screen)
     * @param view
     */
    public void logoutButton(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Method for the visualization button.
     * Opens the visualization day activity.
     * @param view
     */
    public void visualizationsButton(View view) {
        Intent intent = new Intent(this, VisualizeDayActivity.class);
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        intent.putExtra("DATE", formatter.format(new Date()));
        startActivity(intent);
    }

    /**
     * Method for the settings button.
     * Opens the settings page.
     * @param view
     */
    public void settingsButton(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Method for the companions button.
     * Opens the companions page.
     * @param view
     */
    public void companionsButton(View view) {
        Intent intent = new Intent(this, CompanionsActivity.class);
        startActivity(intent);
    }

    /**
     * Method for the companions button.
     * Opens the mood recording page.
     * @param view
     */
    public void recordMoodButton(View view) {
        Intent intent = new Intent(this, RecordMoodActivity.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    /**
     * Method for the help button.
     * Opens the help page.
     * @param view
     */
    public void helpButton(View view) {
        startActivity(new Intent(this, HelpActivity.class));
    }
}