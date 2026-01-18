package com.meditrack.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.meditrack.models.Medicine;

import java.util.List;

@Dao
public interface MedicineDao {
    @Insert
    long insert(Medicine medicine);

    @Update
    void update(Medicine medicine);

    @Delete
    void delete(Medicine medicine);

    @Query("SELECT * FROM medicines WHERE id = :id")
    Medicine getMedicineById(long id);

    @Query("SELECT * FROM medicines WHERE isActive = 1 ORDER BY createdAt DESC")
    List<Medicine> getAllActiveMedicines();

    @Query("SELECT * FROM medicines ORDER BY createdAt DESC")
    List<Medicine> getAllMedicines();
}
