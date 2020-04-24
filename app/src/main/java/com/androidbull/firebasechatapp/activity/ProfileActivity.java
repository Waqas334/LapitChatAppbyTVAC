package com.androidbull.firebasechatapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidbull.firebasechatapp.R;
import com.androidbull.firebasechatapp.util.CustomProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.connection.ListenHashProvider;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    public static final String NOT_FRIENDS = "not_friends";
    public static final String REQUEST_SENT = "request_sent";
    public static final String REQUEST_RECEIVED = "request_received";
    public static final String REQUEST_TYPE = "request_type";
    public static final String FRIENDS = "friends";

    private static final String TAG = "ProfileActivity";


    private String otherUserUid = "";
    private DatabaseReference profileReference;
    private ImageView mIvProfile;
    private TextView mTvName;
    private TextView mTvStatus;
    private TextView mTvTotalFriends;
    private CustomProgressBar customProgressBar;

    private Button mBtnSendFriendRequest;
    private Button mBtnDeclineRequest;

    private String currentState;
    private String currentUserUid;
    private DatabaseReference friendRequestReference;
    private DatabaseReference notificationDatabaseReference;

    private DatabaseReference rootReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        if (getIntent().getExtras() != null) {
            otherUserUid = getIntent().getExtras().getString("UID");
            Log.e(TAG, "onCreate: getIntentExtras is null");
        } else {
            Log.e(TAG, "onCreate: getIntentExtras is not null");

        }


        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("TVAC").child("Friend_req");
        notificationDatabaseReference = FirebaseDatabase.getInstance().getReference().child("TVAC").child("Notification");
        rootReference = FirebaseDatabase.getInstance().getReference().child("TVAC");
        currentUserUid = FirebaseAuth.getInstance().getUid();

        Log.i(TAG, "onCreate: current user id: " + currentUserUid + "\nOther user id: " + otherUserUid);

        //Checking number of friends of target device
        rootReference.child("Friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(otherUserUid)){
                    //Nibba got some friends
                    //Lets check out how many
                    mTvTotalFriends.setText("Total Friends: " + dataSnapshot.child(otherUserUid).getChildrenCount());


                }else{
                    //Nibba got no friends
                    mTvTotalFriends.setText("Total Friends: 0");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Checking Friendship and request state state
        friendRequestReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "firebaseRequestReference onDataChange: firebaseRequestReference");
                //Checking relationship
                //first we are checking if there is any pending from any user
                if (dataSnapshot.hasChild(currentUserUid)) {
                    Log.i(TAG, "firebaseRequestReference onDataChange: firebaseRequestReference has child currentUserId");
                    //Your Uid node is found there. There is some pending request
                    //Now we need to check if that request is between you two
                    DataSnapshot currentUserSnapShot = dataSnapshot.child(currentUserUid);
                    if (currentUserSnapShot.hasChild(otherUserUid)) {
                        Log.i(TAG, "firebaseRequestReference onDataChange: firebaseRequestReference->currentUserId has otherUserUid");
                        //Yes, that pending request is between you two
                        //Now let's check if you sent the request or received one.
                        String requestType = currentUserSnapShot.child(otherUserUid).child("request_type").getValue().toString();
                        switch (requestType) {
                            case "sent":
                                //You (the logged in user) sent friend request to the person who's profile is currently opened
                                Log.i(TAG, "firebaseRequestReference onDataChange: friend request is sent, act accordingly");
                                mBtnSendFriendRequest.setText("Cancel Friend Request");
                                mBtnDeclineRequest.setVisibility(View.INVISIBLE);
                                currentState = REQUEST_SENT;
                                break;
                            case "received":
                                //You (the logged in user) got friend request from the person whose profile is currently opened
                                Log.i(TAG, "firebaseRequestReference onDataChange: friend request is received, act accordingly");
                                mBtnSendFriendRequest.setText("Accept Friend Request");
                                mBtnDeclineRequest.setVisibility(View.VISIBLE);
                                mBtnDeclineRequest.setText("Decline Friend Request");
                                currentState = REQUEST_RECEIVED;
                                break;

                        }
                    } else {
                        //You may have received or sent request to others but not this person
                        Log.e(TAG, "firebaseRequestReference onDataChange: current user id found but the others id is not there");
                        mBtnDeclineRequest.setVisibility(View.GONE);
                        currentState = NOT_FRIENDS;
                        mBtnSendFriendRequest.setText("Send Friend Request");
                    }
                } else {
                    //There is no pending friend request
                    //so we need to check if you two are friends or not
                    Log.e(TAG, "firebaseRequestReference onDataChange: current user id not found in the list");


                    rootReference.child("Friends").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.i(TAG, "FriendsReference onDataChange: called");
                            if (dataSnapshot.hasChild(currentUserUid)) {
                                //You have some friends,
                                //Lets check if this person is in the list
                                Log.i(TAG, "FriendsReference onDataChange: FriendsReference has child currentUserUid");
                                DataSnapshot currentSnapshot = dataSnapshot.child(currentUserUid);
                                if (currentSnapshot.hasChild(otherUserUid)) {
                                    Log.i(TAG, "FriendsReference onDataChange: FriendsReference->currentUserUid has otherUserId");
                                    //Yes you two are friends
                                    mBtnDeclineRequest.setVisibility(View.GONE);
                                    mBtnSendFriendRequest.setText("Unfriend");
                                    currentState = FRIENDS;
                                } else {
                                    //You two are not friends
                                    Log.i(TAG, "FriendsReference onDataChange: FriendsReference->currentUserUid not have otherUserId");
                                    mBtnSendFriendRequest.setText("Send Friend Request");
                                    currentState = NOT_FRIENDS;
                                }
                            } else {
                                //You have ZERO friends
                                Log.i(TAG, "FriendsReference onDataChange: FriendsReference not have currentUserUid");
                                mBtnSendFriendRequest.setText("Send Friend Request");
                                currentState = NOT_FRIENDS;


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    mBtnDeclineRequest.setVisibility(View.GONE);
                    currentState = NOT_FRIENDS;
                    mBtnSendFriendRequest.setText("Send Friend Request");
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mIvProfile = findViewById(R.id.profile_iv_profile);
        mTvName = findViewById(R.id.profile_tv_name);
        mTvStatus = findViewById(R.id.profile_tv_status);
        mBtnDeclineRequest = findViewById(R.id.profile_btn_decline_friend_request);
        mBtnSendFriendRequest = findViewById(R.id.profile_btn_friend_request);
        mTvTotalFriends = findViewById(R.id.profile_tv_total_friends);

        mBtnSendFriendRequest.setOnClickListener(friendRequestClickListener);
        mBtnDeclineRequest.setOnClickListener(declineClickListener);

        currentState = NOT_FRIENDS;

        customProgressBar = new CustomProgressBar(this);
        customProgressBar.show();

        profileReference = FirebaseDatabase.getInstance().getReference().child("TVAC").child("Users").child(otherUserUid);
        profileReference.keepSynced(true);
        profileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String imageUrl = dataSnapshot.child("image").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                Picasso.get().load(imageUrl).placeholder(R.drawable.profile).networkPolicy(NetworkPolicy.OFFLINE).into(mIvProfile, new Callback() {
                            @Override
                            public void onSuccess() {
                                customProgressBar.dismiss();

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(imageUrl).placeholder(R.drawable.profile).into(mIvProfile, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        customProgressBar.dismiss();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        customProgressBar.dismiss();

                                    }
                                });

                            }
                        }
                );
                mTvStatus.setText(status);
                mTvName.setText(name);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                customProgressBar.failed("Try Again");

            }
        });


    }


    private View.OnClickListener declineClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            customProgressBar.show();

            Map queryMap = new HashMap();
            queryMap.put("Friend_req/" + currentUserUid + "/" + otherUserUid, null);
            queryMap.put("Friend_req/" + otherUserUid + "/" + currentUserUid, null);

            rootReference.updateChildren(queryMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    customProgressBar.dismiss();
                    if (databaseError!=null){
                        //we got some error
                        return;
                    }

                    mBtnSendFriendRequest.setText("Send Friend Request");
                    mBtnDeclineRequest.setVisibility(View.INVISIBLE);
                }
            });
        }
    };

    /*
     * Friends Request System Explained
     * If A (having ID: 111) sends Request to B (having ID: 222)
     * Then database update would be
     * Friends
     *          111
     *               222
     *                   sent
     * Friends
     *         222
     *              111
     *                   received
     *
     * OR WE CAN SAY THAT
     *
     * Friends
     *      [Sender Uid]
     *          [Receiver Uid]
     *              sent
     * Friends
     *      [Receiver Udi]
     *          [Sender Uid]
     *              received
     *
     * */


    /*
     *
     * Notification System Explained
     * When a request is sent and the above changes are happen to RTDB
     * Now we will create update in firebase database for the cloud function
     * to read and send notification. We will write:
     * Notification
     *   [Receiver Uid]
     *       [Notification ID - it will be generated by push() function]
     *           from: [Sender Uid]
     *           type: [request]
     *
     * */
    private View.OnClickListener friendRequestClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            //------------SEND FRIEND REQUEST
            if (currentState == NOT_FRIENDS) {


                //The profile user visiting is not your friend.
                //So we need to enable user to send friend request

                Map queryMap = new HashMap();
                //Query doing this:
                //Friends
                //      111
                //          222
                //              sent
                queryMap.put("Friend_req/" + currentUserUid + "/" + otherUserUid + "/" + REQUEST_TYPE, "sent");

                //The following query will do this
                //Friends
                //      222
                //          111
                //              received
                queryMap.put("Friend_req/" + otherUserUid + "/" + currentUserUid + "/" + REQUEST_TYPE, "received");

                //Now we need to set date to notification node so that Cloud function could read it and then
                //use it to send notification

                //First we need to make map for that
                HashMap notificationData = new HashMap();
                notificationData.put("from", currentUserUid);
                notificationData.put("type", "request");

                //now we need to write it to "TVAC/Notification/{ReceiverUid}/{NotificationUid - which is value we get from push() function}/"
                //Let first get the notification ID
                String notificationId = rootReference.child("Notification").child(otherUserUid).push().getKey();
                queryMap.put("Notification/" + otherUserUid + "/" + notificationId, notificationData);

                mBtnSendFriendRequest.setEnabled(false);
                rootReference.updateChildren(queryMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        mBtnSendFriendRequest.setEnabled(true);
                        if (databaseError != null) {
                            //we got some errors
                            mBtnSendFriendRequest.setText("Send Friend Request");
                            Log.e(TAG, "onComplete: Error while perform the write operation on friend request send event: " + databaseError.getMessage());
                        } else {
                            mBtnSendFriendRequest.setText("Cancel Friend Request");
                            currentState = REQUEST_SENT;
                        }
                    }
                });

            }

            //------------UNFRIEND
            else if ( currentState.equals(FRIENDS)) {
                //Request is already sent from you to the person you are waiting for his response
                //Enable user to cancel friend request

                //Or You are already friends and the user clicked the unfriend button

                //in both the cases, we are just going to remove the currentUidNode under otherPersonUid and vice versa

                mBtnSendFriendRequest.setEnabled(false);

                Map queryMap = new HashMap();
                queryMap.put("Friends" + "/" + currentUserUid + "/" + otherUserUid, null);
                queryMap.put("Friends" + "/" + otherUserUid + "/" + currentUserUid, null);
                rootReference.updateChildren(queryMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        mBtnSendFriendRequest.setEnabled(true);
                        if (databaseError != null) {
                            //Something went wrong
                            Log.e(TAG, "onComplete: error: " + databaseError.getDetails());
                            return;
                        }
                        currentState = NOT_FRIENDS;
                        mBtnSendFriendRequest.setText("Send Friend Request");
                    }
                });
            }

            //----------------or CANCEL FRIEND REQUEST
            else if(currentState.equals(REQUEST_SENT)){
                mBtnSendFriendRequest.setEnabled(false);
                Map queryMap = new HashMap();
                queryMap.put("Friend_req/" + currentUserUid + "/" + otherUserUid, null);
                queryMap.put("Friend_req/" + otherUserUid + "/" + currentUserUid, null);

                rootReference.updateChildren(queryMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        mBtnSendFriendRequest.setEnabled(true);
                        if (databaseError!=null){
                            //we got some error
                            return;
                        }

                        mBtnSendFriendRequest.setText("Send Friend Request");
                    }
                });

            }

            //------------ACCEPT FRIEND REQUEST
            else if (currentState.equals(REQUEST_RECEIVED)) {
                //You have received the request from this person, and accept button is pressed
                //In this case we need to do the following changes

                final String date = DateFormat.getDateInstance().format(new Date());


                mBtnSendFriendRequest.setEnabled(false);

                Map queryMap = new HashMap();
                queryMap.put("Friends/" + currentUserUid + "/" + otherUserUid + "/date", date);
                queryMap.put("Friends/" + otherUserUid + "/" + currentUserUid + "/date", date);

                queryMap.put("Friend_req/" + currentUserUid + "/" + otherUserUid, null);
                queryMap.put("Friend_req/" + otherUserUid + "/" + currentUserUid, null);

//                queryMap.put("FriendsNotification" + currentUserUid + "/" + otherUserUid + "/state","accepted");

                rootReference.updateChildren(queryMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        mBtnSendFriendRequest.setEnabled(true);
                        if (databaseError != null) {
                            //we got some error
                            Log.e(TAG, "onComplete: Database Write error: " + databaseError.getMessage());
                            return;
                        }
                        mBtnSendFriendRequest.setText("Unfriend");
                        mBtnDeclineRequest.setVisibility(View.GONE);
                        currentState = FRIENDS;

                    }
                });

//                friendRequestReference.child(currentUserUid).child(otherUserUid).child(REQUEST_TYPE).setValue(FRIENDS).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            friendRequestReference.child(otherUserUid).child(currentUserUid).child(REQUEST_TYPE).setValue(FRIENDS).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()) {
//                                        mBtnSendFriendRequest.setText("Unfriend");
//                                        mBtnDeclineRequest.setVisibility(View.GONE);
//                                        mBtnSendFriendRequest.setEnabled(true);
//                                        currentState = FRIENDS;
//                                    } else {
//                                        Toast.makeText(ProfileActivity.this, "Couldn't accept request", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            });
//
//                        } else {
//                            Toast.makeText(ProfileActivity.this, "Couldn't accept request", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
            }

        }
    };



}
