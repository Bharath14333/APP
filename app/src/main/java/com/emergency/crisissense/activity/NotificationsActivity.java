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
import com.emergency.crisissense.adapter.NotificationAdapter;
import com.emergency.crisissense.model.Notification;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private NotificationAdapter adapter;
    private List<Notification> notificationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_notifications);
        progressBar = findViewById(R.id.progress_bar);
        txtEmpty = findViewById(R.id.txt_empty_notifications);

        sessionManager = new SessionManager(this);
        firebaseHelper = new FirebaseHelper();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationsList);
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        String currentUserId = sessionManager.getUserId();

        firebaseHelper.getNotifications(currentUserId, task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                notificationsList.clear();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    Notification notification = doc.toObject(Notification.class);
                    if (notification != null) {
                        // Filter notifications for this user or broadcast to all
                        if ("all".equals(notification.getUserId()) || currentUserId.equals(notification.getUserId())) {
                            notificationsList.add(notification);
                        }
                    }
                }
                
                if (notificationsList.isEmpty()) {
                    loadFallbackMockNotifications();
                } else {
                    adapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(NotificationsActivity.this, "Offline Mode: Loading simulated notifications.", Toast.LENGTH_SHORT).show();
                loadFallbackMockNotifications();
            }
        });
    }

    private void loadFallbackMockNotifications() {
        notificationsList.clear();
        notificationsList.add(new Notification("n1", "all", "EMERGENCY: High Severity Fire", "A high severity Fire emergency has been reported at downtown. Keep safe."));
        notificationsList.add(new Notification("n2", "all", "Weather Warning: Heavy Rain", "Heavy rainfall expected tonight. Avoid low-lying areas."));
        notificationsList.add(new Notification("n3", "personal", "Profile Verified", "Your citizen profile has been verified successfully."));
        adapter.notifyDataSetChanged();
        txtEmpty.setVisibility(View.GONE);
    }
}
