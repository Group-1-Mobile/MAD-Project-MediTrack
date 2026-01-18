package com.meditrack.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "medicine_history",
        foreignKeys = @ForeignKey(entity = Medicine.class,
                parentColumns = "id",
                childColumns = "medicineId",
                onDelete = ForeignKey.CASCADE))
public class MedicineHistory {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long medicineId;
    private long scheduledTime;
    private long actualTime;
    private DoseStatus status;
    private String notes;

    public MedicineHistory() {
    }

    public MedicineHistory(long medicineId, long scheduledTime, DoseStatus status) {
        this.medicineId = medicineId;
        this.scheduledTime = scheduledTime;
        this.status = status;
        this.actualTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(long medicineId) {
        this.medicineId = medicineId;
    }

    public long getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public long getActualTime() {
        return actualTime;
    }

    public void setActualTime(long actualTime) {
        this.actualTime = actualTime;
    }

    public DoseStatus getStatus() {
        return status;
    }

    public void setStatus(DoseStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "MedicineHistory{" +
                "id=" + id +
                ", medicineId=" + medicineId +
                ", scheduledTime=" + scheduledTime +
                ", actualTime=" + actualTime +
                ", status=" + status +
                ", notes='" + notes + '\'' +
                '}';
    }
}
