package com.androidbull.firebasechatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.androidbull.firebasechatapp.R;
import com.androidbull.firebasechatapp.activity.ProfileActivity;
import com.androidbull.firebasechatapp.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final String TAG = "MessageAdapter";

    private List<Message> messagesListView;
    private String currentUserId;
    private String friendUid;
    private OnProfileClickListener clickListener;

    public MessageAdapter(List<Message> messagesListView,
                          String friendUid,
                          OnProfileClickListener clickListener) {
        this.messagesListView = messagesListView;
        currentUserId = FirebaseAuth.getInstance().getUid();
        this.friendUid = friendUid;
        this.clickListener = clickListener;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder: message at " + position +
                " \n Data: " + messagesListView.get(position).toString());
        Message message = messagesListView.get(position);


        if (message.getFrom() != null) {
            if (message.getFrom().equals(currentUserId)) {
                //It's we who sent the message

                holder.mTvMessage.setBackgroundResource(R.drawable.message_sent_background);
                holder.mTvMessage.setTextColor(holder.view.getContext().getColor(R.color.sentTextColor));

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mTvMessage.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
                holder.mTvMessage.setLayoutParams(params);
                holder.mCivProfile.setVisibility(View.INVISIBLE);
//                params.addRule(RelativeLayout.RIGHT_OF, R.id.message_tv_message,1);

            } else {
                //It is someone else
                FirebaseDatabase.getInstance().getReference().child("TVAC/Users").child(friendUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();
                        Log.i(TAG, "onDataChange: url: " + thumbnail);
                        if (thumbnail.equals("default")) {
                            Picasso.get().load(R.drawable.profile).into(holder.mCivProfile);
                            return;
                        }
                        Picasso.get().load(thumbnail).networkPolicy(NetworkPolicy.OFFLINE).into(holder.mCivProfile, new Callback() {
                            @Override
                            public void onSuccess() {
                                //Profile image loaded from cache
                            }

                            @Override
                            public void onError(Exception e) {
                                //Profile image couldn't be loaded from cache
                                Picasso.get().load(thumbnail).into(holder.mCivProfile);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                holder.mCivProfile.setVisibility(View.VISIBLE);
                holder.mTvMessage.setBackgroundResource(R.drawable.message_received_background);
                holder.mTvMessage.setTextColor(holder.view.getContext().getColor(R.color.receivedTextColor));
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mTvMessage.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                holder.mTvMessage.setLayoutParams(params);
                holder.mCivProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onClick();

                    }
                });

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
        CircleImageView mCivProfile;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            mTvMessage = itemView.findViewById(R.id.message_tv_message);
            mCivProfile = itemView.findViewById(R.id.message_civ_profile);
        }
    }
}
