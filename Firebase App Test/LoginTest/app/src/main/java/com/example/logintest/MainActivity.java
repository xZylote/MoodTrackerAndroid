package com.example.logintest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * MainActivity which is called when the user opens the app.
 * When he was logged in before he is logged in automatically.
 * If not he can log in or create a new account.
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.logintest.MESSAGE";

    String email, password;
    EditText emailInput;
    EditText passwordInput;

    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;


    /**
     * Is called when the activity is started.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        emailInput = (EditText) findViewById(R.id.editTextTextEmailAddress);
        passwordInput = (EditText) findViewById(R.id.editTextTextPassword);

        if(mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, LoggedInActivity.class));
        }
    }

    /**
     * Method for the log in button.
     * Calls the loginAccount method when the user presses the log in button.
     * @param view
     */
    public void loginButton(View view) {
        email = emailInput.getText().toString();
        password = passwordInput.getText().toString();
        loginAccount(email, password);
    }

    /**
     * Method for the sign up button.
     * Calls the createAccount method when the user clicks signup.
     * @param view
     */
    public void signUpButton(View view) {
        email = emailInput.getText().toString();
        password = passwordInput.getText().toString();
        createAccount(email, password);
    }

    /**
     * Method that takes the users email and password and logs into firebase.
     * Then starts the Logged in activity where the user then is logged in.
     * @param email
     * @param password
     */
    private void loginAccount(String email, String password) {

        final Intent intent = new Intent(this, LoggedInActivity.class);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            //intent.putExtra(EXTRA_MESSAGE, message);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    /**
     * Is in the signUpButton method and starts the sign up process.
     * If the user has entered an email and password already it will be taken to the
     * sign up process so he doesn't have to add again.
     * @param email
     * @param password
     */
    private void createAccount(String email, String password) {
        final Intent intent = new Intent(this, SignUpActivity.class);
        intent.putExtra("lEmail", email);
        intent.putExtra("lPassword", password);
        startActivity(intent);
    }

    /**
     * Method to make a toast
     * @param text
     */
    private void showToast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Is called by the reset password button.
     * Sends an reset password email to the given email.
     * @param view
     */
    public void resetPassword(View view) {
        email = emailInput.getText().toString();
        if(email.equals("")) {
            showToast("Please enter your Email address!");
            return;
        }
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    showToast("Email sent!");
                }
                else {
                    showToast("Invalid Email address.");
                }
            }
        });
    }
}