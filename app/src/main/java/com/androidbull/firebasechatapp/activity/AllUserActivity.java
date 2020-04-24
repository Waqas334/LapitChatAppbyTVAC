package com.androidbull.firebasechatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidbull.firebasechatapp.R;
import com.androidbull.firebasechatapp.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUserActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter<User, UserViewHolder> adapter;

    private static final String TAG = "AllUserActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user);

        mToolbar = findViewById(R.id.all_user_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        databaseReference = FirebaseDatabase.getInstance().getReference().child("TVAC").child("Users");
        databaseReference.keepSynced(true);

        mRecyclerView = findViewById(R.id.al_users_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.HORIZONTAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);


        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setQuery(databaseReference, new SnapshotParser<User>() {
            @NonNull
            @Override
            public User parseSnapshot(@NonNull DataSnapshot snapshot) {
                User user = new User();
                Log.i(TAG, "parseSnapshot: data: " + snapshot.toString());
                user.setName(snapshot.child("name").getValue().toString());
                user.setStatus(snapshot.child("status").getValue().toString());
                user.setThumbnail(snapshot.child("thumbnail").getValue().toString());
                return user;
            }
        }).build();


        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final UserViewHolder holder, int position, @NonNull final User model) {
                holder.status.setText(model.getStatus());
                holder.name.setText(model.getName());

                Picasso.get().load(model.getThumbnail())
                        .placeholder(R.drawable.profile)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.circleImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                //data is loaded offline
                            }

                            @Override
                            public void onError(Exception e) {
                                //data couldn't be fetched offline
                                Picasso.get().load(model.getThumbnail()).placeholder(R.drawable.profile).into(holder.circleImageView);


                            }
                        });

                final String Uid = getRef(position).getKey();

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(AllUserActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("UID", Uid);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_user, parent, false);

                return new UserViewHolder(view);
            }
        };

        mRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView name;
        TextView status;
        CircleImageView circleImageView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            name = itemView.findViewById(R.id.user_tv_name);
            status = itemView.findViewById(R.id.user_tv_status);
            circleImageView = itemView.findViewById(R.id.user_civ_profile);

        }
    }
}
