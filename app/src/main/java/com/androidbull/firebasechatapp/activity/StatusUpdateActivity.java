package com.androidbull.firebasechatapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.androidbull.firebasechatapp.MyBaseActivity;
import com.androidbull.firebasechatapp.R;
import com.androidbull.firebasechatapp.util.CustomProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StatusUpdateActivity extends MyBaseActivity {

    private TextInputLayout mTilStatus;
    private Button mBtnSave;
    private Toolbar mToolbar;

    private DatabaseReference databaseReference;
    private String Uid;

    private CustomProgressBar customProgressBar;
    private String status = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);

        if (getIntent() != null) {
            status = getIntent().getExtras().getString("STATUS");
        }

        Uid = FirebaseAuth.getInstance().getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("TVAC")
                .child("Users")
                .child(Uid);


        mToolbar = findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        customProgressBar = new CustomProgressBar(this);

        mTilStatus = findViewById(R.id.status_til_status);
        mTilStatus.getEditText().setText(status);

        mBtnSave = findViewById(R.id.status_btn_save);
        mBtnSave.setOnClickListener(saveClickListener);


    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private View.OnClickListener saveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            customProgressBar.show();
            databaseReference.child("status").setValue(mTilStatus.getEditText().getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                customProgressBar.done("Status updated");
                                customProgressBar.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        finish();

                                    }
                                });
                            } else {
                                customProgressBar.failed("Sorry");
                            }
                        }
                    });
        }
    };

}

