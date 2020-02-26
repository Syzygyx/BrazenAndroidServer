package com.doozycod.brazenserver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.doozycod.brazenwatch.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM Service";
    private NotificationChannel mChannel;
    private NotificationManager notifManager;
    Bundle bundle = new Bundle();
    public static OnTokenReceive tokenInterface;


    public static void setTokenInterface(OnTokenReceive onTokenReceive) {
        tokenInterface = onTokenReceive;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {
//            tokenInterface = this::onNewToken;
//            Log.e(TAG, "Message data " + remoteMessage.getData());
//            Log.e(TAG, "Message data jj" + remoteMessage.getData().get("message"));
            if(tokenInterface !=null)
            tokenInterface.getToken(remoteMessage.getData().get("message"));
//            sendNotification(remoteMessage);
        }

    }

    @Override
    public void onNewToken(@NonNull String s) {
        Log.e(TAG, "onNewToken: " + s);
        super.onNewToken(s);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Intent intent;

        intent = new Intent(this, VideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //Add Any key-value to pass extras to intent

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String channelId = "Default";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("body"))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        ;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }

}
