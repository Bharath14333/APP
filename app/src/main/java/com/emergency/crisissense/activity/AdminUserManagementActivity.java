package com.emergency.crisissense.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.emergency.crisissense.R;
import com.emergency.crisissense.adapter.UserAdapter;
import com.emergency.crisissense.model.User;
import com.emergency.crisissense.util.FirebaseHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminUserManagementActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private FirebaseHelper firebaseHelper;
    private UserAdapter adapter;
    private List<User> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_users);
        progressBar = findViewById(R.id.progress_bar);
        txtEmpty = findViewById(R.id.txt_empty_users);

        firebaseHelper = new FirebaseHelper();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersList = new ArrayList<>();
        adapter = new UserAdapter(usersList, this);
        recyclerView.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getDb().collection("users").get()
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful() && task.getResult() != null) {
                    usersList.clear();
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            usersList.add(user);
                        }
                    }

                    if (usersList.isEmpty()) {
                        loadMockUsers();
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(AdminUserManagementActivity.this, "Offline Mode: Loading simulated user records.", Toast.LENGTH_SHORT).show();
                    loadMockUsers();
                }
            });
    }

    private void loadMockUsers() {
        usersList.clear();
        usersList.add(new User("u1", "John Doe", "john@crisissense.com", "555-0101", "citizen", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=150", "123 Safety Ave"));
        usersList.add(new User("u2", "Alice Vance", "alice@crisissense.com", "555-0102", "volunteer", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=150", "456 Shield Rd"));
        usersList.add(new User("u3", "Chief Officer Bob", "bob@crisissense.com", "555-0103", "responder", "", "Station 9 Office"));
        adapter.notifyDataSetChanged();
        txtEmpty.setVisibility(View.GONE);
    }
}
