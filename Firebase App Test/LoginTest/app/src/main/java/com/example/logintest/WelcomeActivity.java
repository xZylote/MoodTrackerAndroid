package com.example.logintest;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Java class for the first questionnaire
 */

public class WelcomeActivity extends AppCompatActivity {

    //data from this class
    int age;
    String gender;
    String nationality;
    String tongue;
    String occupation;
    String livingSituation;

    /**
     * Create variables
     * @param savedInstanceState
     */

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_activity);
    }

    /**
     * Fetch information on button press, go to second questionnaire if filled in
     * @param view
     */

    public void questionnairesNext(View view) {

        EditText ageText = (EditText) findViewById(R.id.editTextNumber);
        EditText nationalityText = (EditText) findViewById(R.id.editTextTextPersonName5);
        EditText tongueText = (EditText) findViewById(R.id.editTextTextPersonName6);
        EditText occupationText = (EditText) findViewById(R.id.editTextTextPersonName7);

        RadioGroup genderBox = (RadioGroup) findViewById(R.id.genderRadio);
        RadioGroup livingBox = (RadioGroup) findViewById(R.id.livingRadio);

        try {
            age = Integer.parseInt(ageText.getText().toString());
        } catch(Exception e) {
            Toast.makeText(this, "You need to enter an age!", Toast.LENGTH_SHORT).show();
            Log.v("Fehler", e.toString());
            return;
        }


        nationality = nationalityText.getText().toString();
        tongue = tongueText.getText().toString();
        occupation = occupationText.getText().toString();

        switch(genderBox.getCheckedRadioButtonId()){
            case R.id.radioButton3: gender = "male";  break;
            case R.id.radioButton4: gender = "female"; break;
            case R.id.radioButton5: gender = "other"; break;
        }

        switch(livingBox.getCheckedRadioButtonId()){
            case R.id.radioButton6: livingSituation = "alone";  break;
            case R.id.radioButton7: livingSituation = "shared flat"; break;
            case R.id.radioButton8: livingSituation = "family"; break;
            case R.id.radioButton11: livingSituation = "partner"; break;
            case R.id.radioButton12: livingSituation = "other"; break;
        }

        // Check if everything is filled in

        if (!(TextUtils.isEmpty(nationality)) && !(TextUtils.isEmpty(tongue)) && !(TextUtils.isEmpty(gender)) && !(TextUtils.isEmpty(occupation)) && !(TextUtils.isEmpty(livingSituation))) { //check password

            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();

            Intent intent2 = new Intent(this, Questionnaire2Activity.class);
            Intent intent = getIntent();

            Bundle dataBundle = intent.getExtras();
            dataBundle.putInt("AGE", age);
            dataBundle.putCharSequence("GENDER", gender);
            dataBundle.putCharSequence("NATIONALITY", nationality);
            dataBundle.putCharSequence("OCCUPATION", occupation);
            dataBundle.putCharSequence("TONGUE", tongue);
            dataBundle.putCharSequence("LIVING_SITUATION", livingSituation);

            intent2.putExtras(dataBundle);
            startActivity(intent2);

        } else {
            Toast.makeText(this, "You did not fill out the needed fields!", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    /**
     * Cancel, go back to login screen on button click
     * @param view
     */

    public void cancel(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
}
