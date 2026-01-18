package com.meditrack.activities;

import android.app.KeyguardManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meditrack.R;
import com.meditrack.database.AppDatabase;
import com.meditrack.models.DoseStatus;
import com.meditrack.models.Medicine;
import com.meditrack.models.MedicineHistory;
import com.meditrack.utils.AlarmScheduler;
import com.meditrack.utils.Constants;
import com.meditrack.utils.NotificationHelper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AlertActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private Medicine medicine;
    private long scheduledTime;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showOnLockScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        db = AppDatabase.getInstance(this);

        medicine = (Medicine) getIntent().getSerializableExtra(Constants.EXTRA_MEDICINE);
        scheduledTime = getIntent().getLongExtra(Constants.EXTRA_SCHEDULED_TIME, System.currentTimeMillis());

        if (medicine == null) {
            long medId = getIntent().getLongExtra(Constants.EXTRA_MEDICINE_ID, -1);
            if (medId != -1) {
                // Fetch in BG if needed, but usually passed via Intent
                executor.execute(() -> {
                     medicine = db.medicineDao().getMedicineById(medId);
                     runOnUiThread(this::updateUI);
                });
            } else {
                finish(); // Invalid state
                return;
            }
        } else {
            updateUI();
        }

        findViewById(R.id.btn_take).setOnClickListener(v -> markAsTaken());
        findViewById(R.id.btn_snooze).setOnClickListener(v -> snooze());

        startAlarm();
    }

    private void showOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    private void updateUI() {
        if (medicine == null) return;
        TextView tvName = findViewById(R.id.tv_medicine_name);
        TextView tvDosage = findViewById(R.id.tv_dosage);

        tvName.setText(medicine.getName());
        tvDosage.setText(medicine.getDosage() + " • " + medicine.getType());
    }

    private void startAlarm() {
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, alert);
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();

            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                long[] pattern = {0, 1000, 500, 1000, 500, 1000}; // Aggressive
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0)); // 0 = repeat
                } else {
                    vibrator.vibrate(pattern, 0);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAlarm() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private void markAsTaken() {
        stopAlarm();
        if (medicine != null) {
            executor.execute(() -> {
                MedicineHistory history = new MedicineHistory(medicine.getId(), scheduledTime, DoseStatus.TAKEN);
                db.historyDao().insert(history);
                
                // CRITICAL: Check for any scheduled "Nag" alarms and cancel them
                new AlarmScheduler(this).cancelNagAlarm(medicine.getId());
                
                new NotificationHelper(this).cancelNotification(medicine.getId());
                
                // Reschedule next regular occurrence
                new AlarmScheduler(this).scheduleAlarm(medicine); 
            });
            Toast.makeText(this, "Taken: " + medicine.getName(), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void snooze() {
        stopAlarm();
        // The "Nag" alarm is likely already scheduled by Receiver for 5 mins, 
        // but explicit snooze might want custom time (10 mins).
        // For simplicity, we stick to the Nag logic usually, but let's implement standard snooze here.
        if (medicine != null) {
             // Snooze effectively just stops this ring. The Nag logic in Receiver 
             // already scheduled a retry. Or we can explicitly schedule 10 mins.
             // User asked for "5 min interval if not taken". 
             // If they click Snooze, maybe they want 10. Let's respect "Snooze 10 min".
             // So we cancel the 5 min nag and schedule a 10 min one.
             new AlarmScheduler(this).scheduleSnoozeAlarm(medicine, 10);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        stopAlarm();
        super.onDestroy();
    }
}
