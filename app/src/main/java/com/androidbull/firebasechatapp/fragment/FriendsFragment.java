package com.androidbull.firebasechatapp.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.androidbull.firebasechatapp.R;
import com.androidbull.firebasechatapp.activity.AllUserActivity;
import com.androidbull.firebasechatapp.activity.ChatActivity;
import com.androidbull.firebasechatapp.activity.ProfileActivity;
import com.androidbull.firebasechatapp.model.User;
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

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private static final String TAG = "FriendsFragment";

    private RecyclerView mRvRecyclerView;
    private String currentUserId;
    private DatabaseReference currentUserFriendlistReference;
    private FirebaseRecyclerAdapter<Date, FriendsViewHolder> adapter;

    public FriendsFragment() {
        // Required empty public constructor
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



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i(TAG, "onCreateView: called");
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        mRvRecyclerView = view.findViewById(R.id.friends_rv_friends);
        currentUserId = FirebaseAuth.getInstance().getUid();
        Log.i(TAG, "onCreateView: Current USER ID: " + currentUserId);
        currentUserFriendlistReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("TVAC")
                .child("Friends")
                .child(currentUserId);

        setUpContent();
        mRvRecyclerView.setHasFixedSize(true);
        mRvRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvRecyclerView.setAdapter(adapter);

        return view;
    }

    private void setUpContent() {


        FirebaseRecyclerOptions options  = new FirebaseRecyclerOptions.Builder<Date>().setQuery(currentUserFriendlistReference, new SnapshotParser<Date>() {
            @NonNull
            @Override
            public Date parseSnapshot(@NonNull DataSnapshot snapshot) {
                Log.i(TAG, "parseSnapshot: Friendship: " + snapshot.toString());
                return new Date(snapshot.child("date").toString());

            }
        }).build();

        adapter = new FirebaseRecyclerAdapter<Date, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Date model) {

                final String friendId = getRef(position).getKey();
                FirebaseDatabase.getInstance().getReference().child("TVAC/Users/" + friendId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();
                        final String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();
                        boolean online = dataSnapshot.child("online").getValue().toString().equals("true")?true:false;

                        if(online){
                            holder.mImageViewOnlineIndicator.setVisibility(View.VISIBLE);
                        }else{
                            holder.mImageViewOnlineIndicator.setVisibility(View.GONE);
                        }

                        holder.mTvName.setText(name);
                        holder.mTvStatus.setText(status);



                        Picasso.get().load(thumbnail).placeholder(R.drawable.profile).networkPolicy(NetworkPolicy.OFFLINE).into(holder.mCivProfile, new Callback() {
                            @Override
                            public void onSuccess() {
                                //Image is loaded from cache
                            }

                            @Override
                            public void onError(Exception e) {
                                //profile image couldn't be loaded from cache
                                Picasso.get().load(thumbnail).placeholder(R.drawable.profile).into(holder.mCivProfile);
                            }
                        });
                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};

                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.MyDialogTheme);
                                builder.setTitle("Choose option");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which==0){
                                            //Open profile is clicked
                                            //We need to start profile activity
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("UID", friendId);
                                            startActivity(profileIntent);
                                        }else{
                                            //User clicked send message
                                            Intent profileIntent = new Intent(getContext(), ChatActivity.class);
                                            profileIntent.putExtra("UID", friendId);
                                            profileIntent.putExtra("friend_name",name);
                                            startActivity(profileIntent);
                                        }
                                    }
                                });
                                builder.show();

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
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.layout_user,parent,false);
                return new FriendsViewHolder(view);
            }
        };
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView mTvName;
        TextView mTvStatus;
        CircleImageView mCivProfile;
        ImageView mImageViewOnlineIndicator;


        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            mTvName = itemView.findViewById(R.id.user_tv_name);
            mTvStatus = itemView.findViewById(R.id.user_tv_status);
            mCivProfile = itemView.findViewById(R.id.user_civ_profile);
            mImageViewOnlineIndicator = itemView.findViewById(R.id.user_iv_online_status);
        }


    }

    public class Date {
        String date;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Date() {
        }

        public Date(String date) {
            this.date = date;
        }
    }

}

