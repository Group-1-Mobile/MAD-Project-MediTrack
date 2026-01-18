package com.meditrack.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
    // time formats
    private static final String TIME_FORMAT_24 = "HH:mm";
    private static final String TIME_FORMAT_12 = "hh:mm a";
    private static final String DATE_FORMAT = "MMM dd, yyyy";
    private static final String DATETIME_FORMAT = "MMM dd, yyyy hh:mm a";

    // formats time to 24 hour
    public static String formatTime24(int h, int m) {
        return String.format(Locale.getDefault(), "%02d:%02d", h, m);
    }

    public static String formatTime12(String t) {
        try {
            // i think this works for 12 hours??
            SimpleDateFormat sdf24 = new SimpleDateFormat(TIME_FORMAT_24, Locale.getDefault());
            SimpleDateFormat sdf12 = new SimpleDateFormat(TIME_FORMAT_12, Locale.getDefault());
            Date d = sdf24.parse(t);
            if (d != null) {
                return sdf12.format(d);
            }
            return t; // return original if fail
        } catch (ParseException e) {
            return t;
        }
    }

    public static String formatDate(long ts) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date(ts));
    }

    public static String formatDateTime(long ts) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date(ts));
    }

    // gets the next alarm time from string
    public static long getNextAlarmTime(String val) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_24, Locale.getDefault());
            Date d = sdf.parse(val);
            if (d == null) return 0; // error

            Calendar c = Calendar.getInstance();
            Calendar rc = Calendar.getInstance();
            rc.setTime(d);

            c.set(Calendar.HOUR_OF_DAY, rc.get(Calendar.HOUR_OF_DAY));
            c.set(Calendar.MINUTE, rc.get(Calendar.MINUTE));
            c.set(Calendar.SECOND, 0);
            
            // if time passed, do tomorrow
            if (c.getTimeInMillis() <= System.currentTimeMillis()) {
                c.add(Calendar.DAY_OF_MONTH, 1);
            }

            return c.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getNextReminderTime(java.util.List<String> list) {
        if (list == null || list.isEmpty()) return "";
        
        long n = System.currentTimeMillis();
        long min = Long.MAX_VALUE;
        String res = list.get(0);
        
        for (String s : list) {
            long nt = getNextAlarmTime(s);
            if (nt < min) {
                min = nt;
                res = s;
            }
        }
        return res;
    }

    public static int[] parseTime(String s) {
        try {
            // split by colon
            String[] p = s.split(":");
            return new int[]{Integer.parseInt(p[0]), Integer.parseInt(p[1])};
        } catch (Exception e) {
            return new int[]{0, 0};
        }
    }
    
    // logic for relative time
    public static String getRelativeTime(long ts) {
        long n = System.currentTimeMillis();
        long d = n - ts;

        if (d < 60000) return "Just now";
        else if (d < 3600000) return (d/60000) + " min ago";
        else if (d < 86400000) return (d/3600000) + " hr ago";
        else return formatDate(ts);
    }
}
