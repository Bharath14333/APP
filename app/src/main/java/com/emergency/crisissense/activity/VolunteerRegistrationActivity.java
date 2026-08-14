package com.emergency.crisissense.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.User;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;

public class VolunteerRegistrationActivity extends AppCompatActivity {
    private CheckBox checkFirstAid, checkRescue, checkLogistics, checkCrisisComm;
    private EditText editAvailability;
    private Button btnSubmit;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_registration);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        checkFirstAid = findViewById(R.id.check_first_aid);
        checkRescue = findViewById(R.id.check_rescue);
        checkLogistics = findViewById(R.id.check_logistics);
        checkCrisisComm = findViewById(R.id.check_crisis_comm);
        editAvailability = findViewById(R.id.edit_availability);
        btnSubmit = findViewById(R.id.btn_submit_volunteer);

        firebaseHelper = new FirebaseHelper();
        sessionManager = new SessionManager(this);

        btnSubmit.setOnClickListener(v -> submitVolunteerForm());
    }

    private void submitVolunteerForm() {
        boolean hasFirstAid = checkFirstAid.isChecked();
        boolean hasRescue = checkRescue.isChecked();
        boolean hasLogistics = checkLogistics.isChecked();
        boolean hasComm = checkCrisisComm.isChecked();
        String availability = editAvailability.getText().toString().trim();

        if (!hasFirstAid && !hasRescue && !hasLogistics && !hasComm) {
            Toast.makeText(this, "Please select at least one skill/training category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(availability)) {
            editAvailability.setError("Availability details are required");
            return;
        }

        User currentUser = sessionManager.getUserDetails();
        if (currentUser == null) {
            Toast.makeText(this, "Error: User session not found", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        Toast.makeText(this, "Registering as volunteer...", Toast.LENGTH_SHORT).show();

        // Update role to volunteer
        currentUser.setRole("volunteer");
        
        firebaseHelper.saveUserProfile(currentUser, task -> {
            btnSubmit.setEnabled(true);
            if (task.isSuccessful()) {
                sessionManager.createLoginSession(currentUser);
                firebaseHelper.addSystemLog("VOLUNTEER_REGISTRATION", 
                    "User registered as volunteer. Availability: " + availability, 
                    currentUser.getEmail());
                
                Toast.makeText(VolunteerRegistrationActivity.this, "Successfully registered as Volunteer!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(VolunteerRegistrationActivity.this, "Failed to register. Check internet connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
