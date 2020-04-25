package com.androidbull.firebasechatapp.fragment;


import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;

import com.androidbull.firebasechatapp.R;
import com.androidbull.firebasechatapp.activity.ProfileActivity;
import com.androidbull.firebasechatapp.model.Request;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private static final String TAG = "RequestsFragment";


    private RecyclerView mRvRequests;
    private FirebaseRecyclerAdapter<Request, RequestViewHolder> adapter;
    private FirebaseRecyclerOptions<Request> options;
    private DatabaseReference databaseReference;
    private String currentUserUid;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        mRvRequests = view.findViewById(R.id.requests_rv_requests);
        mRvRequests.setHasFixedSize(true);
        mRvRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUserUid = FirebaseAuth.getInstance().getUid();


        databaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("TVAC")
                .child("Friend_req")
                .child(currentUserUid);

        options = new FirebaseRecyclerOptions.Builder<Request>().setQuery(databaseReference, new SnapshotParser<Request>() {
            @NonNull
            @Override
            public Request parseSnapshot(@NonNull DataSnapshot snapshot) {
                Log.i(TAG, "parseSnapshot: " + snapshot.toString());
                Request request = snapshot.getValue(Request.class);
                return request;
            }
        }).build();


        adapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull final Request model) {

                final String senderUid = getRef(position).getKey();


                holder.mTvRequestType.setText(model.getRequest_type());

                if (model.isSent()) {
                    holder.mBtnReject.setVisibility(View.GONE);
                    holder.mBtnAccept.setText("Cancel Request");
                } else {
                    holder.mBtnReject.setVisibility(View.VISIBLE);
                    holder.mBtnAccept.setText("Accept");
                }

                holder.mBtnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (model.isSent()) {
                            //it is you who sent the request
                            //now is the time to cancel the request
                            cancelRequest(senderUid);


                        } else {
                            //Accept request
                            acceptFriendRequest(senderUid);

                        }
                    }
                });

                holder.mBtnReject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rejectFriendRequest(senderUid);
                    }
                });


                FirebaseDatabase.getInstance().getReference()
                        .child("TVAC")
                        .child("Users")
                        .child(senderUid)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                String name = dataSnapshot.child("name").getValue().toString();
                                final String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();
                                String status = dataSnapshot.child("status").getValue().toString();
                                holder.mTvStatus.setText(status);


                                holder.mTvName.setText(name);
                                Picasso.get().load(thumbnail).placeholder(R.drawable.profile).networkPolicy(NetworkPolicy.OFFLINE).into(holder.mCivProfile, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(thumbnail).placeholder(R.drawable.profile).into(holder.mCivProfile);

                                    }
                                });

                                holder.mCivProfile.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent profileActivityIntent = new Intent(getContext(), ProfileActivity.class);
                                        profileActivityIntent.putExtra("UID", senderUid);
                                        startActivity(profileActivityIntent);
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
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_request, parent, false);

                return new RequestViewHolder(view);
            }
        };
        mRvRequests.setAdapter(adapter);

        return view;
    }

    private void rejectFriendRequest(String otherUserUid) {
        Map queryMap = new HashMap();
        queryMap.put("Friend_req/" + currentUserUid + "/" + otherUserUid, null);
        queryMap.put("Friend_req/" + otherUserUid + "/" + currentUserUid, null);

        FirebaseDatabase.getInstance().getReference().child("TVAC").updateChildren(queryMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    //we got some error
                    Log.e(TAG, "onComplete: couldn't reject the friend request from friends fragment: " + databaseError.getMessage());

                    return;
                }

            }
        });

    }

    private void acceptFriendRequest(String otherUserUid) {

        final String date = DateFormat.getDateInstance().format(new Date());

        Map queryMap = new HashMap();
        queryMap.put("Friends/" + currentUserUid + "/" + otherUserUid + "/date", date);
        queryMap.put("Friends/" + otherUserUid + "/" + currentUserUid + "/date", date);

        queryMap.put("Friend_req/" + currentUserUid + "/" + otherUserUid, null);
        queryMap.put("Friend_req/" + otherUserUid + "/" + currentUserUid, null);

//                queryMap.put("FriendsNotification" + currentUserUid + "/" + otherUserUid + "/state","accepted");

        FirebaseDatabase.getInstance().getReference().child("TVAC").updateChildren(queryMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    //we got some error
                    Log.e(TAG, "onComplete: couldn't accept the friend request from friends fragment: " + databaseError.getMessage());
                    return;
                }

            }
        });
    }

    private void cancelRequest(String otherUserUid) {
        Map queryMap = new HashMap();
        queryMap.put("Friend_req/" + currentUserUid + "/" + otherUserUid, null);
        queryMap.put("Friend_req/" + otherUserUid + "/" + currentUserUid, null);

        FirebaseDatabase.getInstance().getReference().child("TVAC").updateChildren(queryMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    //we got some error
                    Log.e(TAG, "onComplete: cloudn't cancel the request from requests fragment: " + databaseError.getDetails());
                    return;
                }

            }
        });
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView mTvName;
        CircleImageView mCivProfile;
        TextView mTvStatus;
        Button mBtnAccept;
        Button mBtnReject;
        TextView mTvRequestType;


        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
            mTvName = itemView.findViewById(R.id.request_tv_name);
            mCivProfile = itemView.findViewById(R.id.request_civ_profile);
            mTvStatus = itemView.findViewById(R.id.request_tv_status);
            mBtnAccept = itemView.findViewById(R.id.request_btn_accept);
            mBtnReject = itemView.findViewById(R.id.request_btn_reject);
            mTvRequestType = itemView.findViewById(R.id.request_tv_request_type);
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
