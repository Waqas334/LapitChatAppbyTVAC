package com.androidbull.firebasechatapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidbull.firebasechatapp.MyBaseActivity;
import com.androidbull.firebasechatapp.R;
import com.androidbull.firebasechatapp.adapter.MessageAdapter;
import com.androidbull.firebasechatapp.model.Message;
import com.androidbull.firebasechatapp.util.Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends MyBaseActivity {

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

    private SwipeRefreshLayout mSwipeToRefresh;

    private static final int MESSAGES_TO_LOAD = 10;
    private int pages = 1;

    //TODO Add pagination to chat recyclerView
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


        mSwipeToRefresh = findViewById(R.id.chat_swipe_to_refresh);
        mSwipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pages++;
                messageList.clear();
                loadMessages();

            }
        });

        mRvMessages = findViewById(R.id.chat_rv_messages);
        mRvMessages.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messageList);
        mRvMessages.setAdapter(messageAdapter);

        loadMessages();

        mIvSend = findViewById(R.id.chat_iv_send);
        mIvSend.setOnClickListener(sendOnClickListener);
        mEtMessage = findViewById(R.id.chat_et_message);


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

        rootRef.child("Chat").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(friendId)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put(currentUserId + "/" + friendId + "/", chatAddMap);
                    chatUserMap.put(friendId + "/" + currentUserId + "/", chatAddMap);

                    rootRef.child("Chat").updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.e(TAG, "onComplete: database error: " + databaseError.getDetails());
                                return;
                            }
                            Log.i(TAG, "onComplete: new chat data written");
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadMessages() {
        DatabaseReference currentConvoersationReference = rootRef.child("Messages").child(currentUserId).child(friendId);
        Query limitTo5 = currentConvoersationReference.limitToLast(pages * MESSAGES_TO_LOAD);


        limitTo5.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                Log.d(TAG, "onChildAdded: called: " + dataSnapshot.toString());
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
                if (!mSwipeToRefresh.isRefreshing()) {

                    Log.i(TAG, "onChildAdded: not refreshing");
                    mRvMessages.scrollToPosition(messageList.size() - 1);

                } else {
                    Log.i(TAG, "onChildAdded: was refreshing");
                    mSwipeToRefresh.setRefreshing(false);
                    mRvMessages.scrollToPosition(mRvMessages.getTop());
                }


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

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", currentUserId);


            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef, messageMap);
            messageUserMap.put(friendRef, messageMap);

            rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.e(TAG, "onComplete: " + databaseError.getDetails());
                        return;
                    }
                    mEtMessage.setText("");
                    Log.i(TAG, "onComplete: data written completed");
                }
            });

        }
    };
}

