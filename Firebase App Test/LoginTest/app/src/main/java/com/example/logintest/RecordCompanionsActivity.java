package com.example.logintest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.Map;

/**
 * RecordCompanionsActivity where the user checks which companions where nearby
 */
public class RecordCompanionsActivity extends AppCompatActivity {
    Companions compost;
    ListView listView;
    ListView listView2;
    final ArrayList<String> companionsList = new ArrayList<String>();
    final ArrayList<String> companionsList2 = new ArrayList<String>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_companions);
        Intent intent = getIntent();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        listView = findViewById(R.id.listView);
        listView2 = findViewById(R.id.listView2);

        final Companions companions = new Companions();
        compost = companions;
        companions.addUpdateListener(new Companions.OnUpdateListener() {
            @Override
            public void onUpdate() {
                for(Map<String, Object> companion : companions) {
                    Long end = (Long) companion.get("ended");
                    long ended = end == null ? 0 : end;

                    if(ended <= 0) {
                        companionsList.add((String) companion.get("nickname"));
                    }
                }
                compList();
            }});

        if (user != null) {
            String email = user.getEmail();
        } else {
            // No user is signed in
        }
    }

    /**
     * Gets the companions list and shows it in the lists
     */
    public void compList(){
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, companionsList);
        final ArrayAdapter adapter2 = new ArrayAdapter(this, android.R.layout.simple_list_item_1, companionsList2);

        listView.setAdapter(adapter);
        listView2.setAdapter(adapter2);

        listView.setClickable(true);
        listView2.setClickable(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Object toRemove = adapter.getItem(position);
                companionsList2.add(companionsList.get(position));
                adapter.remove(toRemove);
                adapter2.notifyDataSetChanged();
            }
        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Object toRemove = adapter2.getItem(position);
                companionsList.add(companionsList2.get(position));
                adapter2.remove(toRemove);
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Back button
     * @param view
     */
    public void backToStressRecording(View view) {
        Intent intent = new Intent(this, RecordStressActivity.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    /**
     * Submit / Next button
     * @param view
     */
    public void submitCompanionsButton(View view) {

        SharedPreferences settings = getApplicationContext().getSharedPreferences("dasisteinerfundenerstring", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        String companionsID = "";
        String companionsData = "";
        for (Map<String, Object> companion: compost){
            if (companionsList2.contains((String) companion.get("nickname"))){
                companionsID += companion.get("uid")+",";
                companionsData += companion.get("nickname") + ",";
            }

        }
        if(!companionsID.equals("")) {
            companionsID = companionsID.substring(0, companionsID.length() - 1);
        }
        if(!companionsData.equals("")) {
        companionsData = companionsData.substring(0, companionsData.length()-1);
        }
        editor.putString("companionIDs", companionsID);
        editor.putString("companionsData", companionsData);
        editor.apply();

        Intent intent = new Intent(this, RecordSpecialSituationsActivity.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}