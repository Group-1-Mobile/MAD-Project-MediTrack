package com.meditrack.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.meditrack.models.MedicineHistory;

import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    long insert(MedicineHistory history);

    @Update
    void update(MedicineHistory history);

    @Delete
    void delete(MedicineHistory history);

    @Query("SELECT * FROM medicine_history WHERE medicineId = :medicineId ORDER BY scheduledTime DESC")
    List<MedicineHistory> getHistoryByMedicineId(long medicineId);

    @Query("SELECT * FROM medicine_history WHERE scheduledTime >= :startTime AND scheduledTime <= :endTime")
    List<MedicineHistory> getHistoryForRange(long startTime, long endTime);

    @Query("SELECT COUNT(*) FROM medicine_history WHERE medicineId = :medicineId AND scheduledTime >= :startTime AND scheduledTime <= :endTime")
    int getTotalCountForMedicineInRange(long medicineId, long startTime, long endTime);

    @Query("SELECT COUNT(*) FROM medicine_history WHERE medicineId = :medicineId AND status = 'TAKEN' AND scheduledTime >= :startTime AND scheduledTime <= :endTime")
    int getTakenCountForMedicineInRange(long medicineId, long startTime, long endTime);

    @Query("SELECT COUNT(*) FROM medicine_history WHERE status = 'TAKEN' AND scheduledTime >= :startTime AND scheduledTime <= :endTime")
    int getTakenCountInRange(long startTime, long endTime);

    @Query("SELECT status, COUNT(*) as count FROM medicine_history WHERE scheduledTime >= :startTime AND scheduledTime <= :endTime GROUP BY status")
    List<StatusCount> getStatusCountsInRange(long startTime, long endTime);

    @Query("SELECT * FROM medicine_history ORDER BY scheduledTime DESC")
    List<MedicineHistory> getAllHistory();
}
