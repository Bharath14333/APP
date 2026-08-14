package com.emergency.crisissense.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.emergency.crisissense.R;

public class OtpVerificationActivity extends AppCompatActivity {
    private EditText editOtp;
    private Button btnVerify;
    private TextView txtResend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        editOtp = findViewById(R.id.edit_otp);
        btnVerify = findViewById(R.id.btn_verify_otp);
        txtResend = findViewById(R.id.txt_resend_otp);

        // Toast a dummy code for developer convenience
        Toast.makeText(this, "Simulated SMS OTP Code: 123456", Toast.LENGTH_LONG).show();

        btnVerify.setOnClickListener(v -> {
            String otp = editOtp.getText().toString().trim();
            if (otp.length() < 6) {
                editOtp.setError("Enter 6-digit OTP code");
                return;
            }

            // Simulate OTP check
            if ("123456".equals(otp)) {
                Toast.makeText(OtpVerificationActivity.this, "OTP Verification Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OtpVerificationActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                editOtp.setError("Incorrect OTP code. Try again.");
            }
        });

        txtResend.setOnClickListener(v -> {
            Toast.makeText(OtpVerificationActivity.this, "New OTP code sent! Code: 123456", Toast.LENGTH_LONG).show();
        });
    }
}
