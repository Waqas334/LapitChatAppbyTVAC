package com.androidbull.firebasechatapp;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MyBaseActivity extends AppCompatActivity {


    private static final String TAG = "MyBaseActivity";

    private static int onStartCount = 0;
    private static int onStopCount = 0;


    @Override
    protected void onStop() {
        super.onStop();
        onStopCount++;
        Log.i(TAG, "onStop: count: " + onStopCount);
        //If user is online then we gonna save the online value as true, if not than time stemp of when he went offline

        if (onStopCount >= onStartCount) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null)
                FirebaseDatabase.getInstance().getReference()
                        .child("TVAC/Users/" + FirebaseAuth.getInstance().getUid())
                        .child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        onStartCount++;
        Log.i(TAG, "onStart: count: " + onStartCount);
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            FirebaseDatabase.getInstance().getReference()
                    .child("TVAC/Users/" + FirebaseAuth.getInstance().getUid())
                    .child("online").setValue("true");
    }
}
