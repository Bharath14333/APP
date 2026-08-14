package com.emergency.crisissense.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.emergency.crisissense.R;
import com.emergency.crisissense.util.SessionManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager sessionManager = new SessionManager(SplashActivity.this);
            Intent intent;
            if (sessionManager.isLoggedIn()) {
                if ("admin".equals(sessionManager.getUserRole())) {
                    intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                }
            } else {
                intent = new Intent(SplashActivity.this, IntroActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2500); // 2.5 seconds splash delay
    }
}
