package com.meditrack.activities;



import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.meditrack.R;
import com.meditrack.database.AppDatabase;
import com.meditrack.models.Medicine;
import com.meditrack.utils.AlarmScheduler;
import com.meditrack.utils.Constants;
import com.meditrack.utils.DateTimeUtils;

import java.util.Calendar;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddEditMedicineActivity extends AppCompatActivity {
    private com.google.android.material.textfield.TextInputEditText etMedicineName;
    private com.google.android.material.textfield.TextInputEditText etDosage;
    private com.google.android.material.textfield.TextInputEditText etAmount;
    private com.google.android.material.chip.ChipGroup chipGroupType;
    private LinearLayout layoutRemindersList;
    private com.google.android.material.materialswitch.MaterialSwitch switchAlarm;
    private Button btnSave;
    private ImageButton btnBack;
    
    private AppDatabase db;
    private AlarmScheduler alarmScheduler;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private Medicine editMedicine;
    private java.util.List<String> reminderTimes = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_medicine);

        db = AppDatabase.getInstance(this);
        alarmScheduler = new AlarmScheduler(this);

        initViews();
        
        long medicineId = getIntent().getLongExtra(Constants.EXTRA_MEDICINE_ID, -1);
        if (medicineId != -1) {
            executor.execute(() -> {
                editMedicine = db.medicineDao().getMedicineById(medicineId);
                runOnUiThread(() -> {
                    if (editMedicine != null) {
                        ((TextView)findViewById(R.id.tv_title)).setText("Edit Medicine");
                        populateFields();
                    }
                });
            });
        } else {
            // Default first reminder
            reminderTimes.add("08:00");
            updateRemindersUI();
        }

        findViewById(R.id.btn_add_reminder).setOnClickListener(v -> showTimePicker(-1));
        btnSave.setOnClickListener(v -> saveMedicine());
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        etMedicineName = findViewById(R.id.et_medicine_name);
        etDosage = findViewById(R.id.et_dosage);
        etAmount = findViewById(R.id.et_amount);
        chipGroupType = findViewById(R.id.chip_group_type);
        layoutRemindersList = findViewById(R.id.layout_reminders_list);
        switchAlarm = findViewById(R.id.switch_alarm);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);
    }

    private void populateFields() {
        etMedicineName.setText(editMedicine.getName());
        etDosage.setText(editMedicine.getDosage());
        etAmount.setText(editMedicine.getAmount());
        
        // Match chip type
        for (int i = 0; i < chipGroupType.getChildCount(); i++) {
            com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) chipGroupType.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(editMedicine.getType())) {
                chip.setChecked(true);
                break;
            }
        }
        
        reminderTimes = editMedicine.getReminderTimesList();
        updateRemindersUI();
        switchAlarm.setChecked(editMedicine.isActive());
    }

    private void updateRemindersUI() {
        layoutRemindersList.removeAllViews();
        for (int i = 0; i < reminderTimes.size(); i++) {
            final int index = i;
            String time = reminderTimes.get(i);
            View row = getLayoutInflater().inflate(R.layout.item_reminder_row, layoutRemindersList, false);
            
            TextView tvTime = row.findViewById(R.id.tv_time);
            tvTime.setText(DateTimeUtils.formatTime12(time));
            
            row.findViewById(R.id.btn_edit).setOnClickListener(v -> showTimePicker(index));
            row.findViewById(R.id.btn_remove).setOnClickListener(v -> {
                if (reminderTimes.size() > 1) {
                    reminderTimes.remove(index);
                    updateRemindersUI();
                } else {
                    Toast.makeText(this, "At least one reminder is required", Toast.LENGTH_SHORT).show();
                }
            });
            
            layoutRemindersList.addView(row);
        }
    }

    private void showTimePicker(final int index) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if (index != -1 && index < reminderTimes.size()) {
            int[] time = DateTimeUtils.parseTime(reminderTimes.get(index));
            hour = time[0];
            minute = time[1];
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minuteOfHour) -> {
                    String formattedTime = DateTimeUtils.formatTime24(hourOfDay, minuteOfHour);
                    if (index == -1) {
                        reminderTimes.add(formattedTime);
                    } else {
                        reminderTimes.set(index, formattedTime);
                    }
                    updateRemindersUI();
                },
                hour,
                minute,
                false
        );
        timePickerDialog.show();
    }

    private void saveMedicine() {
        String name = etMedicineName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String amount = etAmount.getText().toString().trim();
        
        int checkedChipId = chipGroupType.getCheckedChipId();
        String type = "Capsule"; // Default
        if (checkedChipId != View.NO_ID) {
            com.google.android.material.chip.Chip chip = findViewById(checkedChipId);
            type = chip.getText().toString();
        }
        
        boolean isAlarmEnabled = switchAlarm.isChecked();

        if (TextUtils.isEmpty(name)) {
            etMedicineName.setError(getString(R.string.error_empty_name));
            return;
        }

        if (TextUtils.isEmpty(dosage)) {
            etDosage.setError(getString(R.string.error_empty_dosage));
            return;
        }

        if (reminderTimes.isEmpty()) {
            Toast.makeText(this, R.string.error_select_time, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check for exact alarm permission
        if (isAlarmEnabled && !alarmScheduler.canScheduleExactAlarms()) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("To receive timely medication reminders, please allow setting exact alarms.")
                .setPositiveButton("Settings", (dialog, which) -> alarmScheduler.requestExactAlarmPermission())
                .setNegativeButton("Cancel", null)
                .show();
            return;
        }

        final String finalType = type;
        executor.execute(() -> {
            if (editMedicine != null) {
                editMedicine.setName(name);
                editMedicine.setType(finalType);
                editMedicine.setDosage(dosage);
                editMedicine.setAmount(amount);
                editMedicine.setReminderTimesList(reminderTimes);
                editMedicine.setActive(isAlarmEnabled);
                editMedicine.setUpdatedAt(System.currentTimeMillis());
                
                db.medicineDao().update(editMedicine);
                alarmScheduler.cancelAlarm(editMedicine.getId());
                if (isAlarmEnabled) {
                    alarmScheduler.scheduleAlarm(editMedicine);
                }
                runOnUiThread(() -> Toast.makeText(this, R.string.medicine_updated, Toast.LENGTH_SHORT).show());
            } else {
                Medicine medicine = new Medicine(name, finalType, dosage, amount, "Once Daily", "");
                medicine.setReminderTimesList(reminderTimes);
                medicine.setActive(isAlarmEnabled);
                long id = db.medicineDao().insert(medicine);
                medicine.setId(id);
                
                if (isAlarmEnabled) {
                    alarmScheduler.scheduleAlarm(medicine);
                }
                runOnUiThread(() -> Toast.makeText(this, R.string.medicine_added, Toast.LENGTH_SHORT).show());
            }
            runOnUiThread(this::finish);
        });
    }
}
