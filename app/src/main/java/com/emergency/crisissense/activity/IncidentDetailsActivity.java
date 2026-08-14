package com.emergency.crisissense.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.Incident;
import com.emergency.crisissense.model.Notification;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class IncidentDetailsActivity extends AppCompatActivity {
    private TextView txtTitle, txtStatus, txtType, txtSeverity, txtDate, txtDesc, txtLocation, txtReporter;
    private ImageView imgEvidence;
    private LinearLayout layoutAdmin;
    private Spinner spinnerStatus;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private Incident incident;

    private final String[] statusChoices = {"pending", "approved", "rejected", "resolved"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        txtTitle = findViewById(R.id.txt_details_title);
        txtStatus = findViewById(R.id.txt_details_status);
        txtType = findViewById(R.id.txt_details_type);
        txtSeverity = findViewById(R.id.txt_details_severity);
        txtDate = findViewById(R.id.txt_details_date);
        txtDesc = findViewById(R.id.txt_details_desc);
        txtLocation = findViewById(R.id.txt_details_location);
        txtReporter = findViewById(R.id.txt_details_reporter);
        imgEvidence = findViewById(R.id.img_details_evidence);
        
        layoutAdmin = findViewById(R.id.layout_admin_panel);
        spinnerStatus = findViewById(R.id.spinner_manage_status);
        Button btnUpdate = findViewById(R.id.btn_update_status);

        firebaseHelper = new FirebaseHelper();
        sessionManager = new SessionManager(this);

        incident = (Incident) getIntent().getSerializableExtra("incident");

        if (incident != null) {
            populateDetails();
            checkUserPermissions();
        } else {
            Toast.makeText(this, "Error: Incident details missing", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnUpdate.setOnClickListener(v -> updateIncidentStatus());
    }

    private void populateDetails() {
        txtTitle.setText(incident.getTitle());
        txtType.setText("Type: " + incident.getType());
        
        String severity = incident.getSeverity().toUpperCase();
        txtSeverity.setText("Severity: " + severity);
        int severityColor;
        switch (severity) {
            case "LOW":
                severityColor = Color.parseColor("#4CAF50");
                break;
            case "MEDIUM":
                severityColor = Color.parseColor("#FF9800");
                break;
            case "HIGH":
                severityColor = Color.parseColor("#E65100");
                break;
            case "CRITICAL":
            default:
                severityColor = Color.parseColor("#D50000");
                break;
        }
        txtSeverity.setTextColor(severityColor);

        txtStatus.setText(incident.getStatus().toUpperCase());
        int statusColor;
        switch (incident.getStatus().toUpperCase()) {
            case "APPROVED":
                statusColor = Color.parseColor("#1E88E5");
                break;
            case "REJECTED":
                statusColor = Color.parseColor("#E53935");
                break;
            case "RESOLVED":
                statusColor = Color.parseColor("#43A047");
                break;
            case "PENDING":
            default:
                statusColor = Color.parseColor("#FFB300");
                break;
        }
        txtStatus.setBackgroundColor(statusColor);

        if (incident.getCreatedDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy 'at' hh:mm a", Locale.getDefault());
            txtDate.setText("Reported: " + sdf.format(incident.getCreatedDate()));
        } else {
            txtDate.setText("");
        }

        txtDesc.setText(incident.getDescription());
        txtLocation.setText(incident.getLocationName());
        txtReporter.setText("Reported By: " + incident.getReportedByName());

        if (incident.getImage() != null && !incident.getImage().isEmpty()) {
            Glide.with(this)
                .load(incident.getImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imgEvidence);
        } else {
            imgEvidence.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void checkUserPermissions() {
        String role = sessionManager.getUserRole();
        if ("admin".equals(role) || "responder".equals(role)) {
            layoutAdmin.setVisibility(View.VISIBLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusChoices);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStatus.setAdapter(adapter);
            
            // Set current selection
            int index = 0;
            for (int i = 0; i < statusChoices.length; i++) {
                if (statusChoices[i].equals(incident.getStatus())) {
                    index = i;
                    break;
                }
            }
            spinnerStatus.setSelection(index);
        } else {
            layoutAdmin.setVisibility(View.GONE);
        }
    }

    private void updateIncidentStatus() {
        String newStatus = spinnerStatus.getSelectedItem().toString();
        
        Toast.makeText(this, "Updating status...", Toast.LENGTH_SHORT).show();

        firebaseHelper.updateIncidentStatus(incident.getIncidentId(), newStatus, task -> {
            if (task.isSuccessful()) {
                firebaseHelper.addSystemLog("INCIDENT_STATUS_CHANGE", 
                    "Incident title: '" + incident.getTitle() + "' status changed to " + newStatus, 
                    sessionManager.getUserDetails().getEmail());

                // Alert the citizen about their status change via notification
                String notifyId = firebaseHelper.getDb().collection("notifications").document().getId();
                Notification notification = new Notification(
                    notifyId, incident.getUserId(), "Report Update: " + incident.getTitle(),
                    "Your emergency report status has been updated to: " + newStatus.toUpperCase()
                );
                firebaseHelper.sendNotification(notification, task1 -> {});

                Toast.makeText(IncidentDetailsActivity.this, "Incident status updated!", Toast.LENGTH_SHORT).show();
                
                // Update local UI
                incident.setStatus(newStatus);
                populateDetails();
            } else {
                Toast.makeText(IncidentDetailsActivity.this, "Failed to update incident", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
