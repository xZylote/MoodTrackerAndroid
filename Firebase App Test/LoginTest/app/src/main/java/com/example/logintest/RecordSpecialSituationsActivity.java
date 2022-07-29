package com.example.logintest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to record the special situations of the user
 */
public class RecordSpecialSituationsActivity extends AppCompatActivity {

    private FirebaseFunctions mFunctions;

    ListView listView1;
    ListView listView2;
    ListView listView3;

    final ArrayList<String> positiveSpecialSituations = new ArrayList<String>();
    final ArrayList<String> negativeSpecialSituations = new ArrayList<String>();
    final ArrayList<String> selectedSpecialSituations = new ArrayList<String>(); // :)

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_special_situation);
        Intent intent = getIntent();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mFunctions = FirebaseFunctions.getInstance();

        listView1 = findViewById(R.id.listView3);
        listView2 = findViewById(R.id.listView4);
        listView3= findViewById(R.id.listView5);

        positiveSpecialSituations.add("Work / Study");
        positiveSpecialSituations.add("Family");
        positiveSpecialSituations.add("Partner related");
        positiveSpecialSituations.add("Health");
        positiveSpecialSituations.add("Other");

        negativeSpecialSituations.add("Work / Study");
        negativeSpecialSituations.add("Family");
        negativeSpecialSituations.add("Partner related");
        negativeSpecialSituations.add("Health");
        negativeSpecialSituations.add("Other");

        final ArrayAdapter adapter1 = new ArrayAdapter(this, android.R.layout.simple_list_item_1, positiveSpecialSituations);
        final ArrayAdapter adapter2 = new ArrayAdapter(this, android.R.layout.simple_list_item_1, negativeSpecialSituations);
        final ArrayAdapter adapter3 = new ArrayAdapter(this, android.R.layout.simple_list_item_1, selectedSpecialSituations);

        listView1.setAdapter(adapter1);
        listView2.setAdapter(adapter2);
        listView3.setAdapter(adapter3);

        listView1.setClickable(true);
        listView2.setClickable(true);
        listView3.setClickable(true);

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Object toRemove = adapter1.getItem(position);
                selectedSpecialSituations.add(positiveSpecialSituations.get(position)+" (+)");
                adapter1.remove(toRemove);
                adapter3.notifyDataSetChanged();
            }
        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Object toRemove = adapter2.getItem(position);
                selectedSpecialSituations.add(negativeSpecialSituations.get(position)+" (-)");
                adapter2.remove(toRemove);
                adapter3.notifyDataSetChanged();
            }
        });

        listView3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                String toRemove = (String) adapter3.getItem(position);

                if(toRemove.contains("+")) {
                    positiveSpecialSituations.add(selectedSpecialSituations.get(position).substring(0,toRemove.length()-4));
                    adapter1.notifyDataSetChanged();
                }
                else {
                    negativeSpecialSituations.add(selectedSpecialSituations.get(position).substring(0,toRemove.length()-4));
                    adapter2.notifyDataSetChanged();
                }

                adapter3.remove(toRemove);
            }
        });

        SharedPreferences settings = getApplicationContext().getSharedPreferences("dasisteinerfundenerstring", Context.MODE_PRIVATE);
        String nameData = settings.getString("companionsName1", String.valueOf(Context.MODE_PRIVATE));

        showToast(String.valueOf(nameData));

        if (user != null) {
            String email = user.getEmail();
        } else {
            // No user is signed in
        }

        if(getSharedPreferences("blubb", MODE_PRIVATE).getBoolean("skiplast", false)) {
            submitSpecialSituationsButton(null);
        }
    }

    /**
     * gather data gets all the data from the recording and puts it together
     */
    public String gatherData() {

        String allRecordedData = "";
        String selectedSpecialSituationsString = "";

        SharedPreferences settings = getApplicationContext().getSharedPreferences("dasisteinerfundenerstring", Context.MODE_PRIVATE);
        int moodData = settings.getInt("moodButtonData", Context.MODE_PRIVATE);
        int stressData = settings.getInt("stressButtonData", Context.MODE_PRIVATE);
        String companionsData = settings.getString("companionsData", String.valueOf(Context.MODE_PRIVATE));
        String companionsIDs = settings.getString("companionIDs", String.valueOf(Context.MODE_PRIVATE));

        for (int i = 0; i < selectedSpecialSituations.size(); i++) {
            selectedSpecialSituationsString = selectedSpecialSituationsString + "," + selectedSpecialSituations.get(i);
        }
        if(!selectedSpecialSituationsString.equals("")) {
            selectedSpecialSituationsString = selectedSpecialSituationsString.substring(1);
        }

        long millis = settings.getLong("notificationTime", -1);
        boolean voluntary = settings.getBoolean("voluntary", true);

        allRecordedData = allRecordedData + moodData + ";" + stressData + ";" + companionsData + ";" + selectedSpecialSituationsString + ";" + companionsIDs + ";" + millis + ";" + voluntary;
        return allRecordedData;
    }

    /**
     * Method to add the data to the db
     * @param text
     * @param user
     * @param date
     * @return
     */
    private Task<String> addMessage(String text, String user, String date) {

        Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        data.put("userid", user);
        data.put("date", date);

        return mFunctions
                .getHttpsCallable("addMessage")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                        return (String) result.get("data123");
                    }
                });
    }


    /**
     * back button
     * takes the user to the companions recording screen
     * @param view
     */
    public void backToCompanionsRecordingButton(View view) {
        Intent intent = new Intent(this, RecordCompanionsActivity.class);
        startActivity(intent);
    }

    /**
     * Submit button.
     * Calls the addMessage function which sends the recording data to a cloud function
     * which adds the data in the db
     * @param view
     */
    public void submitSpecialSituationsButton(View view) {

        String allData = gatherData();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserID = user.getUid();

        Date currentTime = Calendar.getInstance().getTime();
        String date = String.valueOf(currentTime);


        addMessage(allData, currentUserID, date)
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
                            Log.w("addMood", "addNumbers:onFailure", e);
                            showToast("An error occurred.");
                            return;
                        }
                        String result = task.getResult();
                        Log.w("addMood", "Hier das Ergebnis:");
                        Log.w("addMood", String.valueOf(result));
                    }
                });


        Intent intent = new Intent(this, LoggedInActivity.class);
        startActivity(intent);
    }

    /**
     * Method to show a toast
     * @param text
     */
    private void showToast(String text) {
        Toast.makeText(RecordSpecialSituationsActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}
