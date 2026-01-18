package com.meditrack.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.meditrack.database.AppDatabase;
import com.meditrack.models.DoseStatus;
import com.meditrack.models.Medicine;
import com.meditrack.models.MedicineHistory;
import com.meditrack.utils.AlarmScheduler;
import com.meditrack.utils.Constants;
import com.meditrack.utils.NotificationHelper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        long medicineId = intent.getLongExtra(Constants.EXTRA_MEDICINE_ID, -1);
        long scheduledTime = intent.getLongExtra(Constants.EXTRA_SCHEDULED_TIME, System.currentTimeMillis());

        if (medicineId == -1) {
            Log.e(TAG, "Invalid medicine ID");
            return;
        }

        NotificationHelper notificationHelper = new NotificationHelper(context);
        AppDatabase db = AppDatabase.getInstance(context);
        AlarmScheduler alarmScheduler = new AlarmScheduler(context);

        if (action == null) {
            // Alarm triggered 
            // 1. Schedule "Fail-safe" Nag Alarm (5 mins from now) to ensure persistence
            //    This will keep ringing every 5 mins until cancelled by "Take" action.
            if (medicineId != -1) {
                 // Fetch simple details from intent if possible to avoid DB call just for ID
                 // But we need the medicine object.
                 Medicine medicine = (Medicine) intent.getSerializableExtra(Constants.EXTRA_MEDICINE);
                 if (medicine != null) {
                     alarmScheduler.scheduleNagAlarm(medicine); 
                 }
            }
            
            // 2. Show notification & Start Full Screen Alert
            handleAlarmTrigger(context, intent, notificationHelper, db);
        } else {
            switch (action) {
                case Constants.ACTION_TAKE:
                    executor.execute(() -> {
                        MedicineHistory takenHistory = new MedicineHistory(medicineId, scheduledTime, DoseStatus.TAKEN);
                        db.historyDao().insert(takenHistory);
                        notificationHelper.cancelNotification(medicineId);
                        
                        // Cancel any pending Nag/Snooze alarms since we took it
                        alarmScheduler.cancelNagAlarm(medicineId); 
                        
                        // Reschedule normal next dose
                        rescheduleAlarm(context, medicineId, alarmScheduler, db);
                    });
                    Toast.makeText(context, "Marked as taken", Toast.LENGTH_SHORT).show();
                    break;

                case Constants.ACTION_SNOOZE:
                    notificationHelper.cancelNotification(medicineId);
                    Medicine medicine = (Medicine) intent.getSerializableExtra(Constants.EXTRA_MEDICINE);
                    if (medicine != null) {
                        scheduleSnooze(context, medicine, scheduledTime);
                    }
                    Toast.makeText(context, "Snoozed for 10 minutes", Toast.LENGTH_SHORT).show();
                    break;

                case Constants.ACTION_DISMISS:
                    executor.execute(() -> {
                        MedicineHistory missedHistory = new MedicineHistory(medicineId, scheduledTime, DoseStatus.MISSED);
                        db.historyDao().insert(missedHistory);
                        notificationHelper.cancelNotification(medicineId);
                        
                        // Reschedule
                        rescheduleAlarm(context, medicineId, alarmScheduler, db);
                    });
                    break;
            }
        }
    }

    private void handleAlarmTrigger(Context context, Intent intent, NotificationHelper notificationHelper, AppDatabase db) {
        Medicine medicine = (Medicine) intent.getSerializableExtra(Constants.EXTRA_MEDICINE);
        long scheduledTime = intent.getLongExtra(Constants.EXTRA_SCHEDULED_TIME, System.currentTimeMillis());

        if (medicine != null) {
            Log.d(TAG, "Showing notification for: " + medicine.getName());
            notificationHelper.showMedicineNotification(medicine, scheduledTime);
        } else {
            long medicineId = intent.getLongExtra(Constants.EXTRA_MEDICINE_ID, -1);
            if (medicineId != -1) {
                executor.execute(() -> {
                    Medicine m = db.medicineDao().getMedicineById(medicineId);
                    if (m != null) {
                        notificationHelper.showMedicineNotification(m, scheduledTime);
                    }
                });
            }
        }
    }

    private void rescheduleAlarm(Context context, long medicineId, AlarmScheduler alarmScheduler, AppDatabase db) {
        Medicine medicine = db.medicineDao().getMedicineById(medicineId);
        if (medicine != null && medicine.isActive()) {
            alarmScheduler.scheduleAlarm(medicine);
        }
    }

    private void scheduleSnooze(Context context, Medicine medicine, long originalScheduledTime) {
        AlarmScheduler alarmScheduler = new AlarmScheduler(context);
        
        // Cancel current alarm
        alarmScheduler.cancelAlarm(medicine.getId());
        
        // Create temporary medicine with snooze time
        Medicine snoozeMedicine = new Medicine();
        snoozeMedicine.setId(medicine.getId());
        snoozeMedicine.setName(medicine.getName());
        snoozeMedicine.setDosage(medicine.getDosage());
        snoozeMedicine.setFrequency(medicine.getFrequency());
        
        // Calculate snooze time (10 minutes from now)
        long snoozeTime = System.currentTimeMillis() + (Constants.SNOOZE_DURATION_MINUTES * 60 * 1000);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(snoozeTime);
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = cal.get(java.util.Calendar.MINUTE);
        snoozeMedicine.setReminderTime(String.format("%02d:%02d", hour, minute));
        
        alarmScheduler.scheduleAlarm(snoozeMedicine);
    }
}
