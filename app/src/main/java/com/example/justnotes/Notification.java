package com.example.justnotes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Notification extends Worker {
    public Notification(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void guardarNotification(Long duracion, Data data, String tag){
        OneTimeWorkRequest notification = new OneTimeWorkRequest.Builder(Notification.class)
                .setInitialDelay(duracion, TimeUnit.MILLISECONDS).addTag(tag)
                .setInputData(data).build();

        WorkManager instance = WorkManager.getInstance();
        instance.enqueue(notification);
    }

    @NonNull
    @Override
    public Result doWork() {
        String titulo = getInputData().getString("titulo");
        String detalle = getInputData().getString("detalle");

        int id = (int) getInputData().getLong("id_notification",0);

        oreo(titulo,detalle);

        return Result.success();
    }

    private void oreo(String t,String d){
        String id = "message";
        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),id);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel nc = new NotificationChannel(id,"nuevo",NotificationManager.IMPORTANCE_HIGH);
            nc.setDescription("norification FCM");
            nc.setShowBadge(true);
            assert  nm != null;
            nm.createNotificationChannel(nc);
        }

        Intent intent = new Intent(getApplicationContext(),NotesActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_ONE_SHOT);

        builder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(t)
                .setTicker("New notification")
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentText(d)
                .setContentIntent(pendingIntent)
                .setContentInfo("New");

        Random random = new Random();
        int idNotification = random.nextInt(8000);

        assert  nm != null;
        nm.notify(idNotification,builder.build());
    }
}
