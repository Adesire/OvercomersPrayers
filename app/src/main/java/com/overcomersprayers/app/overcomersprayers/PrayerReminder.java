package com.overcomersprayers.app.overcomersprayers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;

import com.overcomersprayers.app.overcomersprayers.activities.MainActivity;

import androidx.core.app.NotificationCompat;

public class PrayerReminder extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        sendNotifiation(context);
    }

    private void sendNotifiation(Context context){
        NotificationManager mNotifyManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent contentIntent = new Intent(context, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context,NOTIFICATION_ID,contentIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(context)
                .setContentTitle("Overcomer's Prayers")
                .setContentText("Have you checked your prayer guide today?")
                .setContentIntent(contentPendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_overcomers_prayers);

        Notification myNotification = notifyBuilder.build();
        mNotifyManager.notify(NOTIFICATION_ID,myNotification);
    }
}
