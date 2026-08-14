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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminIncidentManagementActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private TabLayout tabLayout;
    
    private FirebaseHelper firebaseHelper;
    private IncidentAdapter adapter;
    private List<Incident> allIncidents;
    private List<Incident> displayedIncidents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_incident_management);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_incidents_admin);
        progressBar = findViewById(R.id.progress_bar);
        txtEmpty = findViewById(R.id.txt_empty_incidents_admin);
        tabLayout = findViewById(R.id.tab_layout_admin);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allIncidents = new ArrayList<>();
        displayedIncidents = new ArrayList<>();
        adapter = new IncidentAdapter(displayedIncidents, this);
        recyclerView.setAdapter(adapter);

        firebaseHelper = new FirebaseHelper();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterIncidents(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadIncidents();
    }

    private void loadIncidents() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getIncidents(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                allIncidents.clear();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    Incident incident = doc.toObject(Incident.class);
                    if (incident != null) {
                        allIncidents.add(incident);
                    }
                }
                
                if (allIncidents.isEmpty()) {
                    loadMockIncidents();
                } else {
                    filterIncidents(tabLayout.getSelectedTabPosition());
                }
            } else {
                Toast.makeText(AdminIncidentManagementActivity.this, "Offline Mode: Loading simulated incident reports.", Toast.LENGTH_SHORT).show();
                loadMockIncidents();
            }
        });
    }

    private void loadMockIncidents() {
        allIncidents.clear();
        allIncidents.add(new Incident("i1", "u1", "John Doe", "Gas Leak Warning", "Fire", "Citizen reported heavy smell of gas in building lobby.", "Lobby B, Center tower", "Lobby B, Center tower", "", "pending", "critical"));
        allIncidents.add(new Incident("i2", "u2", "Alice Vance", "Highway Collision", "Accident", "Two cars collision near exit 4. Ambulances needed.", "Highway 10, KM 14", "Highway 10, KM 14", "", "approved", "high"));
        allIncidents.add(new Incident("i3", "u1", "John Doe", "Power Line Down", "Other", "Tree branch broke the power line on Oak street.", "88 Oak Street", "88 Oak Street", "", "resolved", "medium"));
        filterIncidents(tabLayout.getSelectedTabPosition());
    }

    private void filterIncidents(int tabPosition) {
        displayedIncidents.clear();
        String targetStatus = "";
        
        switch (tabPosition) {
            case 1:
                targetStatus = "pending";
                break;
            case 2:
                targetStatus = "approved";
                break;
            case 3:
                targetStatus = "resolved";
                break;
            case 0:
            default:
                targetStatus = "";
                break;
        }

        for (Incident incident : allIncidents) {
            if (targetStatus.isEmpty() || targetStatus.equals(incident.getStatus().toLowerCase())) {
                displayedIncidents.add(incident);
            }
        }

        adapter.notifyDataSetChanged();

        if (displayedIncidents.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
        } else {
            txtEmpty.setVisibility(View.GONE);
        }
    }
}
