package com.androidbull.firebasechatapp;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class LapitChat extends Application {

private DatabaseReference currentUserReference;
private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        //Enabling Firebase Offline persistence
        //Read more: https://firebase.google.com/docs/database/android/offline-capabilities
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()!=null){
            currentUserReference = FirebaseDatabase.getInstance().getReference().child("TVAC/Users/" + firebaseAuth.getUid());

            currentUserReference.child("online").onDisconnect().setValue("false");

        }

    }



    
}
