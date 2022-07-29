package com.example.logintest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
 * ChangePasswordActivity.
 * Is opened when the user wants to change his password.
 */
public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangePassword";
    EditText passwordInput;
    EditText currentPassword;
    FirebaseAuth mAuth;

    /**
     * onCreate methods is called when the activity is started.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // input for the new and old password for changing the password
        passwordInput = (EditText) findViewById(R.id.editTextTextPassword2);
        currentPassword = (EditText) findViewById(R.id.currentPass);

        //instance of the firebase authentication
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Method for when the user clicks the cancel button.
     * Starts the settings activity again.
     * @param view
     */
    public void cancelButton(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Is called when the user clicks on the change password button.
     * Checks if the user has put in the correct old password by reauthenticating the user
     * and changes the password to the new one when the old one was correct.
     * @param view
     */
    public void changePasswordButton(View view) {
        final Intent intent = new Intent(this, SettingsActivity.class);
        String oldPassword = currentPassword.getText().toString();
        final FirebaseUser user = mAuth.getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

        //reauthenticate
        user.reauthenticate(credential)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        String newPassword = passwordInput.getText().toString();
                        //change password
                        changePassword(user, newPassword);
                        showToast("Password has been changed!");
                        startActivity(intent);
                    }
                    else {
                        //when the old password was incorrect
                        //he can then make a new try
                        showToast("Current password is incorrect.");
                    }
                }
            });
    }

    /**
     * Method that takes the user and password and changes the password
     * of the user to the new one.
     * @param user
     * @param newPassword
     */
    public void changePassword(FirebaseUser user, String newPassword) {

        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Log.d(TAG, "User password updated.");
                        }
                    }
                });
    }

    /**
     * Method to show Toasts.
     * @param text
     */
    private void showToast(String text) {
        Toast.makeText(ChangePasswordActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}