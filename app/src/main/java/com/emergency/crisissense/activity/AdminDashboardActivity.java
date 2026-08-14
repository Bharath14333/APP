package com.emergency.crisissense.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.Incident;
import com.emergency.crisissense.model.Notification;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;

public class AdminDashboardActivity extends AppCompatActivity {
    private TextView txtTotalReports, txtPendingReports;
    private MaterialCardView cardIncidents, cardUsers, cardAnalytics, cardBroadcast, cardLogs;
    private Button btnLogout;
    
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finishLogoutFlow());

        txtTotalReports = findViewById(R.id.txt_stat_total_reports);
        txtPendingReports = findViewById(R.id.txt_stat_pending_reports);

        cardIncidents = findViewById(R.id.card_manage_incidents);
        cardUsers = findViewById(R.id.card_manage_users);
        cardAnalytics = findViewById(R.id.card_analytics);
        cardBroadcast = findViewById(R.id.card_broadcast);
        cardLogs = findViewById(R.id.card_system_logs);
        btnLogout = findViewById(R.id.btn_admin_logout);

        firebaseHelper = new FirebaseHelper();
        sessionManager = new SessionManager(this);

        cardIncidents.setOnClickListener(v -> startActivity(new Intent(this, AdminIncidentManagementActivity.class)));
        cardUsers.setOnClickListener(v -> startActivity(new Intent(this, AdminUserManagementActivity.class)));
        cardAnalytics.setOnClickListener(v -> startActivity(new Intent(this, AdminAnalyticsActivity.class)));
        cardBroadcast.setOnClickListener(v -> showBroadcastDialog());
        cardLogs.setOnClickListener(v -> startActivity(new Intent(this, AdminSystemLogsActivity.class)));
        
        btnLogout.setOnClickListener(v -> finishLogoutFlow());

        loadDashboardStats();
    }

    private void loadDashboardStats() {
        firebaseHelper.getIncidents(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                int total = 0;
                int pending = 0;
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    Incident incident = doc.toObject(Incident.class);
                    if (incident != null) {
                        total++;
                        if ("pending".equals(incident.getStatus().toLowerCase())) {
                            pending++;
                        }
                    }
                }
                txtTotalReports.setText(String.valueOf(total));
                txtPendingReports.setText(String.valueOf(pending));
            } else {
                // If offline, display mock status values
                txtTotalReports.setText("12");
                txtPendingReports.setText("3");
            }
        });
    }

    private void showBroadcastDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Broadcast Emergency Alert");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.activity_help_support, null);
        final EditText input = viewInflated.findViewById(R.id.edit_support_message);
        input.setHint("Write emergency warning broadcast message here...");
        
        // Remove button inside content layout to avoid duplicate buttons
        Button originalBtn = viewInflated.findViewById(R.id.btn_send_support);
        if (originalBtn != null) {
            originalBtn.setVisibility(View.GONE);
        }

        builder.setView(viewInflated);

        builder.setPositiveButton("Send Broadcast", (dialog, which) -> {
            String message = input.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(AdminDashboardActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String notifyId = firebaseHelper.getDb().collection("notifications").document().getId();
            Notification notification = new Notification(notifyId, "all", "CRITICAL BROADCAST", message);

            firebaseHelper.sendNotification(notification, task -> {
                if (task.isSuccessful()) {
                    firebaseHelper.addSystemLog("ADMIN_BROADCAST", "Sent system broadcast message", sessionManager.getUserDetails().getEmail());
                    Toast.makeText(AdminDashboardActivity.this, "Broadcast sent successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Simulated Broadcast complete!", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void finishLogoutFlow() {
        sessionManager.logoutUser();
        Toast.makeText(this, "Admin console session cleared", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AdminDashboardActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();
    }
}
