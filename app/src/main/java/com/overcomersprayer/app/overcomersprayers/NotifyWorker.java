package com.overcomersprayer.app.overcomersprayers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.overcomersprayer.app.overcomersprayers.activities.MainActivity;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotifyWorker extends Worker {

    private int NOTIFICATION_ID;
    private static final String NOTIFICATION_CHANNEL_ID = "OP";

    private Context context;

    public NotifyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        NOTIFICATION_ID = (int) getInputData().getLong("Notify", 0);

        triggerNotification(NOTIFICATION_ID);

        return Result.success();
    }

    private void triggerNotification(int NOTIFICATION_ID) {

        NotificationManager mNotifyManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent contentIntent = new Intent(context, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID,contentIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(context)
                .setContentTitle("Overcomer's Prayers")
                .setContentText("Have you checked your prayer guide today?")
                .setContentIntent(contentPendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.ic_overcomers_prayers);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "OP_CHANNEL", importance);
            notificationChannel.enableLights(true);
            //notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert mNotifyManager != null;
            notifyBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }

        Notification myNotification = notifyBuilder.build();
        mNotifyManager.notify(this.NOTIFICATION_ID,myNotification);

    }

    public static void scheduleReminder(long duration,Data data,String tag){

        PeriodicWorkRequest notificationWork = new PeriodicWorkRequest.Builder(NotifyWorker.class,1,TimeUnit.DAYS)
                .setInitialDelay(duration, TimeUnit.MILLISECONDS).addTag(tag)
                .setInputData(data)
                .build();
        WorkManager.getInstance().enqueueUniquePeriodicWork("My_Work", ExistingPeriodicWorkPolicy.REPLACE,notificationWork);
    }

    public static void cancelReminder(String tag){
        WorkManager.getInstance().cancelAllWorkByTag(tag);
    }
}
