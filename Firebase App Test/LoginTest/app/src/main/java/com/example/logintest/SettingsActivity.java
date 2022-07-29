package com.example.logintest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Java class for settings screen
 */

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "ChangePassword";
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    /**
     * Create, setup variables and display email
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);

            if (user != null) {
                String email = user.getEmail();
                final TextView textViewToChange = (TextView) findViewById(R.id.textView38);
                textViewToChange.setText(email);
            } else {
                // No user is signed in
            }
        } catch(Exception e) {
            Log.v("feeehler", e.toString());
        }
    }

    /**
     * Go to reset password screen on button click
     * @param view
     */

    public void resetPasswordButton(View view) {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    /**
     * Go to change password screen on button click
     * @param view
     */

    public void changePasswordButton(View view) {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    /**
     * Delete account on button click, given correct data is entered
     * @param view
     */

    public void deleteAccount(View view) {
        EditText passwordInput = (EditText) findViewById(R.id.editTextTextPassword5);
        String password = passwordInput.getText().toString();
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Your account has been deleted.");
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                //intent.putExtra(EXTRA_MESSAGE, message);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                        }
                        else {
                            Toast.makeText(SettingsActivity.this, "Please enter the correct password!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }

    /**
     * Cancel, go back to home screen
     * @param view
     */

    public void backButton(View view) {
        Intent intent = new Intent(this, LoggedInActivity.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }



}