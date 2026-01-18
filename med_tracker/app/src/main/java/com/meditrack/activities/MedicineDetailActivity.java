package com.meditrack.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meditrack.R;
import com.meditrack.adapters.HistoryAdapter;
import com.meditrack.database.AppDatabase;
import com.meditrack.models.Medicine;
import com.meditrack.models.MedicineHistory;
import com.meditrack.utils.AlarmScheduler;
import com.meditrack.utils.Constants;
import com.meditrack.utils.DateTimeUtils;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MedicineDetailActivity extends AppCompatActivity {
    private TextView tvMedicineName;
    private TextView tvDosage;
    private TextView tvAdherenceRate;
    private RecyclerView recyclerViewHistory;
    private TextView tvNoHistory;
    
    private Medicine medicine;
    private AppDatabase db;
    private AlarmScheduler alarmScheduler;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this);
        alarmScheduler = new AlarmScheduler(this);

        initViews();
        
        long medicineId = getIntent().getLongExtra(Constants.EXTRA_MEDICINE_ID, -1);
        if (medicineId != -1) {
            executor.execute(() -> {
                medicine = db.medicineDao().getMedicineById(medicineId);
                runOnUiThread(() -> {
                    if (medicine != null) {
                        displayMedicineDetails();
                        loadHistory();
                    } else {
                        Toast.makeText(this, "Medicine not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            });
        } else {
            finish();
        }
    }

    private void initViews() {
        tvMedicineName = findViewById(R.id.tvMedicineName);
        tvDosage = findViewById(R.id.tvDosage);
        tvAdherenceRate = findViewById(R.id.tvAdherenceRate);
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        tvNoHistory = findViewById(R.id.tvNoHistory);
        
        historyAdapter = new HistoryAdapter();
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(historyAdapter);
    }

    private void displayMedicineDetails() {
        tvMedicineName.setText(medicine.getName());
        String dosageInfo = medicine.getDosage() + " • " + medicine.getType();
        tvDosage.setText(dosageInfo);
        
        // Populate reminders ChipGroup
        com.google.android.material.chip.ChipGroup chipGroupReminders = findViewById(R.id.chip_group_reminders);
        chipGroupReminders.removeAllViews();
        List<String> times = medicine.getReminderTimesList();
        for (String time : times) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
            chip.setText(DateTimeUtils.formatTime12(time));
            chip.setChipBackgroundColorResource(R.color.background_light);
            chip.setTextColor(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));
            chip.setChipStrokeWidth(1f);
            chip.setChipStrokeColorResource(R.color.primary);
            chipGroupReminders.addView(chip);
        }
        
        // Calculate adherence for last 30 days
        executor.execute(() -> {
            long endTime = System.currentTimeMillis();
            long startTime = endTime - (30 * Constants.MILLIS_IN_DAY);
            int total = db.historyDao().getTotalCountForMedicineInRange(medicine.getId(), startTime, endTime);
            int taken = db.historyDao().getTakenCountForMedicineInRange(medicine.getId(), startTime, endTime);
            float adherence = total > 0 ? (float) taken / total * 100 : 0;
            runOnUiThread(() -> tvAdherenceRate.setText(String.format("%.0f%%", adherence)));
        });
    }

    private void loadHistory() {
        executor.execute(() -> {
            List<MedicineHistory> historyList = db.historyDao().getHistoryByMedicineId(medicine.getId());
            runOnUiThread(() -> {
                if (historyList.isEmpty()) {
                    recyclerViewHistory.setVisibility(View.GONE);
                    tvNoHistory.setVisibility(View.VISIBLE);
                } else {
                    recyclerViewHistory.setVisibility(View.VISIBLE);
                    tvNoHistory.setVisibility(View.GONE);
                    historyAdapter.setHistory(historyList);
                }
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_medicine_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, AddEditMedicineActivity.class);
            intent.putExtra(Constants.EXTRA_MEDICINE_ID, medicine.getId());
            startActivity(intent);
            return true;
        } else if (id == R.id.action_delete) {
            showDeleteConfirmation();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_medicine)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteMedicine())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteMedicine() {
        executor.execute(() -> {
            // Cancel alarm
            alarmScheduler.cancelAlarm(medicine.getId());
            
            // Delete from database
            db.medicineDao().delete(medicine);
            
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.medicine_deleted, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (medicine != null) {
            executor.execute(() -> {
                medicine = db.medicineDao().getMedicineById(medicine.getId());
                runOnUiThread(() -> {
                    if (medicine != null) {
                        displayMedicineDetails();
                        loadHistory();
                    }
                });
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
