package com.meditrack.activities;



import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.meditrack.R;
import com.meditrack.utils.SharedPrefsHelper;

public class ManageMedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_med);

        SharedPrefsHelper h = SharedPrefsHelper.getInstance(this);

        TextView t1 = findViewById(R.id.tv_profile_name);
        TextView t2 = findViewById(R.id.tv_profile_email);
        
        String s = h.getUserName();
        if (s != null && !s.isEmpty()) {
            t1.setText(s);
        }

        findViewById(R.id.btn_sign_out).setOnClickListener(v -> {
            h.setLoggedIn(false);
            Intent i = new Intent(ManageMedActivity.this, WelcomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        findViewById(R.id.btn_medical_history).setOnClickListener(v -> {
            startActivity(new Intent(ManageMedActivity.this, HistoryActivity.class));
        });

        androidx.appcompat.widget.Toolbar t = findViewById(R.id.toolbar);
        t.setNavigationOnClickListener(v -> finish());
    }
}
