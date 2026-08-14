package com.emergency.crisissense.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.emergency.crisissense.R;
import com.emergency.crisissense.model.Incident;
import com.emergency.crisissense.model.Notification;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;
import java.util.UUID;

public class SosActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST = 101;
    private static final long HOLD_DURATION_MS = 3000; // 3 seconds

    private ProgressBar progressBar;
    private MaterialCardView btnTrigger;
    private TextView txtCountdown;
    private View layoutTriggered;
    private TextView txtCoordinates;
    private Button btnCallPolice, btnCallAmbulance, btnViewNearby;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    private Handler handler;
    private Runnable progressRunnable;
    private long holdStartTime = 0;
    private boolean isTriggered = false;
    private double userLatitude = 34.0522;
    private double userLongitude = -118.2437;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        progressBar = findViewById(R.id.sos_progress);
        btnTrigger = findViewById(R.id.btn_sos_trigger);
        txtCountdown = findViewById(R.id.txt_sos_countdown);
        layoutTriggered = findViewById(R.id.layout_sos_triggered);
        txtCoordinates = findViewById(R.id.txt_sos_coordinates);
        btnCallPolice = findViewById(R.id.btn_sos_call_police);
        btnCallAmbulance = findViewById(R.id.btn_sos_call_ambulance);
        btnViewNearby = findViewById(R.id.btn_sos_view_services);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        firebaseHelper = new FirebaseHelper();
        sessionManager = new SessionManager(this);
        handler = new Handler();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        setupHoldListener();

        btnCallPolice.setOnClickListener(v -> dialNumber("112"));
        btnCallAmbulance.setOnClickListener(v -> dialNumber("108"));
        btnViewNearby.setOnClickListener(v -> {
            Intent intent = new Intent(SosActivity.this, NearbyServicesActivity.class);
            startActivity(intent);
            finish();
        });

        // Request location upfront
        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            detectUserLocation();
        }
    }

    private void detectUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                userLatitude = location.getLatitude();
                userLongitude = location.getLongitude();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            detectUserLocation();
        }
    }

    private void setupHoldListener() {
        btnTrigger.setOnTouchListener((v, event) -> {
            if (isTriggered) return false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    holdStartTime = System.currentTimeMillis();
                    startHoldCountdown();
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    cancelHoldCountdown();
                    return true;
            }
            return false;
        });
    }

    private void startHoldCountdown() {
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - holdStartTime;
                int progress = (int) ((elapsed * 100) / HOLD_DURATION_MS);
                
                if (progress >= 100) {
                    progressBar.setProgress(100);
                    triggerSos();
                } else {
                    progressBar.setProgress(progress);
                    int remainingSec = 3 - (int)(elapsed / 1000);
                    txtCountdown.setText(String.format(Locale.getDefault(), "%ds", remainingSec));
                    handler.postDelayed(this, 50); // check every 50ms
                }
            }
        };
        handler.post(progressRunnable);
    }

    private void cancelHoldCountdown() {
        handler.removeCallbacks(progressRunnable);
        progressBar.setProgress(0);
        txtCountdown.setText("HOLD");
    }

    private void triggerSos() {
        isTriggered = true;
        btnTrigger.setCardBackgroundColor(getResources().getColor(R.color.medium_gray));
        txtCountdown.setText("SENT");
        
        Toast.makeText(this, "🚨 SOS Emergency Activated!", Toast.LENGTH_LONG).show();

        // Retrieve final location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    userLatitude = location.getLatitude();
                    userLongitude = location.getLongitude();
                }
                saveSosIncidentToFirestore();
            }).addOnFailureListener(e -> saveSosIncidentToFirestore());
        } else {
            saveSosIncidentToFirestore();
        }
    }

    private void saveSosIncidentToFirestore() {
        String incidentId = UUID.randomUUID().toString();
        String userId = sessionManager.getUserId() != null ? sessionManager.getUserId() : "sos_offline_user";
        String userName = sessionManager.getUserDetails() != null ? sessionManager.getUserDetails().getName() : "SOS Citizen";
        
        txtCoordinates.setText(String.format(Locale.getDefault(), "Lat: %.6f, Lng: %.6f", userLatitude, userLongitude));
        layoutTriggered.setVisibility(View.VISIBLE);

        Incident incident = new Incident(
            incidentId, userId, userName, "SOS ALERT", "Other", 
            "SOS Emergency Signal activated by holding button.", 
            "User coordinates: " + userLatitude + ", " + userLongitude, 
            userLatitude + "," + userLongitude, "", "pending", "critical"
        );

        // Save incident
        firebaseHelper.reportIncident(incident, task -> {
            if (task.isSuccessful()) {
                firebaseHelper.addSystemLog("SOS_ACTIVATED", "SOS Panic Alert Activated", 
                    sessionManager.getUserDetails() != null ? sessionManager.getUserDetails().getEmail() : "Guest");

                // Trigger FCM alert broadcast notification
                String notifyId = UUID.randomUUID().toString();
                Notification notification = new Notification(
                    notifyId, "all", "EMERGENCY PANIC SOS", 
                    userName + " has triggered an SOS panic signal at " + userLatitude + ", " + userLongitude
                );
                firebaseHelper.sendNotification(notification, task1 -> {});
            } else {
                // Offline demo bypass
                firebaseHelper.addSystemLog("SOS_ACTIVATED", "SOS Panic Alert Activated (Demo Mode)", 
                    sessionManager.getUserDetails() != null ? sessionManager.getUserDetails().getEmail() : "Guest");
            }
        });
    }

    private void dialNumber(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone));
        startActivity(intent);

        firebaseHelper.addSystemLog("EMERGENCY_CALL_INITIATED", "Dialed phone number from SOS: " + phone, 
            sessionManager.getUserDetails() != null ? sessionManager.getUserDetails().getEmail() : "Guest");
    }
}
