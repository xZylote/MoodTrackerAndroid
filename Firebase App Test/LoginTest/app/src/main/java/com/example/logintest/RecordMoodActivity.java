package com.example.logintest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Record mood activity where the user can enter a mood rating from 1 to 5
 */
public class RecordMoodActivity extends AppCompatActivity {

    public int moodButtonData;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_mood);
        Intent intent = getIntent();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * back button
     * @param view
     */
    public void backToLoggedInPageButton(View view) {
        Intent intent = new Intent(this, LoggedInActivity.class);
        startActivity(intent);
    }

    /**
     * Next button
     * Takes the user to the stress recording and takes all the data with it in sharedPreferences.
     * @param view
     */
    public void recordStressButton(View view) {

        int checked = ((RadioGroup) findViewById(R.id.radioGroup) ).getCheckedRadioButtonId();

        switch(checked) {
            case -1:
                return;
            case R.id.moodRadio1:
                moodButtonData = 1;
                break;
            case R.id.moodRadio2:
                moodButtonData = 2;
                break;
            case R.id.moodRadio3:
                moodButtonData = 3;
                break;
            case R.id.moodRadio4:
                moodButtonData = 4;
                break;
            case R.id.moodRadio5:
                moodButtonData = 5;
                break;
        }

        SharedPreferences prefs = getSharedPreferences("blubb", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        boolean voluntary = !prefs.getBoolean("notifshown", false);
        long millis = prefs.getLong("notiftime", -1);
        boolean showlast = prefs.getBoolean("showlast", true);

        editor.putBoolean("skiplast", !voluntary && !showlast);

        if(!voluntary) {
            editor.putBoolean("notifshown", false);
            editor.putLong("notiftime", -1);
            editor.apply();
        }

        SharedPreferences settings = getApplicationContext().getSharedPreferences("dasisteinerfundenerstring", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = settings.edit();
        editor2.putInt("moodButtonData", moodButtonData);
        editor2.putLong("notificationTime", millis);
        editor2.putBoolean("voluntary", voluntary);
        editor2.apply();


        Intent intent = new Intent(this, RecordStressActivity.class);
        startActivity(intent);
    }
}