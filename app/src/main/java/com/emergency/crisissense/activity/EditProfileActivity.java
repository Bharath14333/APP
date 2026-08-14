package com.emergency.crisissense.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.User;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;
import com.google.android.material.card.MaterialCardView;

public class EditProfileActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private EditText editName, editPhone, editAddress;
    private Button btnSave;
    private MaterialCardView cardChangeAvatar;
    
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private User currentUser;
    private String simulatedAvatarUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        imgAvatar = findViewById(R.id.img_edit_avatar);
        editName = findViewById(R.id.edit_name);
        editPhone = findViewById(R.id.edit_phone);
        editAddress = findViewById(R.id.edit_address);
        btnSave = findViewById(R.id.btn_save_profile);
        cardChangeAvatar = findViewById(R.id.card_change_avatar);

        firebaseHelper = new FirebaseHelper();
        sessionManager = new SessionManager(this);
        currentUser = sessionManager.getUserDetails();

        if (currentUser != null) {
            prefillData();
        } else {
            Toast.makeText(this, "Session error", Toast.LENGTH_SHORT).show();
            finish();
        }

        cardChangeAvatar.setOnClickListener(v -> changeAvatarSimulated());
        btnSave.setOnClickListener(v -> saveProfileData());
    }

    private void prefillData() {
        editName.setText(currentUser.getName());
        editPhone.setText(currentUser.getPhone());
        editAddress.setText(currentUser.getAddress());
        simulatedAvatarUrl = currentUser.getProfileImage();

        if (simulatedAvatarUrl != null && !simulatedAvatarUrl.isEmpty()) {
            Glide.with(this)
                .load(simulatedAvatarUrl)
                .placeholder(android.R.drawable.sym_def_app_icon)
                .into(imgAvatar);
        }
    }

    private void changeAvatarSimulated() {
        // Toggle between two predefined simulated avatars
        if (simulatedAvatarUrl.contains("face-1")) {
            simulatedAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200&auto=format&fit=crop";
        } else {
            simulatedAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200&auto=format&fit=crop";
        }
        
        Glide.with(this)
            .load(simulatedAvatarUrl)
            .placeholder(android.R.drawable.sym_def_app_icon)
            .into(imgAvatar);
            
        Toast.makeText(this, "Avatar updated!", Toast.LENGTH_SHORT).show();
    }

    private void saveProfileData() {
        String name = editName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            editPhone.setError("Phone is required");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            editAddress.setError("Address is required");
            return;
        }

        btnSave.setEnabled(false);
        Toast.makeText(this, "Saving changes...", Toast.LENGTH_SHORT).show();

        currentUser.setName(name);
        currentUser.setPhone(phone);
        currentUser.setAddress(address);
        currentUser.setProfileImage(simulatedAvatarUrl);

        firebaseHelper.saveUserProfile(currentUser, task -> {
            btnSave.setEnabled(true);
            if (task.isSuccessful()) {
                sessionManager.createLoginSession(currentUser);
                firebaseHelper.addSystemLog("PROFILE_EDITED", "User edited profile info", currentUser.getEmail());
                Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditProfileActivity.this, "Failed to save to Firestore. Session updated locally.", Toast.LENGTH_SHORT).show();
                sessionManager.createLoginSession(currentUser);
                finish();
            }
        });
    }
}
