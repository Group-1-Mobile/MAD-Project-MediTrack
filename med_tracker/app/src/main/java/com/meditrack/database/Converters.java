package com.meditrack.database;

import androidx.room.TypeConverter;
import com.meditrack.models.DoseStatus;

public class Converters {
    @TypeConverter
    public static DoseStatus fromString(String value) {
        return value == null ? null : DoseStatus.valueOf(value);
    }

    @TypeConverter
    public static String statusToString(DoseStatus status) {
        return status == null ? null : status.name();
    }
}
