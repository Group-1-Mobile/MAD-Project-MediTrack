package com.meditrack.utils;

public class Constants {
    // Database
    public static final String DATABASE_NAME = "meditrack.db";
    public static final int DATABASE_VERSION = 2;

    // Tables
    public static final String TABLE_USERS = "users";
    public static final String TABLE_MEDICINES = "medicines";
    public static final String TABLE_HISTORY = "medicine_history";

    // User Table Columns
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";

    // Medicine Table Columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_DOSAGE = "dosage";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_FREQUENCY = "frequency";
    public static final String COLUMN_REMINDER_TIME = "reminder_time";
    public static final String COLUMN_IS_ACTIVE = "is_active";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    // History Table Columns
    public static final String COLUMN_MEDICINE_ID = "medicine_id";
    public static final String COLUMN_SCHEDULED_TIME = "scheduled_time";
    public static final String COLUMN_ACTUAL_TIME = "actual_time";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_NOTES = "notes";

    // SharedPreferences
    public static final String PREFS_NAME = "MediTrackPrefs";
    public static final String PREF_FIRST_LAUNCH = "first_launch";
    public static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";

    // Notification
    public static final String NOTIFICATION_CHANNEL_ID = "medicine_reminders_v2";
    public static final String NOTIFICATION_CHANNEL_NAME = "Medicine Reminders";
    public static final int NOTIFICATION_ID_BASE = 1000;

    // Intent Extras
    public static final String EXTRA_MEDICINE_ID = "medicine_id";
    public static final String EXTRA_MEDICINE = "medicine";
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_SCHEDULED_TIME = "scheduled_time";

    // Actions
    public static final String ACTION_TAKE = "action_take";
    public static final String ACTION_SNOOZE = "action_snooze";
    public static final String ACTION_DISMISS = "action_dismiss";

    // Request Codes
    public static final int REQUEST_CODE_ALARM_BASE = 2000;
    public static final int REQUEST_CODE_NOTIFICATION = 3000;

    // Time
    public static final int SNOOZE_DURATION_MINUTES = 10;
    public static final long MILLIS_IN_DAY = 24 * 60 * 60 * 1000L;
    public static final long MILLIS_IN_WEEK = 7 * MILLIS_IN_DAY;
    public static final long MILLIS_IN_MONTH = 30 * MILLIS_IN_DAY;

    private Constants() {
        // Prevent instantiation
    }
}
