package com.androidbull.firebasechatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout mTilName;
    private TextInputLayout mTilEmail;
    private TextInputLayout mTilPassword;
    private Button mBtnCreateAccount;

    private FirebaseAuth firebaseAuth;
    private CustomProgressBar customProgressBar;

    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        //Toolbar Adjustments
        toolbar = findViewById(R.id.resgister_app_bar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTilName = findViewById(R.id.register_til_display_name);
        mTilPassword = findViewById(R.id.register_til_password);
        mTilEmail = findViewById(R.id.register_til_email);

        mBtnCreateAccount = findViewById(R.id.register_btn_create_account);
        mBtnCreateAccount.setOnClickListener(registerClickListener);
        customProgressBar = new CustomProgressBar(RegisterActivity.this);


    }


    private View.OnClickListener registerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            customProgressBar.show();
            final String displayName = mTilName.getEditText().getText().toString();
            String email = mTilEmail.getEditText().getText().toString();
            String password = mTilPassword.getEditText().getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || password.length() < 6) {
                customProgressBar.failed("Please enter\nvalid values");
                return;
            }

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //Registration Completed
                        //Getting the token
                        firebaseAuth.getCurrentUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            @Override
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                if (task.isSuccessful()) {
                                    //Got the token,
                                    //Writing it to RTDB
                                    String tokenId = task.getResult().getToken();
                                    databaseReference = FirebaseDatabase.getInstance().getReference()
                                            .child("TVAC")
                                            .child("Users")
                                            .child(firebaseAuth.getUid());

                                    HashMap<String, String> data = new HashMap<>();
                                    data.put("image", "default");
                                    data.put("name", displayName);
                                    data.put("status", "Hi there!");
                                    data.put("thumbnail", "default");
                                    data.put("tokenId", tokenId);


                                    databaseReference.setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //Data written successfully

                                                customProgressBar.done("Done");
                                                customProgressBar.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                    @Override
                                                    public void onDismiss(DialogInterface dialog) {
                                                        Intent mainActivityIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(mainActivityIntent);
                                                        RegisterActivity.this.finish();
                                                    }
                                                });
                                            } else {
                                                customProgressBar.failed(task.getException().getLocalizedMessage());
                                            }
                                        }
                                    });

                                } else {
                                    //Couldn't get the token
                                    customProgressBar.failed("Sorry");

                                }
                            }
                        });
                    } else {
                        customProgressBar.failed(task.getException().getLocalizedMessage());
                        //Something went wrong with the registration
                    }
                }
            });


        }
    };
}
