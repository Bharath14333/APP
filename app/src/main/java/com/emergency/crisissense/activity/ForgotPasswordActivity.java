package com.emergency.crisissense.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.emergency.crisissense.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText editEmail;
    private Button btnReset;
    private TextView txtBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editEmail = findViewById(R.id.edit_email);
        btnReset = findViewById(R.id.btn_reset_password);
        txtBack = findViewById(R.id.txt_back_to_login);

        btnReset.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                editEmail.setError("Email is required");
                return;
            }

            btnReset.setEnabled(false);
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    btnReset.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Reset email sent successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Error: " + (task.getException() != null ? task.getException().getMessage() : "Failed reset"), Toast.LENGTH_SHORT).show();
                    }
                });
        });

        txtBack.setOnClickListener(v -> finish());
    }
}
