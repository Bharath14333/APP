package com.emergency.crisissense.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.emergency.crisissense.R;
import com.emergency.crisissense.adapter.IncidentAdapter;
import com.emergency.crisissense.model.Incident;
import com.emergency.crisissense.util.FirebaseHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AlertsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private FirebaseHelper firebaseHelper;
    private IncidentAdapter adapter;
    private List<Incident> alertsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts, container, false);

        recyclerView = view.findViewById(R.id.recycler_alerts);
        progressBar = view.findViewById(R.id.progress_bar);
        txtEmpty = view.findViewById(R.id.txt_empty_alerts);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        alertsList = new ArrayList<>();
        adapter = new IncidentAdapter(alertsList, getContext());
        recyclerView.setAdapter(adapter);

        firebaseHelper = new FirebaseHelper();

        loadLiveAlerts();

        return view;
    }

    private void loadLiveAlerts() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getIncidents(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                alertsList.clear();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    Incident incident = doc.toObject(Incident.class);
                    if (incident != null && ("approved".equals(incident.getStatus().toLowerCase()) 
                        || "resolved".equals(incident.getStatus().toLowerCase()))) {
                        alertsList.add(incident);
                    }
                }
                adapter.notifyDataSetChanged();

                if (alertsList.isEmpty()) {
                    txtEmpty.setVisibility(View.VISIBLE);
                } else {
                    txtEmpty.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(getContext(), "Failed to load live alerts", Toast.LENGTH_SHORT).show();
                txtEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}
