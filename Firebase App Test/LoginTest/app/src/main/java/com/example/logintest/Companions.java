package com.example.logintest;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class acts as a list containing the companions of the signed in user
 * It's extended by features to automatically fetch the list from the database
 * and allow for simple modification and saving of the list
 */

public class Companions extends LinkedList<Map<String, Object>> {

    FirebaseAuth mAuth;
    FirebaseDatabase db;
    final DatabaseReference ref;

    Companions omgthis;

    List<OnUpdateListener> subscribers;

    /**
     * Instantiate, set up variables and call fetch() below
     */

    public Companions() {
        super();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        if(mAuth.getCurrentUser() == null)
            throw new UnsupportedOperationException("Cannot instantiate Companions with no authenticated user");

        omgthis = this;
        subscribers = new LinkedList<OnUpdateListener>();

        String uid = mAuth.getCurrentUser().getUid();
        ref = db.getReference("Users/" + uid + "/companions");
        fetch();
    }

    /**
     * Fetch current companion list from database
     * patch list (self) with new contents
     */

    public void fetch() {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                omgthis.clear();
                if(snapshot.exists()) {
                    List<Map<String, Object>> value = (List) snapshot.getValue();
                    addAll(value);
                }
                notifyUpdate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.v("Companions", "Geht nisch");
                notifyUpdate();
            }
        });
    }

    /**
     * Notify subscribers of new update
     */

    public void notifyUpdate() {
        for(OnUpdateListener listener : subscribers) {
            listener.onUpdate();
        }
    }

    /**
     * Save the current list to the database
     */

    public void save() {
        ref.setValue(this);
    }

    /**
     * Embedded update listener class
     */

    public static abstract class OnUpdateListener {
        public abstract void onUpdate();
    }

    /**
     * Add listener for updates on the list, notified through notifyUpdate()
     * @param a # The update listener to add
     */

    public void addUpdateListener(OnUpdateListener a) {
        subscribers.add(a);
    }
}
