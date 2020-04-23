package com.androidbull.firebasechatapp.util;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.androidbull.firebasechatapp.R;

public class CustomProgressBar extends Dialog {

    private View view;
    private Context context;
    private TextView mTvMessage;
    private ProgressBar mProgressBar;
    private ImageView mIvIcon;

    public CustomProgressBar(@NonNull Context context) {
        super(context);
        view = getLayoutInflater().inflate(R.layout.progress_view, null, false);
        setContentView(view);
        this.context = context;
        mTvMessage = view.findViewById(R.id.tv_message);
        mProgressBar = view.findViewById(R.id.progress_bar);

        mIvIcon = view.findViewById(R.id.iv_icon);
        mIvIcon.setVisibility(View.INVISIBLE);


        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);
    }

    public void done(String message) {
        if (message == null) {
            message = "   Done    ";
        }
        mIvIcon.setImageDrawable(getContext().getDrawable(R.drawable.ic_done_white_24dp));
        mProgressBar.setVisibility(View.INVISIBLE);
        mIvIcon.setVisibility(View.VISIBLE);
        mTvMessage.setText(message);
        setCancelable(true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CustomProgressBar.this.dismiss();
            }
        }, 1000);
    }

    public void failed(String message) {
        if (message == null) {
            message = "   Failed    ";
        }
        mProgressBar.setVisibility(View.INVISIBLE);
        mIvIcon.setVisibility(View.VISIBLE);
        Drawable drawable = getContext().getDrawable(R.drawable.ic_clear_black_24dp);
        drawable.setTint(getContext().getColor(R.color.colorAccent));
        mIvIcon.setImageDrawable(drawable);
        mTvMessage.setText(message);
        setCancelable(true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CustomProgressBar.this.dismiss();
            }
        }, 3000);
    }

    private void reset() {
        mTvMessage.setText("Please wait");
        mProgressBar.setVisibility(View.VISIBLE);
        mIvIcon.setVisibility(View.INVISIBLE);
    }

    @Override
    public void dismiss() {
        reset();
        super.dismiss();
    }
}