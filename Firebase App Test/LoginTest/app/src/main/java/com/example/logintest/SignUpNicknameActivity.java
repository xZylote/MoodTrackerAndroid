package com.example.logintest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Java class for final signup screen: nickname and profile picture
 */

public class SignUpNicknameActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    SignUpNicknameActivity omgthis;
    FirebaseDatabase database;
    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_nickname);
        mFunctions = FirebaseFunctions.getInstance();
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        omgthis = this;
    }

    /**
     * Perform signup on button press
     * @param view
     */

    public void nextToLoggedInButton(View view) {

        final Bundle dataBundle = getIntent().getExtras();

        mAuth.createUserWithEmailAndPassword(dataBundle.getString("EMAIL_ADDRESS"), dataBundle.getString("PASSWORD"))
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override

                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        try {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Create database entry
                            Map<String, Object> dataset = new HashMap<String, Object>();
                            dataset.put("age", dataBundle.getInt("AGE"));

                            String[] raws = {"FIRST_NAME", "LAST_NAME", "GENDER", "NATIONALITY", "OCCUPATION", "TONGUE", "LIVING_SITUATION"};
                            for (String current : raws) {
                                dataset.put(current.toLowerCase(), dataBundle.getString(current));
                            }

                            String nickname = ((TextView)findViewById(R.id.nickname)).getText().toString();
                            if(!nickname.equals(""))
                                dataset.put("nickname", nickname);

                            String[] q2a = dataBundle.getStringArray("questionnaire2");
                            String[] q3a = dataBundle.getStringArray("questionnaire3");
                            List<String> qOut = new LinkedList<String>();
                            for (String q2c : q2a) {
                                qOut.add(q2c);
                            }
                            for (String q3c : q3a) {
                                qOut.add(q3c);
                            }
                            dataset.put("questionnaire", qOut);
                            
                            dataset.put("joined", System.currentTimeMillis());

                            AddCompanionActivity.resetCode();

                            // TODO: Profile picture

                            String base = "Users/" + user.getUid() + "/profile";
                            database.getReference(base).setValue(dataset).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    try {
                                        Thread.sleep(2500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    setAcceptedConsentVersion();
                                    AddCompanionActivity.resetCode();
                                }
                            });

                            Intent intent = new Intent(omgthis, LoggedInActivity.class);
                            //intent.putExtra(EXTRA_MESSAGE, message);
                            startActivity(intent);
                        }
                        catch (Exception e) {
                            Log.v("FEHLER", e.toString());
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpNicknameActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    /**
     * Call cloud function to set accepted consent form version
     */

    private Task<String> setAcceptedConsentVersion() {
        Map<String, Object> data = new HashMap<>();
        Log.e("fff",mAuth.getCurrentUser().getUid());
        String ide = mAuth.getCurrentUser().getUid();
        data.put("uid", ide );

        return mFunctions
                .getHttpsCallable("setCSVVersion")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return (String) result.get("success");
                    }
                });
    }

    /**
     * Cancel, go back to login screen
     * @param view
     */

    public void cancel(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }

}