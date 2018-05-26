package io.github.stevenzack.pm25reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import pm25.Pm25;

import static android.content.ContentValues.TAG;

public class PM25Service extends Service {
    private final Thread thread;
    private int bound;
    private String city;
    private SharedPreferences sp;
    public static Resources resources;
    public PM25Service() {
        thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while (true) {
                        long i = Pm25.getData(city);
                        if (i >= bound) {
                            String str = String.valueOf(i);
                            if (str.equals("500"))
                                str=str+"+(爆表!)";
                            showNtf(city+"空气质量警告:" +str);
                        }
                        Thread.sleep(3600000);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    showNtf(e.toString());
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        city = sp.getString("city", "北京");
        bound = sp.getInt("bound", 180);
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }
    private void showNtf(String str) {
        final String NOTIFICATION_CHANNEL_ID = "my_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "my_notification_channel");
        long when = System.currentTimeMillis();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.lung);
            builder.setColor(resources.getColor(R.color.red));
        } else {
            builder.setSmallIcon(R.drawable.lung);
        }
        builder.setContentTitle(str);
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class).putExtra("notification", "1");
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        builder.setSound(null);
        builder.setAutoCancel(true);
        Notification notification = builder.build();
        notification.when = when;
//        startForeground(2, notification);
        notificationManager.notify(2,notification);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
