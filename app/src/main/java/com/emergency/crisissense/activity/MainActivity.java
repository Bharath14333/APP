package com.emergency.crisissense.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.emergency.crisissense.R;
import com.emergency.crisissense.model.User;
import com.emergency.crisissense.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sessionManager = new SessionManager(this);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.nav_dashboard, R.string.nav_dashboard
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Load Default Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DashboardFragment())
                .commit();
            bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        }

        // Bottom Navigation click listener
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.nav_alerts) {
                selectedFragment = new AlertsFragment();
            } else if (itemId == R.id.nav_contacts) {
                selectedFragment = new ContactsFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
                return true;
            }
            return false;
        });

        // Setup Header User Information in Side Drawer
        setupDrawerHeader(navigationView);
    }

    private void setupDrawerHeader(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        ImageView imgProfile = headerView.findViewById(R.id.img_profile_header);
        TextView txtName = headerView.findViewById(R.id.txt_name_header);
        TextView txtEmail = headerView.findViewById(R.id.txt_email_header);

        User user = sessionManager.getUserDetails();
        if (user != null) {
            txtName.setText(user.getName());
            txtEmail.setText(user.getEmail());
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                Glide.with(this)
                    .load(user.getProfileImage())
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .into(imgProfile);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.drawer_dashboard) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DashboardFragment())
                .commit();
        } else if (id == R.id.drawer_alerts) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AlertsFragment())
                .commit();
        } else if (id == R.id.drawer_contacts) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ContactsFragment())
                .commit();
        } else if (id == R.id.drawer_history) {
            startActivity(new Intent(this, IncidentHistoryActivity.class));
        } else if (id == R.id.drawer_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.drawer_help) {
            startActivity(new Intent(this, HelpSupportActivity.class));
        } else if (id == R.id.drawer_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.drawer_logout) {
            sessionManager.logoutUser();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
