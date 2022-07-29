package com.example.logintest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

/**
 * HelpActivity gives advice for the user on how to use the app
 */
public class HelpActivity extends AppCompatActivity {

    /**
     * OnCreate loads the XML file in which the text for the help is specified
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
    }

    /**
     * return to home button
     * @param view
     */
    public void returnHome(View view) {
        Intent intent = new Intent(this, LoggedInActivity.class);
        startActivity(intent);
    }
}