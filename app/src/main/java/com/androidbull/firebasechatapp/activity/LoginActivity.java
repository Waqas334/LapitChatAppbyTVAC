package com.androidbull.firebasechatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.androidbull.firebasechatapp.R;
import com.androidbull.firebasechatapp.util.CustomProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private Button mBtnLogin;
    private TextInputLayout mTilEmail;
    private TextInputLayout mTilPass;
    private FirebaseAuth firebaseAuth;
    private CustomProgressBar customProgressBar;
    private Toolbar mToolbar;

    private DatabaseReference userDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.login_app_bar);
        mToolbar.setTitle("Login");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("TVAC/Users");


        mTilEmail = findViewById(R.id.login_til_email);
        mTilPass = findViewById(R.id.login_til_password);
        mBtnLogin = findViewById(R.id.login_btn_login);

        mBtnLogin.setOnClickListener(loginButtonClickListener);

        customProgressBar = new CustomProgressBar(this);
    }

    private View.OnClickListener loginButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            customProgressBar.show();
            String email = mTilEmail.getEditText().getText().toString();
            String pass = mTilPass.getEditText().getText().toString();
            if (TextUtils.isEmpty(email) || pass.length() < 6) {
                customProgressBar.failed("Invalid Detials");
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //Sign in

                        //Getting the token
                        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (task.isSuccessful()) {
                                    //We got the token
                                    //Writing token to RTDB
                                    String token = task.getResult().getToken();

                                    userDatabaseReference.child(firebaseAuth.getUid()).child("tokenId").setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //Wrote the token to Firebase Realtime database
                                            //Starting main activity
                                            if (task.isSuccessful()) {
                                                customProgressBar.done("Completed");
                                                customProgressBar.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                    @Override
                                                    public void onDismiss(DialogInterface dialog) {

                                                        Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
                                                        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(mainActivityIntent);
                                                        LoginActivity.this.finish();
                                                    }
                                                });
                                            } else {
                                                //Could not write token id to Firebase Database
                                                customProgressBar.failed("Try again");
                                            }
                                        }
                                    });

                                } else {
                                    Log.e(TAG, "onComplete: couldn't get the token");
                                }
                            }
                        });


                    } else {
                        customProgressBar.failed(task.getException().getLocalizedMessage());
                    }
                }
            });

        }
    };
}
