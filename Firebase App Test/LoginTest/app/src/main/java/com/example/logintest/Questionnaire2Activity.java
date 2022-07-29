package com.example.logintest;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Java class for second questionnaire
 */

public class Questionnaire2Activity extends AppCompatActivity {

    String[] questionnaire2 = new String[15];

    /**
     * Create, setup variables
     * @param savedInstanceState
     */

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire_2);
    }

    /**
     * Fetch values and go to third questionnaire if filled in
     * @param view
     */

    public void nextToQuestionnaire3Button(View view) {

        Resources resources = getResources();
        Bundle dataBundle = getIntent().getExtras();

        for(int i=1; i<=questionnaire2.length; i++) {
            int qid = resources.getIdentifier("q" + i, "id", getPackageName());
            TextView qview = findViewById(qid);
            String question = qview.getText().toString();

            try {
                int aid = resources.getIdentifier("qGroup" + i, "id", getPackageName());
                RadioGroup agp = findViewById(aid);
                int sid = agp.getCheckedRadioButtonId();
                String answer = resources.getResourceEntryName(sid);

                int dash = answer.indexOf("-");
                questionnaire2[i-1] = question + ": " + answer.substring(dash + 1);
            } catch (Exception e) {
                Toast.makeText(this, "Must answer all questions.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        dataBundle.putCharSequenceArray("questionnaire2", questionnaire2);

        Intent intent = new Intent(this, Questionnaire3Activity.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtras(dataBundle);
        startActivity(intent);
    }

    /**
     * Cancel, go back to login screen
     * @param view
     */

    public void cancel(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
}
