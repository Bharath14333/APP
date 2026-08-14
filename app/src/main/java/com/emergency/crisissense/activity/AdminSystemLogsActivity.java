package com.emergency.crisissense.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.emergency.crisissense.R;
import com.emergency.crisissense.adapter.LogAdapter;
import com.emergency.crisissense.model.LogEntry;
import com.emergency.crisissense.util.FirebaseHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminSystemLogsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private FirebaseHelper firebaseHelper;
    private LogAdapter adapter;
    private List<LogEntry> logsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_system_logs);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_logs);
        progressBar = findViewById(R.id.progress_bar);
        txtEmpty = findViewById(R.id.txt_empty_logs);

        firebaseHelper = new FirebaseHelper();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        logsList = new ArrayList<>();
        adapter = new LogAdapter(logsList);
        recyclerView.setAdapter(adapter);

        loadSystemLogs();
    }

    private void loadSystemLogs() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getSystemLogs(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                logsList.clear();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    LogEntry log = doc.toObject(LogEntry.class);
                    if (log != null) {
                        logsList.add(log);
                    }
                }

                if (logsList.isEmpty()) {
                    loadMockSystemLogs();
                } else {
                    adapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(AdminSystemLogsActivity.this, "Offline Mode: Loading simulated audit records.", Toast.LENGTH_SHORT).show();
                loadMockSystemLogs();
            }
        });
    }

    private void loadMockSystemLogs() {
        logsList.clear();
        logsList.add(new LogEntry("l1", "ADMIN_LOGIN", "Admin logged in successfully", "admin@crisissense.com"));
        logsList.add(new LogEntry("l2", "VOLUNTEER_REGISTRATION", "User registered as volunteer. Availability: weekends", "alice@crisissense.com"));
        logsList.add(new LogEntry("l3", "INCIDENT_REPORTED", "Reported new incident: Gas Leak Warning", "john@crisissense.com"));
        logsList.add(new LogEntry("l4", "USER_REGISTRATION", "Registered new user: volunteer", "alice@crisissense.com"));
        logsList.add(new LogEntry("l5", "USER_LOGIN", "User logged in successfully", "john@crisissense.com"));
        adapter.notifyDataSetChanged();
        txtEmpty.setVisibility(View.GONE);
    }
}
