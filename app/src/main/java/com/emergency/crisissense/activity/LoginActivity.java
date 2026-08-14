package com.emergency.crisissense.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.User;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private EditText editEmail, editPassword;
    private Button btnLogin;
    private TextView txtForgotPassword, txtRegister;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);
        txtForgotPassword = findViewById(R.id.txt_forgot_password);
        txtRegister = findViewById(R.id.txt_register);

        firebaseHelper = new FirebaseHelper();
        sessionManager = new SessionManager(this);

        btnLogin.setOnClickListener(v -> performLogin());

        txtRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        txtForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void performLogin() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Password is required");
            return;
        }

        btnLogin.setEnabled(false);
        Toast.makeText(this, "Authenticating user...", Toast.LENGTH_SHORT).show();

        firebaseHelper.login(email, password, task -> {
            if (task.isSuccessful() && firebaseHelper.getAuth().getCurrentUser() != null) {
                String uid = firebaseHelper.getAuth().getCurrentUser().getUid();
                
                // Fetch profile
                firebaseHelper.getUserProfile(uid, documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        sessionManager.createLoginSession(user);
                        firebaseHelper.addSystemLog("USER_LOGIN", "User logged in successfully", user.getEmail());
                        
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                }, e -> {
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Failed to load user profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

            } else {
                // Support offline demo testing: bypass Firebase Auth if API key is invalid/missing
                if (task.getException() != null && (task.getException().getMessage().contains("API key") || task.getException().getMessage().contains("internal error") || task.getException().getMessage().contains("API Key"))) {
                    User user = new User("offline_user_id", "Demo User", email, "6380549366", "citizen", "", "Chittoor");
                    sessionManager.createLoginSession(user);
                    
                    Toast.makeText(LoginActivity.this, "Authentication Bypass (Offline Demo Mode)", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Authentication failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
