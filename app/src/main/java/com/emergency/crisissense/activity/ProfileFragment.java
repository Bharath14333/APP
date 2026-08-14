package com.emergency.crisissense.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.Incident;
import com.emergency.crisissense.model.User;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileFragment extends Fragment {
    private TextView txtName, txtRole, txtEmail, txtPhone, txtAddress, txtReportsCount, txtResolvedCount;
    private ImageView imgAvatar;
    private SessionManager sessionManager;
    private FirebaseHelper firebaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        txtName = view.findViewById(R.id.txt_profile_name);
        txtRole = view.findViewById(R.id.txt_profile_role);
        txtEmail = view.findViewById(R.id.txt_profile_email);
        txtPhone = view.findViewById(R.id.txt_profile_phone);
        txtAddress = view.findViewById(R.id.txt_profile_address);
        txtReportsCount = view.findViewById(R.id.txt_stat_reports);
        txtResolvedCount = view.findViewById(R.id.txt_stat_resolved);
        imgAvatar = view.findViewById(R.id.img_profile_avatar);
        
        Button btnEdit = view.findViewById(R.id.btn_edit_profile);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        sessionManager = new SessionManager(requireContext());
        firebaseHelper = new FirebaseHelper();

        loadProfileData();

        btnEdit.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }

    private void loadProfileData() {
        User user = sessionManager.getUserDetails();
        if (user != null) {
            txtName.setText(user.getName());
            txtRole.setText(user.getRole().toUpperCase());
            txtEmail.setText(user.getEmail());
            txtPhone.setText(user.getPhone());
            txtAddress.setText(user.getAddress());

            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                Glide.with(this)
                    .load(user.getProfileImage())
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .into(imgAvatar);
            }

            // Fetch statistics
            firebaseHelper.getIncidentsByUserId(user.getUserId(), task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    int total = 0;
                    int resolved = 0;
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        Incident incident = doc.toObject(Incident.class);
                        if (incident != null) {
                            total++;
                            if ("resolved".equals(incident.getStatus().toLowerCase())) {
                                resolved++;
                            }
                        }
                    }
                    txtReportsCount.setText(String.valueOf(total));
                    txtResolvedCount.setText(String.valueOf(resolved));
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData(); // Refresh profile updates after editing
    }
}
