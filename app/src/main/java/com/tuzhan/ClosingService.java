package com.tuzhan;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Dhaulagiri on 7/12/2017.
 */

public class ClosingService extends Service {

    FirebaseDatabase database;
    DatabaseReference root;
    String email;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //set up database
        database = FirebaseDatabase.getInstance();
        root = database.getReference();
        this.email = intent.getStringExtra("email");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        // Handle application closing
        root.child("UsersStates").child(email.replace('.',',')).setValue(false);

        // Destroy the service
        stopSelf();
    }
}