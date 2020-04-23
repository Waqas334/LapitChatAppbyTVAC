package com.androidbull.firebasechatapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.androidbull.firebasechatapp.R;

public class WelcomeActivity extends AppCompatActivity {

    private Button mBtnAlreadyHaveAccount;
    private Button mBtnNeedAccount;


    private View.OnClickListener needAccountCLickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent signUpIntent = new Intent(WelcomeActivity.this, RegisterActivity.class);
            startActivity(signUpIntent);
        }
    };

    private View.OnClickListener loginCLickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent signUpIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(signUpIntent);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        mBtnAlreadyHaveAccount = findViewById(R.id.welcome_already_have_account);
        mBtnNeedAccount = findViewById(R.id.welcome_need_a_new_account);

        mBtnAlreadyHaveAccount.setOnClickListener(loginCLickListener);
        mBtnNeedAccount.setOnClickListener(needAccountCLickListener);

    }
}
