package com.androidbull.firebasechatapp;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MyBaseActivity extends AppCompatActivity {


    private static final String TAG = "MyBaseActivity";

    private static int onStartCount = 0;
    private static int onStopCount = 0;


    @Override
    protected void onStop() {
        super.onStop();
        onStopCount++;
        Log.i(TAG, "onStop: count: " + onStopCount);
        if (onStopCount >= onStartCount)
            FirebaseDatabase.getInstance().getReference()
                    .child("TVAC/Users/" + FirebaseAuth.getInstance().getUid())
                    .child("online").setValue(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        onStartCount++;
        Log.i(TAG, "onStart: count: " + onStartCount);
        FirebaseDatabase.getInstance().getReference()
                .child("TVAC/Users/" + FirebaseAuth.getInstance().getUid())
                .child("online").setValue(true);
    }
}
