package com.example.logintest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * In this class all the companions adding, inviting etc is done
 */
public class AddCompanionActivity extends AppCompatActivity {

    //Firebase functions, database and authentication
    FirebaseFunctions mFuncs;
    FirebaseDatabase db;
    FirebaseAuth mAuth;
    //this reference for this activity
    AddCompanionActivity omgthis;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_companion);

        mFuncs = FirebaseFunctions.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
        }

        db = FirebaseDatabase.getInstance();

        refreshCode();
        omgthis = this;

        // This part finds the user code in the given link and passes it to the app
        Intent appLinkIntent = getIntent();
        Uri appLinkData = appLinkIntent.getData();
        if(appLinkData != null) {
            String query = appLinkData.getEncodedQuery();
            Log.v("Companions", query);
            Pattern pattern = Pattern.compile("ucode=(\\w+)");
            Matcher matcher = pattern.matcher(query);
            if(matcher.find()) {
                String ucode = matcher.group(1);

                ((EditText) findViewById(R.id.enterCode)).setText(ucode);
            }
            else {
                Log.v("Companions", "Nisch gefunden");
            }
        }
    }

    /**
     * Method to invite companions.
     * Opens the email application on the phone with given text and invite link
     * @param view
     */
    public void invite(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Get this awesome mood tracking app!");

        String ucode = ((TextView) findViewById(R.id.myCode)).getText().toString();
        ucode = ucode.substring(ucode.indexOf('\n') + 1);
        intent.putExtra(Intent.EXTRA_TEXT, "Click to download, click again to add me as a companion!\n\nhttps://angrynerds-dac9e.web.app/invite?ucode=" + ucode);
        try {
            startActivity(Intent.createChooser(intent, "Send invitation"));
        }
        catch (Exception e) {
            Toast.makeText(this, "Could not open Email app", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method that handles the companions adding proccess.
     * Includes checking if the companion is already in the list.
     * @param view
     */
    public void add(View view) {
        String ucode = ((TextView) findViewById(R.id.enterCode)).getText().toString();
        Map<String, String> put = new HashMap();
        put.put("code", ucode);

        final String urel;
        switch(((RadioGroup) findViewById(R.id.relationship)).getCheckedRadioButtonId()) {
            case R.id.relCouple:
                urel = "Couple";
                break;
            case R.id.relFriend:
                urel = "Friend";
                break;
            case R.id.relFamily:
                urel = "Family";
                break;
            case R.id.relRoommate:
                urel = "Roommate";
                break;
            default:
                urel = "Other";
        }

        final String nickname = ((TextView) findViewById(R.id.nickname)).getText().toString();

        mFuncs.getHttpsCallable("codeToUid").call(put).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
            @Override
            public void onComplete(@NonNull Task<HttpsCallableResult> task) {

                // Get UID
                Map<String, Object> result = (Map) task.getResult().getData();
                if(result == null) {
                    Toast.makeText(omgthis, "Invalid user code.", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String uid = (String) result.get("uid");
                
                // Check existing companions
                final Companions companions = new Companions();
                companions.addUpdateListener(new Companions.OnUpdateListener() {
                    @Override
                    public void onUpdate() {
                        Map<String, Object> newcomp = null;
                        for (Map<String, Object> companion : companions) {
                            Long end = (Long) companion.get("ended");
                            long ended = end == null ? 0 : end;
                            Log.v("Companions", String.valueOf(uid.equals(companion.get("uid"))));
                            Log.v("Companions", String.valueOf(ended <= 0));
                            if (uid.equals(companion.get("uid"))) {
                                if(ended <= 0) {
                                    Toast.makeText(omgthis, "This user is already your companion.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                else {
                                    companion.remove("ended");
                                    newcomp = companion;
                                    break;
                                }
                            }
                        }

                        // Add companion
                        if(newcomp == null) {
                            newcomp = new HashMap();
                            companions.add(newcomp);
                        }
                        newcomp.put("uid", uid);
                        newcomp.put("start", System.currentTimeMillis());
                        newcomp.put("relationship", urel);

                        final Map<String, Object> newone = newcomp;

                        db.getReference("Users/" + uid + "/profile").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(!nickname.equals("")) {
                                    newone.put("nickname", nickname);
                                }
                                else {
                                    Map<String, Object> profile = (Map) snapshot.getValue();
                                    String usernick = (String) profile.get("nickname");
                                    if(usernick == null || usernick.equals("")) {
                                        usernick = (String) profile.get("first_name") + " " + profile.get("last_name");
                                    }
                                    newone.put("nickname", usernick);
                                }

                                companions.save();
                                Toast.makeText(omgthis, "Companion has been added!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * Method that changes the unique user code of the logged in user.
     * Can then be sent to the other users.
     * @return
     */
    public static Task<HttpsCallableResult> resetCode() {
        Map<String, String> put = new HashMap();
        put.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        return FirebaseFunctions.getInstance().getHttpsCallable("ucode").call(put);
    }

    /**
     * Reset code button.
     * Calls the above resetCode method.
     * After resetting it shows the new user code.
     * @param view
     */
    public void resetCode(View view) {
        Toast.makeText(this, "Changing user code...", Toast.LENGTH_SHORT).show();
        resetCode().addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
            @Override
            public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                refreshCode();
            }
        });
    }

    /**
     * Method that gets the unique user code from the firebase db.
     * Called when the activity is started or the user resets his code.
     */
    public void refreshCode() {
        String uid = mAuth.getCurrentUser().getUid();
        db.getReference("Users/" + uid + "/profile/user_code").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ((TextView) findViewById(R.id.myCode)).setText("My user code:\n" + snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Back button.
     * Starts the companions activity.
     * @param view
     */
    public void back(View view) {
        startActivity(new Intent(this, CompanionsActivity.class));
    }
}