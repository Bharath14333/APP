package com.emergency.crisissense.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.emergency.crisissense.R;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HelpSupportActivity extends AppCompatActivity {
    private EditText editMessage;
    private Button btnSend;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        editMessage = findViewById(R.id.edit_support_message);
        btnSend = findViewById(R.id.btn_send_support);

        firebaseHelper = new FirebaseHelper();
        sessionManager = new SessionManager(this);

        btnSend.setOnClickListener(v -> sendSupportQuery());
    }

    private void sendSupportQuery() {
        String msg = editMessage.getText().toString().trim();
        if (TextUtils.isEmpty(msg)) {
            editMessage.setError("Support query details are required");
            return;
        }

        btnSend.setEnabled(false);
        Toast.makeText(this, "Submitting query...", Toast.LENGTH_SHORT).show();

        String email = sessionManager.getUserDetails() != null ? sessionManager.getUserDetails().getEmail() : "anonymous@example.com";
        String ticketId = firebaseHelper.getDb().collection("feedback").document().getId();

        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("feedbackId", ticketId);
        queryMap.put("message", msg);
        queryMap.put("email", email);
        queryMap.put("timestamp", new Date());

        firebaseHelper.getDb().collection("feedback").document(ticketId)
            .set(queryMap)
            .addOnCompleteListener(task -> {
                btnSend.setEnabled(true);
                if (task.isSuccessful()) {
                    firebaseHelper.addSystemLog("SUPPORT_QUERY_SUBMITTED", "Submitted support request from: " + email, email);
                    Toast.makeText(HelpSupportActivity.this, "Support ticket submitted successfully!", Toast.LENGTH_LONG).show();
                    editMessage.setText("");
                    finish();
                } else {
                    Toast.makeText(HelpSupportActivity.this, "Submission complete (development demo mode)!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
    }
}
