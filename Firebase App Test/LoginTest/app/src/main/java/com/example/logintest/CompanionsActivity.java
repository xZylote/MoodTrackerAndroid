package com.example.logintest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompanionsActivity extends AppCompatActivity {

    ListView listView;
    final ArrayList<String> companionsList = new ArrayList<String>();
    ArrayAdapter thatadapter;
    Companions companions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companions);
        Intent intent = getIntent();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        companions = new Companions();

        companionsList.add("Loading...");
        listView = findViewById(R.id.companionsList);

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, companionsList);
        thatadapter = adapter;
        listView.setAdapter(adapter);
        listView.setClickable(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, final View arg1, final int position, long arg3) {

                AlertDialog alertDialog = new AlertDialog.Builder(CompanionsActivity.this).create();
                alertDialog.setTitle("Choose action:");
                alertDialog.setMessage("Do you want to delete the companion?");

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String uid = "";
                            for(Map<String, Object> companion : companions) {
                                if(companion.get("nickname").equals(((TextView) arg1).getText().toString())) {
                                    companion.put("ended", System.currentTimeMillis());
                                    uid = (String) companion.get("uid");
                                    break;
                                }
                            }
                            companions.save();
                            refreshList();
                            dialog.dismiss();

                            // Also remove self from other's list
                            final String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users/" + uid + "/companions");
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    List<Map<String, Object>> comps = (List) snapshot.getValue();
                                    for(Map<String, Object> companion : comps) {
                                        if(myid.equals((String) companion.get("uid"))) {
                                            companion.put("ended", System.currentTimeMillis());
                                            ref.setValue(comps);
                                            break;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                alertDialog.show();
            }
        });

        companions.addUpdateListener(new Companions.OnUpdateListener() {
            @Override
            public void onUpdate() {
                refreshList();
            }
        });
    }

    public void refreshList() {
        Log.v("Companions", "refresh");
        companionsList.clear();
        companionsList.add("Loading...");
        thatadapter.notifyDataSetChanged();
        companionsList.clear();

        for(Map<String, Object> companion : companions) {
            Long end = (Long) companion.get("ended");
            long ended = end == null ? 0 : end;

            if(ended <= 0) {
                String uid = (String) companion.get("uid");
                String nickname = (String) companion.get("nickname");
                companionsList.add(nickname);
            }
            thatadapter.notifyDataSetChanged();
        }
    }

    public void addCompanion(View view) {
        startActivity(new Intent(this, AddCompanionActivity.class));
    }
}