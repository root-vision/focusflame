package com.focusflame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchForwarding;
    private SeekBar seekFocus, seekBreak;
    private TextView tvFocusVal, tvBreakVal;
    private TextView tvBotStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        loadSettings();
        setupListeners();
        checkBotStatus();
    }

    private void initViews() {
        switchForwarding = findViewById(R.id.switchForwarding);
        seekFocus = findViewById(R.id.seekFocus);
        seekBreak = findViewById(R.id.seekBreak);
        tvFocusVal = findViewById(R.id.tvFocusVal);
        tvBreakVal = findViewById(R.id.tvBreakVal);
        tvBotStatus = findViewById(R.id.tvBotStatus);
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("focusflame_prefs", MODE_PRIVATE);
        boolean forwarding = prefs.getBoolean("forwarding_enabled", true);
        int focus = prefs.getInt("focus_minutes", 25);
        int breakMin = prefs.getInt("break_minutes", 5);

        switchForwarding.setChecked(forwarding);
        seekFocus.setProgress(focus);
        seekBreak.setProgress(breakMin);
        tvFocusVal.setText(focus + " daqiqa");
        tvBreakVal.setText(breakMin + " daqiqa");
    }

    private void setupListeners() {
        // Forwarding toggle
        switchForwarding.setOnCheckedChangeListener((btn, checked) -> {
            SharedPreferences prefs = getSharedPreferences("focusflame_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("forwarding_enabled", checked).apply();
            Toast.makeText(this,
                checked ? "✅ Yuborish yoqildi" : "⏸ Yuborish to'xtatildi",
                Toast.LENGTH_SHORT).show();
        });

        // Focus duration
        seekFocus.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean user) {
                int val = Math.max(5, progress); // Minimum 5 daqiqa
                tvFocusVal.setText(val + " daqiqa");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {
                int val = Math.max(5, s.getProgress());
                SharedPreferences prefs = getSharedPreferences("focusflame_prefs", MODE_PRIVATE);
                prefs.edit().putInt("focus_minutes", val).apply();
            }
        });

        // Break duration
        seekBreak.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean user) {
                int val = Math.max(1, progress);
                tvBreakVal.setText(val + " daqiqa");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {
                int val = Math.max(1, s.getProgress());
                SharedPreferences prefs = getSharedPreferences("focusflame_prefs", MODE_PRIVATE);
                prefs.edit().putInt("break_minutes", val).apply();
            }
        });

        // Notification access tugmasi
        findViewById(R.id.btnNotifAccess).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });

        // Bot test tugmasi
        findViewById(R.id.btnTestBot).setOnClickListener(v -> {
            testTelegramBot();
        });
    }

    /**
     * Telegram botini test qilish
     */
    private void testTelegramBot() {
        tvBotStatus.setText("⏳ Tekshirilmoqda...");
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(
                    "https://api.telegram.org/bot8404558084:AAG33eFh_RQzdhJiC1-URlS0_pBFkvJetT0/sendMessage"
                );
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(8000);

                org.json.JSONObject json = new org.json.JSONObject();
                json.put("chat_id", "5351720273");
                json.put("text", "✅ Focus Flame ulanishi muvaffaqiyatli!\n\nIlova sozlandi va tayyor.");

                byte[] bytes = json.toString().getBytes("UTF-8");
                conn.getOutputStream().write(bytes);
                conn.getOutputStream().flush();

                int code = conn.getResponseCode();
                boolean ok = (code == 200);

                runOnUiThread(() -> {
                    if (ok) {
                        tvBotStatus.setText("✅ Bot ishlayapti! Telegramni tekshiring.");
                        tvBotStatus.setTextColor(0xFF4CAF50);
                    } else {
                        tvBotStatus.setText("❌ Xatolik: " + code);
                        tvBotStatus.setTextColor(0xFFFF5252);
                    }
                });
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvBotStatus.setText("❌ Internet xatolik: " + e.getMessage());
                    tvBotStatus.setTextColor(0xFFFF5252);
                });
            }
        }).start();
    }

    private void checkBotStatus() {
        boolean hasPermission = isNotificationListenerEnabled();
        if (hasPermission) {
            tvBotStatus.setText("📡 Ruxsat berilgan. Bot tayyor.");
            tvBotStatus.setTextColor(0xFF4CAF50);
        } else {
            tvBotStatus.setText("⚠️ Notification ruxsati kerak");
            tvBotStatus.setTextColor(0xFFFF7A00);
        }
    }

    private boolean isNotificationListenerEnabled() {
        String flat = Settings.Secure.getString(
            getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(getPackageName());
    }
}
