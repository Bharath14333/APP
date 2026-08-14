package com.emergency.crisissense.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.emergency.crisissense.R;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button btnLoginCitizen = findViewById(R.id.btn_login_citizen);
        Button btnRegisterCitizen = findViewById(R.id.btn_register_citizen);
        TextView btnAdminPortal = findViewById(R.id.btn_admin_portal);

        btnLoginCitizen.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        });

        btnRegisterCitizen.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
        });

        btnAdminPortal.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, AdminLoginActivity.class));
        });
    }
}
