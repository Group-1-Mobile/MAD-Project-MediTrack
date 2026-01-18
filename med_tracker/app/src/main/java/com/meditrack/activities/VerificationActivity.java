package com.meditrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meditrack.R;

public class VerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Button btnVerify = findViewById(R.id.btn_verify);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        btnVerify.setOnClickListener(v -> {
            // Simulation: verification success
            Intent intent = new Intent(VerificationActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });
    }
}
