package com.emergency.crisissense.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.User;
import com.emergency.crisissense.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class DashboardFragment extends Fragment {
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        sessionManager = new SessionManager(requireContext());
        TextView txtWelcome = view.findViewById(R.id.txt_welcome);
        
        User user = sessionManager.getUserDetails();
        if (user != null) {
            txtWelcome.setText("Hello, " + user.getName() + "!");
        }

        // Action Cards
        MaterialCardView cardReport = view.findViewById(R.id.card_report_emergency);
        MaterialCardView cardMyReports = view.findViewById(R.id.card_my_reports);
        MaterialCardView cardLiveAlerts = view.findViewById(R.id.card_live_alerts);
        MaterialCardView cardContacts = view.findViewById(R.id.card_contacts);
        MaterialCardView cardNearby = view.findViewById(R.id.card_nearby);
        MaterialCardView cardVolunteer = view.findViewById(R.id.card_volunteer);
        MaterialCardView cardSettings = view.findViewById(R.id.card_settings);

        cardReport.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SosActivity.class));
        });

        cardMyReports.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), IncidentHistoryActivity.class));
        });

        cardLiveAlerts.setOnClickListener(v -> {
            BottomNavigationView bnv = requireActivity().findViewById(R.id.bottom_navigation);
            if (bnv != null) {
                bnv.setSelectedItemId(R.id.nav_alerts);
            }
        });

        cardContacts.setOnClickListener(v -> {
            BottomNavigationView bnv = requireActivity().findViewById(R.id.bottom_navigation);
            if (bnv != null) {
                bnv.setSelectedItemId(R.id.nav_contacts);
            }
        });

        cardNearby.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), NearbyServicesActivity.class));
        });

        cardVolunteer.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), VolunteerRegistrationActivity.class));
        });

        cardSettings.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        });

        return view;
    }
}
