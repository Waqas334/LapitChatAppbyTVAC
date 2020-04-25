package com.androidbull.firebasechatapp.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.androidbull.firebasechatapp.R;
import com.androidbull.firebasechatapp.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final String TAG = "MessageAdapter";

    private List<Message> messagesListView;
    private String currentUserId;
    private String friendThumbnailUrl;

    public MessageAdapter(List<Message> messagesListView) {
        this.messagesListView = messagesListView;
        currentUserId = FirebaseAuth.getInstance().getUid();
//        this.friendThumbnailUrl = friendThumbnailUrl;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder: message at " + position +
                " \n Data: " + messagesListView.get(position).toString());
        Message message = messagesListView.get(position);

        if (message.getFrom() != null) {
            if (message.getFrom().equals(currentUserId)) {
                //It's we who sent the message
                holder.mTvMessage.setBackgroundResource(R.drawable.message_sent_background);
                holder.mTvMessage.setTextColor(Color.BLACK);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mTvMessage.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,1);
                holder.mTvMessage.setLayoutParams(params);
                holder.mCivProfile.setVisibility(View.INVISIBLE);
//                params.addRule(RelativeLayout.RIGHT_OF, R.id.message_tv_message,1);

            } else {
                //It is someone else
                holder.mCivProfile.setVisibility(View.VISIBLE);
                holder.mTvMessage.setBackgroundResource(R.drawable.message_received_background);
                holder.mTvMessage.setTextColor(Color.WHITE);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mTvMessage.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0);
                holder.mTvMessage.setLayoutParams(params);

            }
        }

        holder.mTvMessage.setText(message.getMessage());


    }

    @Override
    public int getItemCount() {
        return messagesListView.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView mTvMessage;
        View mCivProfile;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            mTvMessage = itemView.findViewById(R.id.message_tv_message);
            mCivProfile  =itemView.findViewById(R.id.message_civ_profile);
        }
    }
}
