package com.focusflame;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

/**
 * ForegroundService — Android servisni fon rejimida tirik saqlaydi.
 * Status barida "Focus Flame ishlayapti" ko'rinib turadi.
 * Bu servis Android'ning battery optimization va RAM tozalashdan saqlaydi.
 */
public class ForegroundService extends Service {

    private static final String CHANNEL_ID = "focusflame_fg";
    private static final int NOTIF_ID = 42;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(NOTIF_ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // START_STICKY: o'ldirilsa Android qayta tiklaydi
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Focus Flame",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Focus Flame fon xizmati");
            channel.setShowBadge(false);
            channel.enableLights(false);
            channel.enableVibration(false);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        // Ilovaga qaytish uchun intent
        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Flame 🔥")
            .setContentText("Bildirishnomalar Telegramga yuborilmoqda...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)      // O'chirib bo'lmaydi
            .setSilent(true)       // Tovush yo'q
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }
}
