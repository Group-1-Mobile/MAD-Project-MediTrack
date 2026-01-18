package com.meditrack.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.meditrack.R;
import com.meditrack.activities.MainActivity;
import com.meditrack.models.Medicine;
import com.meditrack.receivers.AlarmReceiver;

public class NotificationHelper {
    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) 
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    // Create notification channel (required for Android O+)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .build();

            NotificationChannel channel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    Constants.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(context.getString(R.string.notification_channel_desc));
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            channel.enableLights(true);
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Show notification for medicine reminder
    public void showMedicineNotification(Medicine medicine, long scheduledTime) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.EXTRA_MEDICINE_ID, medicine.getId());

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) medicine.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action: Mark as Taken
        Intent takeIntent = new Intent(context, AlarmReceiver.class);
        takeIntent.setAction(Constants.ACTION_TAKE);
        takeIntent.putExtra(Constants.EXTRA_MEDICINE_ID, medicine.getId());
        takeIntent.putExtra(Constants.EXTRA_SCHEDULED_TIME, scheduledTime);
        PendingIntent takePendingIntent = PendingIntent.getBroadcast(
                context,
                (int) (medicine.getId() * 10 + 1),
                takeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action: Snooze
        Intent snoozeIntent = new Intent(context, AlarmReceiver.class);
        snoozeIntent.setAction(Constants.ACTION_SNOOZE);
        snoozeIntent.putExtra(Constants.EXTRA_MEDICINE_ID, medicine.getId());
        snoozeIntent.putExtra(Constants.EXTRA_MEDICINE, medicine);
        snoozeIntent.putExtra(Constants.EXTRA_SCHEDULED_TIME, scheduledTime);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                (int) (medicine.getId() * 10 + 2),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action: Dismiss
        Intent dismissIntent = new Intent(context, AlarmReceiver.class);
        dismissIntent.setAction(Constants.ACTION_DISMISS);
        dismissIntent.putExtra(Constants.EXTRA_MEDICINE_ID, medicine.getId());
        dismissIntent.putExtra(Constants.EXTRA_SCHEDULED_TIME, scheduledTime);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                (int) (medicine.getId() * 10 + 3),
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        Intent fullScreenIntent = new Intent(context, com.meditrack.activities.AlertActivity.class);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        fullScreenIntent.putExtra(Constants.EXTRA_MEDICINE, medicine);
        fullScreenIntent.putExtra(Constants.EXTRA_SCHEDULED_TIME, scheduledTime);
        
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                (int) (medicine.getId() * 10 + 4), // Unique request code
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_text, 
                        medicine.getName(), medicine.getDosage()))
                .setPriority(NotificationCompat.PRIORITY_MAX) // Increased priority
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setFullScreenIntent(fullScreenPendingIntent, true) // CRITICAL: This wakes the lock screen
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_check, context.getString(R.string.take), takePendingIntent)
                .addAction(R.drawable.ic_snooze, context.getString(R.string.snooze), snoozePendingIntent)
                .addAction(R.drawable.ic_close, context.getString(R.string.dismiss), dismissPendingIntent);

        notificationManager.notify(getNotificationId(medicine.getId()), builder.build());
    }

    // Cancel notification
    public void cancelNotification(long medicineId) {
        notificationManager.cancel(getNotificationId(medicineId));
    }

    // Generate unique notification ID
    private int getNotificationId(long medicineId) {
        return Constants.NOTIFICATION_ID_BASE + (int) medicineId;
    }
}
