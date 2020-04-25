package com.androidbull.firebasechatapp.fragment;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidbull.firebasechatapp.R;
import com.androidbull.firebasechatapp.activity.ChatActivity;
import com.androidbull.firebasechatapp.model.Conversation;
import com.androidbull.firebasechatapp.model.Message;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationsFragment extends Fragment {

    private static final String TAG = "ConversationsFragment";

    public ConversationsFragment() {
        // Required empty public constructor
    }

    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter<Conversation, ConversationViewHolder> adapter;
    private RecyclerView recyclerView;
    private String currentUserId;

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_conversations, container, false);
        currentUserId = FirebaseAuth.getInstance().getUid();
        recyclerView = view.findViewById(R.id.conversations_rv_conversations);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        databaseReference = FirebaseDatabase.getInstance().getReference().child("TVAC").child("Chat").child(currentUserId);
        Log.i(TAG, "onCreateView: current User ID: " + currentUserId);

        final Query query = databaseReference.orderByChild("timestamp");

        FirebaseRecyclerOptions<Conversation> options = new FirebaseRecyclerOptions.Builder<Conversation>().setQuery(query, new SnapshotParser<Conversation>() {
            @NonNull
            @Override
            public Conversation parseSnapshot(@NonNull DataSnapshot snapshot) {
                return snapshot.getValue(Conversation.class);
            }
        }).build();

        adapter = new FirebaseRecyclerAdapter<Conversation, ConversationViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ConversationViewHolder holder, int position, @NonNull Conversation model) {

                final String friendUid = getRef(position).getKey();
                Log.i(TAG, "onBindViewHolder: userUid: " + friendUid);

                if(!model.isSeen()){
                    holder.mTvLastMessage.setTypeface(null, Typeface.BOLD);
                    holder.mTvLastMessage.setTextColor(Color.RED);
                }

                DatabaseReference lastMessageRefeernce = FirebaseDatabase.getInstance().getReference()
                        .child("TVAC")
                        .child("Messages")
                        .child(currentUserId)
                        .child(friendUid);
                Query lastMessageQuery = lastMessageRefeernce.limitToLast(1);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Log.i(TAG, "onDataChange: lastMessageNode: " + dataSnapshot.toString());
                        Message message = dataSnapshot.getValue(Message.class);
                        holder.mTvLastMessage.setText(message.getMessage());


                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Log.i(TAG, "onChildChanged: changed Date:  " + dataSnapshot.toString());
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                        .child("TVAC")
                        .child("Users")
                        .child(friendUid);
                reference.keepSynced(true);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("UID", friendUid);
                                chatIntent.putExtra("friend_name", name);
                                startActivity(chatIntent);

                            }
                        });


                        final String thumbnailUrl = dataSnapshot.child("thumbnail").getValue().toString();
                        holder.mTvName.setText(name);
                        Picasso.get().load(thumbnailUrl).placeholder(R.drawable.profile).networkPolicy(NetworkPolicy.OFFLINE).into(holder.mCivImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(thumbnailUrl).placeholder(R.drawable.profile).into(holder.mCivImage);

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_user, parent, false);
                return new ConversationViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        return view;
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {

        View view;
        CircleImageView mCivImage;
        TextView mTvName;
        TextView mTvLastMessage;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            mCivImage = itemView.findViewById(R.id.user_civ_profile);
            mTvName = itemView.findViewById(R.id.user_tv_name);
            mTvLastMessage = itemView.findViewById(R.id.user_tv_status);

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}

