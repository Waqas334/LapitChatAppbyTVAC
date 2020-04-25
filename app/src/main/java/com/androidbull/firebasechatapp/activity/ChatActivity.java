package com.androidbull.firebasechatapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidbull.firebasechatapp.MyBaseActivity;
import com.androidbull.firebasechatapp.R;
import com.androidbull.firebasechatapp.adapter.MessageAdapter;
import com.androidbull.firebasechatapp.adapter.OnProfileClickListener;
import com.androidbull.firebasechatapp.model.Message;
import com.androidbull.firebasechatapp.util.Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends MyBaseActivity implements OnProfileClickListener {

    private static final String TAG = "ChatActivity";
    private String friendId;
    private String friendName;

    private Toolbar mToolbar;
    private DatabaseReference friendProfileReference;
    private DatabaseReference rootRef;


    private TextView mTvFriendName;
    private TextView mTvLastSeen;

    private CircleImageView mCivProfile;
    private String currentUserId;

    private ImageView mIvSend;
    private EditText mEtMessage;

    private RecyclerView mRvMessages;
    private MessageAdapter messageAdapter;

    private List<Message> messageList = new ArrayList<>();

//    private SwipeRefreshLayout mSwipeToRefresh;

    private static final int MESSAGES_TO_LOAD = 10;
    private int pages = 1;

    boolean justUpdated = false;

    //TODO Add pagination to chat recyclerView
    //TODO Add friend image to recyclerView
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        friendId = getIntent().getStringExtra("UID");
        friendName = getIntent().getStringExtra("friend_name");

        mToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("");


        friendProfileReference = FirebaseDatabase.getInstance().getReference()
                .child("TVAC/Users/" + friendId);
        rootRef = FirebaseDatabase.getInstance().getReference().child("TVAC");


        currentUserId = FirebaseAuth.getInstance().getUid();

        mTvFriendName = findViewById(R.id.chat_app_bar_name);
        mTvFriendName.setText(friendName);

        mTvLastSeen = findViewById(R.id.chat_app_bar_last_seen);
        mTvLastSeen.setVisibility(View.GONE);

        mCivProfile = findViewById(R.id.chat_app_bar_civ_profile);
        mCivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProfileActivity();
            }
        });

        mIvSend = findViewById(R.id.chat_iv_send);
        mIvSend.setOnClickListener(sendOnClickListener);
        mEtMessage = findViewById(R.id.chat_et_message);


        mRvMessages = findViewById(R.id.chat_rv_messages);
        mRvMessages.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
        messageAdapter = new MessageAdapter(messageList, friendId, ChatActivity.this);
        mRvMessages.setAdapter(messageAdapter);
        loadMessages();


        friendProfileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String onlineValue = dataSnapshot.child("online").getValue().toString();
                String online = onlineValue.equals("true") ? "online" : Utility.getTimeAgo(Long.valueOf(onlineValue));
                mTvLastSeen.setVisibility(View.VISIBLE);
                mTvLastSeen.setText(online);

                final String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();

                Picasso.get().load(thumbnail).placeholder(R.drawable.profile).networkPolicy(NetworkPolicy.OFFLINE).into(mCivProfile, new Callback() {
                    @Override
                    public void onSuccess() {
                        //Image loaded from cache
                    }

                    @Override
                    public void onError(Exception e) {
                        //Couldn't load image from cache, going online now
                        Picasso.get().load(thumbnail).placeholder(R.drawable.profile).into(mCivProfile);
                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        justUpdated = false;


        rootRef.child("Chat").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(friendId) && !justUpdated) {
                    DataSnapshot convoData = dataSnapshot.child(friendId);
                    String from = convoData.child("from").getValue().toString();
                    if (from.equals(currentUserId)) return; //If true means last message was sent from this device so we don't need to update the last seetn value
                    Log.i(TAG, "onDataChange: from in chat activity start: ");


                    rootRef.child("Chat").child(currentUserId).child(friendId).child("seen").setValue(true);
                    rootRef.child("Chat").child(friendId).child(currentUserId).child("seen").setValue(true);
                    justUpdated = true;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void startProfileActivity() {
        Intent profileActivityIntent = new Intent(ChatActivity.this, ProfileActivity.class);
        profileActivityIntent.putExtra("UID", friendId);
        startActivity(profileActivityIntent);
    }

    private void loadMessages() {
        DatabaseReference currentConversationReference = rootRef.child("Messages").child(currentUserId).child(friendId);
//        Query limitTo5 = currentConvoersationReference.limitToLast(pages * MESSAGES_TO_LOAD);


        currentConversationReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                Log.d(TAG, "onChildAdded: called: " + dataSnapshot.toString());
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();

                mRvMessages.scrollToPosition(messageList.size() - 1);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private View.OnClickListener sendOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String message = mEtMessage.getText().toString();
            if (TextUtils.isEmpty(message)) return;

            String pushId = rootRef.child("Messages").child(currentUserId).child(friendId).push().getKey();

            String currentUserRef = "Messages/" + currentUserId + "/" + friendId + "/" + pushId;
            String friendRef = "Messages/" + friendId + "/" + currentUserId + "/" + pushId;

            String friendChatRef = "Chat/" + currentUserId + "/" + friendId;
            String currentUserChatRef = "Chat/" + friendId + "/" + currentUserId;
//
            Map otherUserConvo = new HashMap();
            otherUserConvo.put("seen", true);
            otherUserConvo.put("timestamp", ServerValue.TIMESTAMP);
            otherUserConvo.put("from", currentUserId);
//
//
            Map currentUserConvoMap = new HashMap();
            currentUserConvoMap.put("seen", false);
            currentUserConvoMap.put("timestamp", ServerValue.TIMESTAMP);
            currentUserConvoMap.put("from", currentUserId);


            Map currentUserRefMessage = new HashMap();
            currentUserRefMessage.put("message", message);
//            currentUserRefMessage.put("seen", true);
            currentUserRefMessage.put("type", "text");
            currentUserRefMessage.put("time", ServerValue.TIMESTAMP);
            currentUserRefMessage.put("from", currentUserId);

            Map friendRefMessage = new HashMap();
            friendRefMessage.put("message", message);
//            friendRefMessage.put("seen", false);
            friendRefMessage.put("type", "text");
            friendRefMessage.put("time", ServerValue.TIMESTAMP);
            friendRefMessage.put("from", currentUserId);


            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef, currentUserRefMessage);
            messageUserMap.put(friendRef, friendRefMessage);

            messageUserMap.put(currentUserChatRef, currentUserConvoMap);
            messageUserMap.put(friendChatRef, otherUserConvo);


            rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        //Message could not sent
                        Log.e(TAG, "onComplete: " + databaseError.getDetails());
                        return;
                    }
                    //Message Sent
                    Log.i(TAG, "onComplete: Message sent");
                }
            });

            mEtMessage.setText("");
        }


    };

    @Override
    public void onClick() {
      startProfileActivity();
    }
}

