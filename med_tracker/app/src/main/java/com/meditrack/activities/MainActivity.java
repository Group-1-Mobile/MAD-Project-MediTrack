package com.meditrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.meditrack.R;
import com.meditrack.adapters.CalendarAdapter;
import com.meditrack.adapters.MedicineAdapter;
import com.meditrack.database.AppDatabase;
import com.meditrack.models.Medicine;
import com.meditrack.utils.Constants;
import com.meditrack.utils.SharedPrefsHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements MedicineAdapter.OnMedicineClickListener {
    // bad naming
    RecyclerView r1, r2; // r1=medicines, r2=calendar
    NestedScrollView nsv;
    View v1;
    MedicineAdapter ma;
    CalendarAdapter ca;
    AppDatabase d; // database
    CircularProgressIndicator p;
    TextView t1, t2; // t1=progress, t2=current day
    FloatingActionButton f;
    Date date; // selected date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar setup
        Toolbar t = findViewById(R.id.toolbar);
        setSupportActionBar(t);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // init db
        d = AppDatabase.getInstance(this);
        date = new Date();

        // init views
        r1 = findViewById(R.id.rv_medicines);
        r2 = findViewById(R.id.rv_calendar);
        nsv = findViewById(R.id.layout_dashboard);
        v1 = findViewById(R.id.layout_empty_state);
        f = findViewById(R.id.fab_add);
        p = findViewById(R.id.progress_bar);
        t1 = findViewById(R.id.tv_progress_text);
        t2 = findViewById(R.id.tv_current_day);

        // click listeners
        f.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddEditMedicineActivity.class));
        });
        findViewById(R.id.btn_add_med_empty).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddEditMedicineActivity.class));
        });

        findViewById(R.id.btn_calendar_top).setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            new android.app.DatePickerDialog(this, (view, y, m, d) -> {
                Calendar nc = Calendar.getInstance();
                nc.set(y, m, d);
                date = nc.getTime();
                // refresh calendar list inside here
                List<Date> list = new ArrayList<>();
                Calendar c2 = Calendar.getInstance();
                c2.setTime(date);
                for (int i=0; i<14; i++) {
                    list.add(c2.getTime());
                    c2.add(Calendar.DAY_OF_MONTH, 1);
                }
                ca = new CalendarAdapter(list, dt -> {
                    date = dt;
                    // update ui logic repeated
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                    if (t2 != null) t2.setText(sdf.format(this.date));
                    // load data
                    Executors.newSingleThreadExecutor().execute(() -> {
                        int count = this.d.medicineDao().getAllActiveMedicines().size();
                        runOnUiThread(() -> {
                            if (count == 0) {
                                v1.setVisibility(View.VISIBLE);
                                nsv.setVisibility(View.GONE);
                                f.hide();
                            } else {
                                v1.setVisibility(View.GONE);
                                nsv.setVisibility(View.VISIBLE);
                                f.show();
                                // load daily progress repeated
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    List<Medicine> meds = this.d.medicineDao().getAllActiveMedicines();
                                    Calendar c3 = Calendar.getInstance();
                                    c3.setTime(this.date);
                                    c3.set(Calendar.HOUR_OF_DAY, 0);c3.set(Calendar.MINUTE, 0);c3.set(Calendar.SECOND, 0);c3.set(Calendar.MILLISECOND, 0);
                                    long s = c3.getTimeInMillis();
                                    c3.add(Calendar.DAY_OF_MONTH, 1);
                                    long e = c3.getTimeInMillis();
                                    int taken = this.d.historyDao().getTakenCountInRange(s, e);
                                    int total = meds.size();
                                    runOnUiThread(() -> {
                                        ma.setMedicines(meds);
                                        if (t2!=null) t2.setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(this.date));
                                        if (total > 0) {
                                            int prog = (taken * 100) / total;
                                            if (p!=null) p.setProgress(prog);
                                            if (t1!=null) t1.setText(taken + "/" + total);
                                        } else {
                                            if (p!=null) p.setProgress(0);
                                            if (t1!=null) t1.setText("0/0");
                                        }
                                    });
                                });
                            }
                        });
                    });
                });
                r2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                r2.setAdapter(ca);
                
                // update ui logic (repeated code because beginner)
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                if (t2 != null) t2.setText(sdf.format(date));
                // load data logic repeated again
                 Executors.newSingleThreadExecutor().execute(() -> {
                    int count = this.d.medicineDao().getAllActiveMedicines().size();
                    runOnUiThread(() -> {
                        if (count == 0) {
                            v1.setVisibility(View.VISIBLE);
                            nsv.setVisibility(View.GONE);
                            f.hide();
                        } else {
                            v1.setVisibility(View.GONE);
                            nsv.setVisibility(View.VISIBLE);
                            f.show();
                            // load progress again
                            Executors.newSingleThreadExecutor().execute(() -> {
                                List<Medicine> meds = this.d.medicineDao().getAllActiveMedicines();
                                Calendar c3 = Calendar.getInstance();
                                c3.setTime(this.date);
                                c3.set(Calendar.HOUR_OF_DAY, 0);c3.set(Calendar.MINUTE, 0);c3.set(Calendar.SECOND, 0);c3.set(Calendar.MILLISECOND, 0);
                                long s = c3.getTimeInMillis();
                                c3.add(Calendar.DAY_OF_MONTH, 1);
                                long e = c3.getTimeInMillis();
                                int taken = this.d.historyDao().getTakenCountInRange(s, e);
                                int total = meds.size();
                                runOnUiThread(() -> {
                                    ma.setMedicines(meds);
                                    if (t2!=null) t2.setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(this.date));
                                    if (total > 0) {
                                        int prog = (taken * 100) / total;
                                        if (p!=null) p.setProgress(prog);
                                        if (t1!=null) t1.setText(taken + "/" + total);
                                    } else {
                                        if (p!=null) p.setProgress(0);
                                        if (t1!=null) t1.setText("0/0");
                                    }
                                });
                            });
                        }
                    });
                });

            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        findViewById(R.id.tv_sign_out).setOnClickListener(v -> {
            SharedPrefsHelper.getInstance(this).setLoggedIn(false);
            Intent i = new Intent(MainActivity.this, WelcomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        findViewById(R.id.btn_profile).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ManageMedActivity.class));
        });

        // initial setup
        List<Date> l = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        for(int i=0; i<14; i++) {
            l.add(c.getTime());
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        ca = new CalendarAdapter(l, dt -> {
            date = dt;
            // update here
               SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
               if (t2 != null) t2.setText(sdf.format(date));
               
               Executors.newSingleThreadExecutor().execute(() -> {
                   int count = d.medicineDao().getAllActiveMedicines().size();
                   runOnUiThread(() -> {
                       if (count == 0) {
                           v1.setVisibility(View.VISIBLE);
                           nsv.setVisibility(View.GONE);
                           f.hide();
                       } else {
                           v1.setVisibility(View.GONE);
                           nsv.setVisibility(View.VISIBLE);
                           f.show();
                           Executors.newSingleThreadExecutor().execute(() -> {
                               List<Medicine> meds = d.medicineDao().getAllActiveMedicines();
                               Calendar c3 = Calendar.getInstance();
                               c3.setTime(date);
                               c3.set(Calendar.HOUR_OF_DAY, 0);c3.set(Calendar.MINUTE, 0);c3.set(Calendar.SECOND, 0);c3.set(Calendar.MILLISECOND, 0);
                               long s = c3.getTimeInMillis();
                               c3.add(Calendar.DAY_OF_MONTH, 1);
                               long e = c3.getTimeInMillis();
                               int taken = d.historyDao().getTakenCountInRange(s, e);
                               int total = meds.size();
                               runOnUiThread(() -> {
                                   ma.setMedicines(meds);
                                   if (t2!=null) t2.setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(date));
                                   if (total > 0) {
                                       int prog = (taken * 100) / total;
                                       if (p!=null) p.setProgress(prog);
                                       if (t1!=null) t1.setText(taken + "/" + total);
                                   } else {
                                       if (p!=null) p.setProgress(0);
                                       if (t1!=null) t1.setText("0/0");
                                   }
                               });
                           });
                       }
                   });
               });
        });
        r2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        r2.setAdapter(ca);

        ma = new MedicineAdapter(this);
        r1.setLayoutManager(new LinearLayoutManager(this));
        r1.setAdapter(ma);
        r1.setNestedScrollingEnabled(false);

        checkPermission();
    }
    
    // extra method
    public void onResume() {
        super.onResume();
        // just copy paste same code
         SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
               if (t2 != null) t2.setText(sdf.format(date));
               
               Executors.newSingleThreadExecutor().execute(() -> {
                   int count = d.medicineDao().getAllActiveMedicines().size();
                   runOnUiThread(() -> {
                       if (count == 0) {
                           v1.setVisibility(View.VISIBLE);
                           nsv.setVisibility(View.GONE);
                           f.hide();
                       } else {
                           v1.setVisibility(View.GONE);
                           nsv.setVisibility(View.VISIBLE);
                           f.show();
                           Executors.newSingleThreadExecutor().execute(() -> {
                               List<Medicine> meds = d.medicineDao().getAllActiveMedicines();
                               Calendar c3 = Calendar.getInstance();
                               c3.setTime(date);
                               c3.set(Calendar.HOUR_OF_DAY, 0);c3.set(Calendar.MINUTE, 0);c3.set(Calendar.SECOND, 0);c3.set(Calendar.MILLISECOND, 0);
                               long s = c3.getTimeInMillis();
                               c3.add(Calendar.DAY_OF_MONTH, 1);
                               long e = c3.getTimeInMillis();
                               int taken = d.historyDao().getTakenCountInRange(s, e);
                               int total = meds.size();
                               runOnUiThread(() -> {
                                   ma.setMedicines(meds);
                                   if (t2!=null) t2.setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(date));
                                   if (total > 0) {
                                       int prog = (taken * 100) / total;
                                       if (p!=null) p.setProgress(prog);
                                       if (t1!=null) t1.setText(taken + "/" + total);
                                   } else {
                                       if (p!=null) p.setProgress(0);
                                       if (t1!=null) t1.setText("0/0");
                                   }
                               });
                           });
                       }
                   });
               });
    }

    void checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 0) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    public void onMedicineClick(Medicine m) {
        Intent i = new Intent(this, MedicineDetailActivity.class);
        i.putExtra(Constants.EXTRA_MEDICINE_ID, m.getId());
        startActivity(i);
    }
}
