package com.example.logintest;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Java class for first signup screen
 */

public class SignUpActivity extends AppCompatActivity {

    FirebaseUser user;
    private static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;
    private FirebaseFunctions mFunctions;

    String consentForm;

    /**
     * Create, setup variables
      * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        TextView consent = (TextView) findViewById(R.id.textView12);
        consent.setMovementMethod(new ScrollingMovementMethod());

        mAuth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

        getConsentForm();

        Intent input = getIntent();
        String mail = input.getStringExtra("lEmail");
        ((EditText) findViewById(R.id.editTextTextEmailAddress2)).setText(mail);
        String pass = input.getStringExtra("lPassword");
        ((EditText) findViewById(R.id.editTextTextPassword3)).setText(pass);
    }


    public void submitSignUp(String email, String password) {
        /*
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            user = mAuth.getCurrentUser();
                            updateUI(user.getUid());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
         */
    }

    /**
     * Get consent form from database
     * @param ok
     * @return
     */

    private Task<String> getConsent(String ok) {

        Map<String, Object> data = new HashMap<>();
        data.put("OK", ok);

        return mFunctions
                .getHttpsCallable("getConsent")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return (String) result.get("data321");
                    }
                });
    }

    /**
     * Get consent form using method above, patch TextView with text
     */

    public void getConsentForm() {
        String ok = "OK";

        getConsent(ok)
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            if (e instanceof FirebaseFunctionsException) {
                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                FirebaseFunctionsException.Code code = ffe.getCode();
                                Object details = ffe.getDetails();
                            }
                            Log.w("signUp", "addNumbers:onFailure", e);
                            return;
                        }

                        String result = task.getResult();
                        Log.w("signUp", "Hier das Ergebnis:");
                        Log.w("signUp", String.valueOf(result));

                        final TextView textViewToChange = (TextView) findViewById(R.id.textView12);
                        textViewToChange.setText(String.valueOf(result));
                    }
                });
    }

    /**
     * Cancel, go back to login screen
     * @param view
     */

    public void cancelSignUpButton(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    /**
     * Signup, check if input fields are filled and go to first questionnaire
     * @param view
     */

    public void signUpButton(View view) {
        EditText email;
        EditText password;
        EditText password2;

        password = (EditText) findViewById(R.id.editTextTextPassword3);
        password2 = (EditText) findViewById(R.id.editTextTextPassword4);

        EditText firstName = (EditText) findViewById(R.id.editTextTextPersonName);
        EditText lastName = (EditText) findViewById(R.id.editTextTextPersonName4);
        EditText emailAddress = (EditText) findViewById(R.id.editTextTextEmailAddress2);

        String firstNameString = firstName.getText().toString();
        String lastNameString = lastName.getText().toString();
        String emailAddressString = emailAddress.getText().toString();
        String passwordString = password.getText().toString();
        String password2String = password2.getText().toString();

        CheckBox checkbox = (CheckBox) findViewById(R.id.checkBox);


        if (checkbox.isChecked() && (password2String.equals(passwordString)) && !(TextUtils.isEmpty(firstNameString)) && !(TextUtils.isEmpty(lastNameString)) && !(TextUtils.isEmpty(emailAddressString)) && !(TextUtils.isEmpty(passwordString))) { //check password
            //submitSignUp(email.getText().toString(), password.getText().toString());

            Intent intent2 = new Intent(this, WelcomeActivity.class);
            intent2.putExtra("FIRST_NAME", firstNameString);
            intent2.putExtra("LAST_NAME", lastNameString);
            intent2.putExtra("EMAIL_ADDRESS", emailAddressString);
            intent2.putExtra("PASSWORD", passwordString);
            startActivity(intent2);

        } else {
            Toast.makeText(this, "You did not fill out the needed fields!", Toast.LENGTH_SHORT).show();
            return;
        }
    }

}
