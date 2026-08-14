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
import com.emergency.crisissense.adapter.IncidentAdapter;
import com.emergency.crisissense.model.Incident;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class IncidentHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private IncidentAdapter adapter;
    private List<Incident> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_history);
        progressBar = findViewById(R.id.progress_bar);
        txtEmpty = findViewById(R.id.txt_empty_history);

        sessionManager = new SessionManager(this);
        firebaseHelper = new FirebaseHelper();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyList = new ArrayList<>();
        adapter = new IncidentAdapter(historyList, this);
        recyclerView.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = sessionManager.getUserId();
        
        firebaseHelper.getIncidentsByUserId(userId, task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                historyList.clear();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    Incident incident = doc.toObject(Incident.class);
                    if (incident != null) {
                        historyList.add(incident);
                    }
                }
                adapter.notifyDataSetChanged();

                if (historyList.isEmpty()) {
                    txtEmpty.setVisibility(View.VISIBLE);
                } else {
                    txtEmpty.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(IncidentHistoryActivity.this, "Failed to load report history", Toast.LENGTH_SHORT).show();
                txtEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}
