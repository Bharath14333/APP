package com.emergency.crisissense.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.User;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;

public class AdminLoginActivity extends AppCompatActivity {
    private EditText editEmail, editPassword, editPasskey;
    private Button btnLogin;
    private TextView txtBack;
    
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        editEmail = findViewById(R.id.edit_admin_email);
        editPassword = findViewById(R.id.edit_admin_password);
        editPasskey = findViewById(R.id.edit_admin_passkey);
        btnLogin = findViewById(R.id.btn_admin_login);
        txtBack = findViewById(R.id.txt_back_to_welcome);

        firebaseHelper = new FirebaseHelper();
        sessionManager = new SessionManager(this);

        // Prefill admin credentials for easier developer/client testing
        editEmail.setText("admin@crisissense.com");
        editPassword.setText("admin123");
        editPasskey.setText("admin123");

        btnLogin.setOnClickListener(v -> performAdminLogin());
        txtBack.setOnClickListener(v -> finish());
    }

    private void performAdminLogin() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String passkey = editPasskey.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Admin Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Password is required");
            return;
        }
        if (TextUtils.isEmpty(passkey)) {
            editPasskey.setError("Passkey is required");
            return;
        }

        if (!"admin123".equals(passkey)) {
            editPasskey.setError("Invalid security passkey");
            return;
        }

        btnLogin.setEnabled(false);
        Toast.makeText(this, "Verifying credentials...", Toast.LENGTH_SHORT).show();

        // Perform login with Firebase auth, or use fallback if offline
        firebaseHelper.login(email, password, task -> {
            if (task.isSuccessful() && firebaseHelper.getAuth().getCurrentUser() != null) {
                String uid = firebaseHelper.getAuth().getCurrentUser().getUid();
                // Fetch/Set user
                User user = new User(uid, "System Administrator", email, "555-0199", "admin", "", "HQ Command Center");
                sessionManager.createLoginSession(user);
                firebaseHelper.addSystemLog("ADMIN_LOGIN", "Admin logged in successfully", email);
                
                navigateToDashboard();
            } else {
                // Support offline demo testing: bypass Firebase Auth if credentials match admin123
                if ("admin@crisissense.com".equals(email) && "admin123".equals(password)) {
                    User user = new User("admin_offline_id", "System Administrator", email, "555-0199", "admin", "", "HQ Command Center");
                    sessionManager.createLoginSession(user);
                    firebaseHelper.addSystemLog("ADMIN_LOGIN", "Admin logged in successfully (Demo mode)", email);
                    
                    Toast.makeText(AdminLoginActivity.this, "Admin Authenticated (Development Demo)", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                } else {
                    btnLogin.setEnabled(true);
                    Toast.makeText(AdminLoginActivity.this, "Authentication Failed: Incorrect credentials", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
