package com.meditrack.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsHelper {
    private static SharedPrefsHelper instance;
    private final SharedPreferences prefs;

    private SharedPrefsHelper(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsHelper(context);
        }
        return instance;
    }

    public boolean isFirstLaunch() {
        return prefs.getBoolean(Constants.PREF_FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean isFirst) {
        prefs.edit().putBoolean(Constants.PREF_FIRST_LAUNCH, isFirst).apply();
    }

    public boolean areNotificationsEnabled() {
        return prefs.getBoolean(Constants.PREF_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(Constants.PREF_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("is_logged_in", false);
    }

    public void setLoggedIn(boolean loggedIn) {
        prefs.edit().putBoolean("is_logged_in", loggedIn).apply();
    }

    public String getUserName() {
        return prefs.getString("user_name", "User");
    }

    public void setUserName(String name) {
        prefs.edit().putString("user_name", name).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
