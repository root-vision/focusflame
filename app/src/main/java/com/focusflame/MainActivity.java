package com.focusflame;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // UI elementlari
    private TextView tvTime, tvDate, tvTimer, tvStatus, tvSessions, tvMinutes;
    private TextView btnStartPause, btnReset;
    private CardView cardStats;
    private View statusDot;

    // Taymer
    private int focusSeconds = 25 * 60;
    private int totalSeconds = 25 * 60;
    private boolean isRunning = false;
    private Timer timer;
    private int sessions = 0;

    // Vaqt yangilovchi
    private Handler clockHandler = new Handler(Looper.getMainLooper());
    private Runnable clockRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadSessions();
        startClock();
        updateTimerDisplay();

        // Sozlamalar tugmasi
        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        // Start/Pause tugmasi
        btnStartPause.setOnClickListener(v -> toggleTimer());

        // Reset tugmasi
        btnReset.setOnClickListener(v -> resetTimer());

        // Ruxsat tekshirish
        checkPermissions();
    }

    private void initViews() {
        tvTime = findViewById(R.id.tvTime);
        tvDate = findViewById(R.id.tvDate);
        tvTimer = findViewById(R.id.tvTimer);
        tvStatus = findViewById(R.id.tvStatus);
        tvSessions = findViewById(R.id.tvSessions);
        tvMinutes = findViewById(R.id.tvMinutes);
        btnStartPause = findViewById(R.id.btnStartPause);
        btnReset = findViewById(R.id.btnReset);
        statusDot = findViewById(R.id.statusDot);
        cardStats = findViewById(R.id.cardStats);
    }

    /**
     * Hozirgi vaqt va sanani yangilab turish
     */
    private void startClock() {
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE, d MMMM", new Locale("uz"));
                tvTime.setText(timeFmt.format(new Date()));
                tvDate.setText(dateFmt.format(new Date()));
                clockHandler.postDelayed(this, 1000);
            }
        };
        clockHandler.post(clockRunnable);
    }

    /**
     * Taymer boshlash / to'xtatish
     */
    private void toggleTimer() {
        if (isRunning) {
            pauseTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        isRunning = true;
        btnStartPause.setText("PAUSE");
        tvStatus.setText("Work Mode");

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (focusSeconds > 0) {
                        focusSeconds--;
                        updateTimerDisplay();
                    } else {
                        // Sessiya tugadi
                        timer.cancel();
                        isRunning = false;
                        sessions++;
                        saveSessions();
                        focusSeconds = totalSeconds;
                        updateTimerDisplay();
                        btnStartPause.setText("START FOCUS");
                        tvStatus.setText("Ready");
                        updateStats();
                        showSessionComplete();
                    }
                });
            }
        }, 1000, 1000);
    }

    private void pauseTimer() {
        isRunning = false;
        if (timer != null) timer.cancel();
        btnStartPause.setText("RESUME");
        tvStatus.setText("Paused");
    }

    private void resetTimer() {
        isRunning = false;
        if (timer != null) timer.cancel();
        focusSeconds = totalSeconds;
        updateTimerDisplay();
        btnStartPause.setText("START FOCUS");
        tvStatus.setText("Ready");
    }

    private void updateTimerDisplay() {
        int min = focusSeconds / 60;
        int sec = focusSeconds % 60;
        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", min, sec));

        // Progress ring yangilash
        TimerRingView ringView = findViewById(R.id.timerRing);
        if (ringView != null) {
            float progress = 1f - ((float) focusSeconds / totalSeconds);
            ringView.setProgress(progress);
        }
    }

    private void updateStats() {
        tvSessions.setText(String.valueOf(sessions));
        tvMinutes.setText(String.valueOf(sessions * (totalSeconds / 60)));
    }

    private void showSessionComplete() {
        Toast.makeText(this, "🔥 Sessiya tugadi! Ajoyib!", Toast.LENGTH_LONG).show();
    }

    /**
     * Notification Listener ruxsatini tekshirish
     */
    private void checkPermissions() {
        boolean hasPermission = isNotificationListenerEnabled();
        updateStatusIndicator(hasPermission);

        if (!hasPermission) {
            new AlertDialog.Builder(this)
                .setTitle("Ruxsat kerak")
                .setMessage("Bildirishnomalarni Telegramga yuborish uchun " +
                    "\"Bildirishnomaga kirish\" ruxsatini bering.\n\n" +
                    "Keyingi ekranda Focus Flame ni toping va yoqing.")
                .setPositiveButton("Ruxsat berish", (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Keyinroq", null)
                .setCancelable(false)
                .show();
        }
    }

    private boolean isNotificationListenerEnabled() {
        String flat = Settings.Secure.getString(
            getContentResolver(),
            "enabled_notification_listeners"
        );
        return flat != null && flat.contains(getPackageName());
    }

    private void updateStatusIndicator(boolean enabled) {
        if (statusDot != null) {
            statusDot.setBackgroundResource(
                enabled ? R.drawable.dot_green : R.drawable.dot_red
            );
        }
        // Servis holati
        SharedPreferences prefs = getSharedPreferences("focusflame_prefs", MODE_PRIVATE);
        boolean forwarding = prefs.getBoolean("forwarding_enabled", true);
        // Status panel
        TextView tvForwardStatus = findViewById(R.id.tvForwardStatus);
        if (tvForwardStatus != null) {
            if (!enabled) {
                tvForwardStatus.setText("⚠️ Ruxsat kerak");
                tvForwardStatus.setTextColor(0xFFFF7A00);
            } else if (forwarding) {
                tvForwardStatus.setText("✅ Telegram aktiv");
                tvForwardStatus.setTextColor(0xFF4CAF50);
            } else {
                tvForwardStatus.setText("⏸ Yoqilmagan");
                tvForwardStatus.setTextColor(0xFF9AA4B2);
            }
        }
    }

    private void loadSessions() {
        SharedPreferences prefs = getSharedPreferences("focusflame_prefs", MODE_PRIVATE);
        String savedDate = prefs.getString("last_date", "");
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (today.equals(savedDate)) {
            sessions = prefs.getInt("sessions_today", 0);
        } else {
            sessions = 0;
            prefs.edit().putString("last_date", today).putInt("sessions_today", 0).apply();
        }
        updateStats();
    }

    private void saveSessions() {
        SharedPreferences prefs = getSharedPreferences("focusflame_prefs", MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        prefs.edit()
            .putInt("sessions_today", sessions)
            .putString("last_date", today)
            .apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ruxsat holati yangilash
        updateStatusIndicator(isNotificationListenerEnabled());
        loadSessions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
        clockHandler.removeCallbacks(clockRunnable);
    }
}
