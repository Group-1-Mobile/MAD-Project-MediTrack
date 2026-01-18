package com.meditrack.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.meditrack.R;
import com.meditrack.database.AppDatabase;
import com.meditrack.database.StatusCount;
import com.meditrack.models.DoseStatus;
import com.meditrack.utils.Constants;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProgressReportActivity extends AppCompatActivity {
    private Spinner spinnerPeriod;
    private TextView tvOverallAdherence;
    private TextView tvDosesTaken;
    private TextView tvDosesMissed;
    
    private AppDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_report);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getInstance(this);

        initViews();
        setupPeriodSpinner();
        
        // Load default period (last 7 days)
        loadStatistics(0);
    }

    private void initViews() {
        spinnerPeriod = findViewById(R.id.spinnerPeriod);
        tvOverallAdherence = findViewById(R.id.tvOverallAdherence);
        tvDosesTaken = findViewById(R.id.tvDosesTaken);
        tvDosesMissed = findViewById(R.id.tvDosesMissed);
    }

    private void setupPeriodSpinner() {
        String[] periods = {
                getString(R.string.last_7_days),
                getString(R.string.last_30_days),
                getString(R.string.this_month)
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                periods
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(adapter);
        
        spinnerPeriod.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, 
                                       int position, long id) {
                loadStatistics(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void loadStatistics(int periodIndex) {
        long endTime = System.currentTimeMillis();
        long startTime;
        
        switch (periodIndex) {
            case 0: // Last 7 days
                startTime = endTime - Constants.MILLIS_IN_WEEK;
                break;
            case 1: // Last 30 days
                startTime = endTime - Constants.MILLIS_IN_MONTH;
                break;
            case 2: // This month
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                startTime = cal.getTimeInMillis();
                break;
            default:
                startTime = endTime - Constants.MILLIS_IN_WEEK;
        }
        
        executor.execute(() -> {
            List<StatusCount> stats = db.historyDao().getStatusCountsInRange(startTime, endTime);
            
            int taken = 0;
            int missed = 0;
            int skipped = 0;

            for (StatusCount sc : stats) {
                if (DoseStatus.TAKEN.name().equals(sc.status)) taken = sc.count;
                else if (DoseStatus.MISSED.name().equals(sc.status)) missed = sc.count;
                else if (DoseStatus.SKIPPED.name().equals(sc.status)) skipped = sc.count;
            }

            int total = taken + missed + skipped;
            float adherence = total > 0 ? ((float) taken / total * 100) : 0;
            
            final int finalTaken = taken;
            final int finalMissed = missed;
            runOnUiThread(() -> {
                tvOverallAdherence.setText(String.format("%.0f%%", adherence));
                tvDosesTaken.setText(String.valueOf(finalTaken));
                tvDosesMissed.setText(String.valueOf(finalMissed));
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
