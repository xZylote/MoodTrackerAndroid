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
 * Class for the stress recording
 */
public class RecordStressActivity extends AppCompatActivity {

    public int stressButtonData;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_stress);
        Intent intent = getIntent();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("dasisteinerfundenerstring", Context.MODE_PRIVATE);
    }

    /**
     * Back button
     * @param view
     */
    public void backToMoodRecordingButton(View view) {
        Intent intent = new Intent(this, RecordMoodActivity.class);
        startActivity(intent);
    }

    /**
     * Next button.
     * Takes the data to the next step
     * @param view
     */
    public void recordCompanionsButton(View view) {
        int checked = ((RadioGroup) findViewById(R.id.stressRadioGroup) ).getCheckedRadioButtonId();

        switch(checked) {
            case -1:
                return;
            case R.id.stressButton1:
                stressButtonData = 1;
                break;
            case R.id.stressButton2:
                stressButtonData = 2;
                break;
            case R.id.stressButton3:
                stressButtonData = 3;
                break;
            case R.id.stressButton4:
                stressButtonData = 4;
                break;
            case R.id.stressButton5:
                stressButtonData = 5;
                break;
        }
        SharedPreferences settings = getApplicationContext().getSharedPreferences("dasisteinerfundenerstring", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("stressButtonData", stressButtonData);
        editor.apply();
        Intent intent = new Intent(this, RecordCompanionsActivity.class);
        startActivity(intent);
    }
}