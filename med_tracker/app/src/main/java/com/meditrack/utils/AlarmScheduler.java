package com.meditrack.utils;



import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.meditrack.database.AppDatabase;
import com.meditrack.models.Medicine;
import com.meditrack.receivers.AlarmReceiver;

public class AlarmScheduler {
    private static final String TAG = "AlarmScheduler";
    Context context;
    AlarmManager alarmManager;

    public AlarmScheduler(Context ctx) {
        this.context = ctx.getApplicationContext();
        this.alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
    }

    // Schedule alarm
    public void scheduleAlarm(Medicine m) {
        if (!canScheduleExactAlarms()) {
            return; // no permission
        }

        java.util.List<String> list = m.getReminderTimesList();
        for (int i = 0; i < list.size(); i++) {
            String t = list.get(i);
            long time = DateTimeUtils.getNextAlarmTime(t);
            if (time == 0) continue;

            Intent i2 = new Intent(context, AlarmReceiver.class);
            i2.putExtra(Constants.EXTRA_MEDICINE_ID, m.getId());
            i2.putExtra(Constants.EXTRA_MEDICINE, m);
            i2.putExtra(Constants.EXTRA_SCHEDULED_TIME, time);

            PendingIntent pi = PendingIntent.getBroadcast(
                    context,
                    getCode(m.getId(), i),
                    i2,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pi);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Cancel alarms
    public void cancelAlarm(long id) {
        // trying to cancel 10 just in case
        for (int i = 0; i < 10; i++) {
            Intent intent = new Intent(context, AlarmReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(
                    context,
                    getCode(id, i),
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (pi != null) {
                alarmManager.cancel(pi);
                pi.cancel();
            }
        }
    }

    // Reschedule all
    public void rescheduleAllAlarms(Context c) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(c);
            java.util.List<Medicine> list = db.medicineDao().getAllActiveMedicines();
            for (Medicine m : list) {
                scheduleAlarm(m);
            }
        }).start();
    }

    public boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= 31) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    public void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= 31) {
            Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    public void scheduleNagAlarm(Medicine m) {
        scheduleRecurringAlarm(m, 5); 
    }
    
    public void scheduleSnoozeAlarm(Medicine m, int mins) {
        cancelNagAlarm(m.getId());
        scheduleRecurringAlarm(m, mins);
    }
    
    private void scheduleRecurringAlarm(Medicine m, int mins) {
        if (!canScheduleExactAlarms()) return;

        long time = System.currentTimeMillis() + (mins * 60 * 1000);
        Intent i = new Intent(context, AlarmReceiver.class);
        i.putExtra(Constants.EXTRA_MEDICINE_ID, m.getId());
        i.putExtra(Constants.EXTRA_MEDICINE, m);
        i.putExtra(Constants.EXTRA_SCHEDULED_TIME, time);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                getCode(m.getId(), 999), 
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= 23) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pi);
            }
        } catch (Exception e) {
             e.printStackTrace();
        }
    }

    public void cancelNagAlarm(long id) {
        Intent i = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                getCode(id, 999),
                i,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pi != null) {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    private int getCode(long id, int idx) {
        return Constants.REQUEST_CODE_ALARM_BASE + (int) (id * 100) + idx; 
    }
}
