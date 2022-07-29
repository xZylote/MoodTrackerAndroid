package com.example.logintest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Java class for reset password settings screen
 */

public class ResetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "resetPassword" ;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    /**
     * Create, setup variables
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        String emailAddress = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        sendResetEmail(emailAddress);
    }

    /**
     * Cancel and go back on button click
     * @param view
     */

    public void backButton(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    /**
     * Send reset email on button click
     * @param email
     */

    private void sendResetEmail(String email) {

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });

    }
}