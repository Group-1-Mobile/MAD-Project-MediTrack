package com.meditrack.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.meditrack.models.Medicine;
import com.meditrack.models.MedicineHistory;
import com.meditrack.models.User;

@Database(entities = {User.class, Medicine.class, MedicineHistory.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract MedicineDao medicineDao();
    public abstract HistoryDao historyDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "mediminder_database")
                    .fallbackToDestructiveMigration() // For development, will clear data on version change
                    .build();
        }
        return instance;
    }
}
