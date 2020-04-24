package com.androidbull.firebasechatapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.androidbull.firebasechatapp.MyBaseActivity;
import com.androidbull.firebasechatapp.R;
import com.androidbull.firebasechatapp.util.CustomProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends MyBaseActivity {
    private static final String TAG = "SettingsActivity";

    private final int GALLERY_REQUEST_CODE = 001;

    private Button mBtnChangeStatus;
    private Button mBtnChangeProfileImage;
    private CircleImageView mCivProfileImage;
    private TextView mTvStatus;
    private TextView mTvDisplayName;

    private DatabaseReference profileDatabaseReference;
    private String userId;
    private String status;
    private StorageReference profileImageStorageReference;
    private StorageReference thumbnailImageStorageReference;

    private CustomProgressBar customProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        customProgressBar = new CustomProgressBar(this);
        userId = FirebaseAuth.getInstance().getUid();
        profileDatabaseReference = FirebaseDatabase.getInstance().getReference().child("TVAC")
                .child("Users")
                .child(userId);
        profileImageStorageReference = FirebaseStorage.getInstance().getReference()
                .child("TVAC")
                .child("Profile_image").child(userId + ".png");

        thumbnailImageStorageReference = FirebaseStorage.getInstance().getReference().child("TVAC").child("Profile_image").child("thumbnail").child(userId + ".png");


        mBtnChangeProfileImage = findViewById(R.id.settings_btn_change_image);
        mBtnChangeProfileImage.setOnClickListener(profileChangeClickListener);

        mBtnChangeStatus = findViewById(R.id.settings_btn_change_status);
        mBtnChangeStatus.setOnClickListener(changeStatusClickListener);

        mTvDisplayName = findViewById(R.id.settings_tv_display_name);
        mTvStatus = findViewById(R.id.settings_tv_status);

        mCivProfileImage = findViewById(R.id.settings_civ_profile);

        profileDatabaseReference.keepSynced(true);
        profileDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                final String imageUrl = dataSnapshot.child("image").getValue().toString();

                mTvDisplayName.setText(name);
                mTvStatus.setText(status);

                //checking if there is some url stored or not, by default "default" is stored
                if (!imageUrl.equals("default"))
                    //There is some URL
                    //First we will try to load the image offline, if we get the image than there is no need to fetch it from web
                    Picasso.get().load(imageUrl).placeholder(R.drawable.profile).networkPolicy(NetworkPolicy.OFFLINE).into(mCivProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.i(TAG, "onSuccess: picasso fetched the image offline");


                        }

                        @Override
                        public void onError(Exception e) {
                            //Image can't fetch offline, so lets go online
                            Log.i(TAG, "onError: couldn't fetch the image offline");
                            Picasso.get().load(imageUrl).placeholder(R.drawable.profile).into(mCivProfileImage);

                        }
                    });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private View.OnClickListener changeStatusClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent statusUpdateIntent = new Intent(SettingsActivity.this, StatusUpdateActivity.class);
            statusUpdateIntent.putExtra("STATUS", status);
            startActivity(statusUpdateIntent);


        }
    };


    @Override
    protected void onStart() {
        super.onStart();
//        MainActivity.setOnline(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        MainActivity.setOnline(false);
    }

    private View.OnClickListener profileChangeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//            startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_REQUEST_CODE);


            //This is a library I found on github: https://github.com/ArthurHub/Android-Image-Cropper
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(SettingsActivity.this);
        }
    };

    /*Upload Process Explained
     *
     * First Original image will be uploaded
     *   once completed the download url will be fetched
     *       once fetched the download url will be stored to realtime database
     *           once stored thumbnail will be uploaded
     *               once completed download url will be fetch
     *                   once fetch download url will be stored to realtime database
     *                       once completed we can say that the task is completed
     *
     *
     *
     *
     * */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                customProgressBar.show();
                Uri resultUri = result.getUri();
                mCivProfileImage.setImageURI(resultUri);
                Bitmap bitmap = ((BitmapDrawable) mCivProfileImage.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);

                final byte[] imageData = baos.toByteArray();
                Log.i(TAG, "onActivityResult: compressed image size in bytes: " + imageData.length);


                //Uploading the Original (Uncompressed) profile image
                profileImageStorageReference.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Image uploaded successfully
                        //Now getting original image download URL
                        Log.i(TAG, "onSuccess: original image uploaded successfully");
                        profileImageStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //Got the original image download URL
                                //Writing it the original image download url to firebase database
                                Log.i(TAG, "onSuccess: got original image download url");
                                profileDatabaseReference.child("image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //Stored the original image download url to firebase realtime database
                                            //Uploading the thumbnail
                                            UploadTask uploadTask = thumbnailImageStorageReference.putBytes(imageData);
                                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        //thumbnail uploaded
                                                        //Getting the download URL of thumbnail
                                                        thumbnailImageStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                //Got the download URL of thumbnail image
                                                                //Not putting it to Realtime Database
                                                                profileDatabaseReference.child("thumbnail").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            //Download Url of thumbnail image is stored in Firebase Realtime Database
                                                                            customProgressBar.done("Done");
                                                                        } else {
                                                                            //Download URL of thumbnail is not stored in Firebase Realtime Database
                                                                            customProgressBar.failed("Thumbnail URL Could not be stored");
                                                                            Log.e(TAG, "onComplete: Thumbnail download url could not be stored in Firebase Storage: " + task.getException().getLocalizedMessage());
                                                                        }
                                                                    }
                                                                });

                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                //Failed to get the thumbnail download url
                                                                customProgressBar.failed("Sorry");
                                                                Log.e(TAG, "onComplete: failed to fetch the thumbnail download url: " + e.getLocalizedMessage());

                                                            }
                                                        });
                                                    } else {
                                                        customProgressBar.failed("Sorry");
                                                        Log.e(TAG, "onComplete: failed to upload the thumbnail image to firebase storage: " + task.getException().getLocalizedMessage());
                                                    }
                                                }
                                            });

                                        } else {
                                            Log.e(TAG, "onFailure: could write the original image download url to firebase database: " + task.getException().getLocalizedMessage());
                                            customProgressBar.failed("sorry");
                                        }
                                    }
                                });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure: could not get the original image download URL: " + e.getLocalizedMessage());
                                customProgressBar.failed("Sorry");
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: could not upload original image to Firebase Storage: " + e.getLocalizedMessage());
                        customProgressBar.failed("Sorry");
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
