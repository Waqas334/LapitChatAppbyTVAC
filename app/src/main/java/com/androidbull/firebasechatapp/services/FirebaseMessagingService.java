package com.androidbull.firebasechatapp.services;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.androidbull.firebasechatapp.R;
import com.androidbull.firebasechatapp.activity.MainActivity;
import com.androidbull.firebasechatapp.activity.ProfileActivity;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FirebaseMessagingServic";


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.i(TAG, "onNewToken: s: " + s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i(TAG, "onMessageReceived: notification received");

        if(remoteMessage.getData().size()>0){
            Log.i(TAG, "onMessageReceived: Size is greater than ZERO");
//            String title = remoteMessage.getNotification().getTitle();
//            String body = remoteMessage.getNotification().getBody();
//            String click_action = remoteMessage.getNotification().getClickAction();
//            String fromUserId = remoteMessage.getData().get("from_user_id");


            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String click_action = remoteMessage.getData().get("click_action");
            String fromUserId = remoteMessage.getData().get("from_user_id");

            Log.i(TAG, "onMessageReceived: title: " + title+
                    "\nbody: " + body +
                    "\nclick action: " + click_action +
                    "\nfromUserId: " + fromUserId);
            showNotification(title,body,click_action,fromUserId);
        }


//        Notificatio
    }

    private void showNotification(String title, String body,String click_action,String fromUserId) {

        String DailNotificationChannelId = "chatAppNotification";
        NotificationCompat.Builder builder;
        NotificationChannel defaultChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            defaultChannel = new NotificationChannel(DailNotificationChannelId, "Request Notifications", NotificationManager.IMPORTANCE_HIGH);
            defaultChannel.setDescription("Request notifications will notify you when someone sends you friend request");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(defaultChannel);
        }

        //Pending Intent when notification closed
        Intent openIntent = new Intent(click_action);
        openIntent.putExtra("UID",fromUserId);

        PendingIntent openedPendingIntent = PendingIntent.getActivity(
                this,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new NotificationCompat.Builder(this, DailNotificationChannelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(openedPendingIntent)
//                .setDeleteIntent(delPendingIntent)
                //autocancel will remove the notification when tapped
                .setAutoCancel(true)

                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(1000, builder.build());
    }

}
