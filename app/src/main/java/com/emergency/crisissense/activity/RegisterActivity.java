package com.emergency.crisissense.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.User;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;

public class RegisterActivity extends AppCompatActivity {
    private EditText editName, editEmail, editPhone, editAddress, editPassword, editConfirmPassword;
    private Spinner spinnerRole;
    private Button btnRegister;
    private TextView txtLogin;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    private final String[] roles = {"Citizen", "Volunteer", "Emergency Responder"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        editAddress = findViewById(R.id.edit_address);
        editPassword = findViewById(R.id.edit_password);
        editConfirmPassword = findViewById(R.id.edit_confirm_password);
        spinnerRole = findViewById(R.id.spinner_role);
        btnRegister = findViewById(R.id.btn_register);
        txtLogin = findViewById(R.id.txt_login);

        firebaseHelper = new FirebaseHelper();
        sessionManager = new SessionManager(this);

        // Spinner adapters
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> performRegistration());

        txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void performRegistration() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString().toLowerCase();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Email is required");
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
        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Password is required");
            return;
        }
        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("Passwords do not match");
            return;
        }

        btnRegister.setEnabled(false);
        Toast.makeText(this, "Creating profile...", Toast.LENGTH_SHORT).show();

        User user = new User("", name, email, phone, role, "", address);

        firebaseHelper.register(user, password, task -> {
            if (task.isSuccessful() && firebaseHelper.getAuth().getCurrentUser() != null) {
                String uid = firebaseHelper.getAuth().getCurrentUser().getUid();
                user.setUserId(uid);
                sessionManager.createLoginSession(user);
                
                firebaseHelper.addSystemLog("USER_REGISTRATION", "Registered new user: " + role, email);
                
                // Redirect to OTP verification screen for simulated verification
                Intent intent = new Intent(RegisterActivity.this, OtpVerificationActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Support offline demo testing: bypass Firebase Auth if API key is invalid/missing
                if (task.getException() != null && (task.getException().getMessage().contains("API key") || task.getException().getMessage().contains("internal error") || task.getException().getMessage().contains("API Key"))) {
                    String uid = "offline_user_" + System.currentTimeMillis();
                    user.setUserId(uid);
                    sessionManager.createLoginSession(user);
                    
                    Toast.makeText(RegisterActivity.this, "Registration Bypass (Offline Demo Mode)", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(RegisterActivity.this, OtpVerificationActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
