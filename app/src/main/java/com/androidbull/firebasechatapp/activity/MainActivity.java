package com.androidbull.firebasechatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.androidbull.firebasechatapp.LapitChat;
import com.androidbull.firebasechatapp.MyBaseActivity;
import com.androidbull.firebasechatapp.R;
import com.androidbull.firebasechatapp.adapter.SectionsPageAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends MyBaseActivity {

    public static final String TAG = "MainActivity";

    private FirebaseAuth firebaseAuth;
    private Toolbar toolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private SectionsPageAdapter sectionsPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseMessaging.getInstance().subscribeToTopic("request");


        firebaseAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.main_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Lapit Chat App");


        mViewPager = findViewById(R.id.main_view_pager);
        mTabLayout = findViewById(R.id.main_tab);


        sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mViewPager.setAdapter(sectionsPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public void onStart() {
        super.onStart();

        Log.i(TAG, "onStart: of MainActivity");

        // Check if User is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            //no one is logged in
            Intent registerActivityIntent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(registerActivityIntent);
            finish();
        } else {
            //Someone is logged in, we need to set it's status to online
//            setOnline(true);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: of MainActivity");

//        setOnline(false);
    }
/*
    public static void setOnline(boolean isOnline) {
        //This function will set the state of online node to true or false accordingly
        FirebaseDatabase.getInstance().getReference()
                .child("TVAC/Users/" + FirebaseAuth.getInstance().getUid())
                .child("online")
                .setValue(isOnline)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.i(TAG, "onComplete: online status update to online" );
                        }
                    }
                });
    }
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.main_menu_logout:
                firebaseAuth.signOut();
                this.onStart();
                break;
            case R.id.menu_account_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.menu_all_users:
                startActivity(new Intent(MainActivity.this, AllUserActivity.class));
                break;
        }

        return true;
    }
}
