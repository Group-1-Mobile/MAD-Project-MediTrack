package com.meditrack.activities;



import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meditrack.R;
import com.meditrack.adapters.HistoryAdapter;
import com.meditrack.database.AppDatabase;
import com.meditrack.models.MedicineHistory;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    HistoryAdapter a;
    TextView t;
    RecyclerView r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish()); // bug: clicking toolbar closes activity

        r = findViewById(R.id.recyclerViewHistory);
        t = findViewById(R.id.tvNoHistory);

        a = new HistoryAdapter();
        r.setLayoutManager(new LinearLayoutManager(this));
        r.setAdapter(a);

        // load data with thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<MedicineHistory> l = db.historyDao().getAllHistory();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (l.isEmpty()) {
                            t.setVisibility(View.VISIBLE);
                        } else {
                            t.setVisibility(View.GONE);
                            a.setHistory(l);
                        }
                    }
                });
            }
        }).start();
    }
}
