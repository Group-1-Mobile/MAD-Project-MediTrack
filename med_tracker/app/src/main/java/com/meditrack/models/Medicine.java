package com.meditrack.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "medicines")
public class Medicine implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String type;
    private String dosage;
    private String amount;
    private String frequency;
    private String reminderTime; // Format: HH:mm (can be comma-separated)
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    public Medicine() {
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Medicine(String name, String type, String dosage, String amount, String frequency, String reminderTime) {
        this();
        this.name = name;
        this.type = type;
        this.dosage = dosage;
        this.amount = amount;
        this.frequency = frequency;
        this.reminderTime = reminderTime;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    // Helper methods for multiple reminders
    public java.util.List<String> getReminderTimesList() {
        if (reminderTime == null || reminderTime.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return new java.util.ArrayList<>(java.util.Arrays.asList(reminderTime.split(",")));
    }

    public void setReminderTimesList(java.util.List<String> times) {
        if (times == null || times.isEmpty()) {
            this.reminderTime = "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < times.size(); i++) {
                sb.append(times.get(i));
                if (i < times.size() - 1) {
                    sb.append(",");
                }
            }
            this.reminderTime = sb.toString();
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Medicine{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dosage='" + dosage + '\'' +
                ", frequency='" + frequency + '\'' +
                ", reminderTime='" + reminderTime + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
