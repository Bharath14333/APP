package com.emergency.crisissense.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emergency.crisissense.R;
import com.emergency.crisissense.adapter.EmergencyServicesAdapter;
import com.emergency.crisissense.model.EmergencyService;
import com.emergency.crisissense.model.FavoriteService;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NearbyServicesActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private TabLayout tabLayout;
    private EditText editSearch;
    
    // GPS UI elements
    private TextView txtAddress, txtGps;
    private ImageButton btnRefreshLocation;
    
    // Map launching button
    private View btnOpenMap;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private EmergencyServicesAdapter adapter;

    private List<EmergencyService> allServices;
    private List<EmergencyService> displayedServices;
    private Set<String> favoriteIds;

    // Default current coordinates (Downtown Los Angeles/Fallback)
    private double currentLatitude = 34.0522;
    private double currentLongitude = -118.2437;
    private boolean isLocationDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_services);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Bind Views
        recyclerView = findViewById(R.id.recycler_nearby);
        progressBar = findViewById(R.id.progress_bar);
        txtEmpty = findViewById(R.id.txt_empty_nearby);
        tabLayout = findViewById(R.id.tab_layout);
        editSearch = findViewById(R.id.edit_search_service);
        txtAddress = findViewById(R.id.txt_current_address);
        txtGps = findViewById(R.id.txt_gps_coords);
        btnRefreshLocation = findViewById(R.id.btn_refresh_location);
        btnOpenMap = findViewById(R.id.btn_open_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        firebaseHelper = new FirebaseHelper();
        sessionManager = new SessionManager(this);

        allServices = new ArrayList<>();
        displayedServices = new ArrayList<>();
        favoriteIds = new HashSet<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmergencyServicesAdapter(displayedServices, favoriteIds, new EmergencyServicesAdapter.OnServiceClickListener() {
            @Override
            public void onCallClick(EmergencyService service) {
                dialPhoneNumber(service.getPhone());
            }

            @Override
            public void onDirectionsClick(EmergencyService service) {
                openDirectionsMap(service.getLatitude(), service.getLongitude(), service.getName());
            }

            @Override
            public void onFavoriteClick(EmergencyService service, boolean isFavorite) {
                toggleFavoriteService(service, isFavorite);
            }
        }, this);
        recyclerView.setAdapter(adapter);

        // Bind Emergency Card clicks
        setupQuickCategoryCards();

        // Search text watcher
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndSearchServices();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Tab selection change listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterAndSearchServices();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // GPS Refresh button
        btnRefreshLocation.setOnClickListener(v -> checkLocationSettingsAndDetect());

        // Map View button
        btnOpenMap.setOnClickListener(v -> {
            Intent intent = new Intent(NearbyServicesActivity.this, MapActivity.class);
            intent.putExtra("user_lat", currentLatitude);
            intent.putExtra("user_lng", currentLongitude);
            startActivity(intent);
        });

        // Initialize Services database & load
        initializeData();
        checkLocationSettingsAndDetect();
    }

    private void setupQuickCategoryCards() {
        MaterialCardView cardAmbulance = findViewById(R.id.card_cat_ambulance);
        MaterialCardView cardPolice = findViewById(R.id.card_cat_police);
        MaterialCardView cardFire = findViewById(R.id.card_cat_fire);
        MaterialCardView cardHospital = findViewById(R.id.card_cat_hospital);
        MaterialCardView cardPharmacy = findViewById(R.id.card_cat_pharmacy);
        MaterialCardView cardShelter = findViewById(R.id.card_cat_shelter);

        cardAmbulance.setOnClickListener(v -> selectTab(4));
        cardPolice.setOnClickListener(v -> selectTab(2));
        cardFire.setOnClickListener(v -> selectTab(3));
        cardHospital.setOnClickListener(v -> selectTab(1));
        cardPharmacy.setOnClickListener(v -> selectTab(5));
        cardShelter.setOnClickListener(v -> selectTab(6));
    }

    private void selectTab(int index) {
        TabLayout.Tab tab = tabLayout.getTabAt(index);
        if (tab != null) {
            tab.select();
        }
    }

    private void checkLocationSettingsAndDetect() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 
                LOCATION_PERMISSION_REQUEST);
            return;
        }

        detectLocation();
    }

    private void detectLocation() {
        txtAddress.setText("Detecting location...");
        progressBar.setVisibility(View.VISIBLE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && 
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            progressBar.setVisibility(View.GONE);
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                isLocationDetected = true;
                txtGps.setText(String.format(Locale.getDefault(), "Lat: %.6f, Lng: %.6f", currentLatitude, currentLongitude));

                // Reverse geocoding
                Geocoder geocoder = new Geocoder(NearbyServicesActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        txtAddress.setText(addresses.get(0).getAddressLine(0));
                    } else {
                        txtAddress.setText("Address details unavailable");
                    }
                } catch (IOException e) {
                    txtAddress.setText("Offline (Geocoder failed)");
                }
            } else {
                txtAddress.setText("Failed to detect location (Mocking Center City)");
                txtGps.setText(String.format(Locale.getDefault(), "Lat: %.6f, Lng: %.6f (Mock)", currentLatitude, currentLongitude));
            }
            sortServicesByDistance();
            filterAndSearchServices();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                detectLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Mocking default coordinates.", Toast.LENGTH_SHORT).show();
                txtAddress.setText("Permission Denied (Mocking Center City)");
                txtGps.setText(String.format(Locale.getDefault(), "Lat: %.6f, Lng: %.6f (Mock)", currentLatitude, currentLongitude));
                sortServicesByDistance();
                filterAndSearchServices();
            }
        }
    }

    private void initializeData() {
        allServices.clear();
        // Seed initial services matching web portal
        allServices.add(new EmergencyService("s_h1", "City Central General Hospital", "Hospital", "102 Healthcare Ave, Center City", "108", 34.0522, -118.2437, true, true, "System"));
        allServices.add(new EmergencyService("s_h2", "St. Jude Emergency Center", "Hospital", "405 Mercy Blvd, South District", "108", 34.0622, -118.2337, true, true, "System"));
        allServices.add(new EmergencyService("s_p1", "District 1 Police Headquarters", "Police", "12 Civic Center Plaza", "112", 34.0532, -118.2417, true, true, "System"));
        allServices.add(new EmergencyService("s_f1", "Fire Station 9 (Downtown)", "Fire", "223 Responder St, Downtown", "101", 34.0512, -118.2457, true, true, "System"));
        allServices.add(new EmergencyService("s_a1", "Metro Life Support Ambulance", "Ambulance", "Central Dispatch Hub", "108", 34.0500, -118.2400, true, true, "System"));
        allServices.add(new EmergencyService("s_ph1", "Apex 24/7 Pharmacy", "Pharmacy", "55 Medication Way", "8888888888", 34.0550, -118.2460, false, true, "System"));
        allServices.add(new EmergencyService("s_sh1", "Community Red Cross Shelter", "Shelter", "77 Safety Boulevard", "112", 34.0480, -118.2380, true, true, "System"));

        // Fetch user favorites from database/local storage
        loadUserFavorites();
    }

    private void loadUserFavorites() {
        favoriteIds.clear();
        String userId = sessionManager.getUserId();
        if (userId == null) userId = "guest_user";
        
        firebaseHelper.getFavorites(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Parse documents
                // Bypassed locally in offline mode by checking local cache if empty
                loadFavoritesFromCache();
            } else {
                loadFavoritesFromCache();
            }
        });
    }

    private void loadFavoritesFromCache() {
        // Fetch from sharedPreferences to support Offline Demo Mode
        Set<String> cachedFavs = getSharedPreferences("cs_app_favs", MODE_PRIVATE).getStringSet("fav_ids", new HashSet<>());
        favoriteIds.addAll(cachedFavs);
        adapter.notifyDataSetChanged();
    }

    private void toggleFavoriteService(EmergencyService service, boolean isFavorite) {
        String userId = sessionManager.getUserId();
        if (userId == null) userId = "guest_user";

        if (isFavorite) {
            favoriteIds.add(service.getServiceId());
            FavoriteService fav = new FavoriteService(service.getServiceId(), service.getName(), service.getCategory(), 
                service.getLatitude(), service.getLongitude(), service.getAddress(), service.getPhone(), System.currentTimeMillis());
            
            firebaseHelper.addFavorite(userId, fav, task -> {});
            firebaseHelper.addSystemLog("FAVORITE_ADDED", "Added favorite: " + service.getName(), sessionManager.getUserDetails() != null ? sessionManager.getUserDetails().getEmail() : "Guest");
        } else {
            favoriteIds.remove(service.getServiceId());
            firebaseHelper.deleteFavorite(userId, service.getServiceId(), task -> {});
        }

        // Cache locally for offline demo persistence
        getSharedPreferences("cs_app_favs", MODE_PRIVATE)
            .edit()
            .putStringSet("fav_ids", favoriteIds)
            .apply();
    }

    private void sortServicesByDistance() {
        // Calculate distance in-place for all items using Haversine formula
        for (EmergencyService s : allServices) {
            double distance = calculateHaversineDistance(currentLatitude, currentLongitude, s.getLatitude(), s.getLongitude());
            s.setAddress(s.getAddress()); // preserve address
        }

        // Sort list
        Collections.sort(allServices, (s1, s2) -> {
            double d1 = calculateHaversineDistance(currentLatitude, currentLongitude, s1.getLatitude(), s1.getLongitude());
            double d2 = calculateHaversineDistance(currentLatitude, currentLongitude, s2.getLatitude(), s2.getLongitude());
            return Double.compare(d1, d2);
        });
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // in kilometers
    }

    private void filterAndSearchServices() {
        displayedServices.clear();
        String query = editSearch.getText().toString().trim().toLowerCase();
        
        // Tab mapping
        int tabPos = tabLayout.getSelectedTabPosition();
        String targetCategory = "All";
        if (tabPos == 1) targetCategory = "Hospital";
        else if (tabPos == 2) targetCategory = "Police";
        else if (tabPos == 3) targetCategory = "Fire";
        else if (tabPos == 4) targetCategory = "Ambulance";
        else if (tabPos == 5) targetCategory = "Pharmacy";
        else if (tabPos == 6) targetCategory = "Shelter";

        for (EmergencyService s : allServices) {
            // Apply category filter
            if (!"All".equals(targetCategory) && !s.getCategory().equalsIgnoreCase(targetCategory)) {
                continue;
            }
            // Apply search query
            if (!query.isEmpty() && !s.getName().toLowerCase().contains(query) && !s.getAddress().toLowerCase().contains(query)) {
                continue;
            }
            displayedServices.add(s);
        }

        adapter.notifyDataSetChanged();

        if (displayedServices.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
        } else {
            txtEmpty.setVisibility(View.GONE);
        }
    }

    private void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
        
        firebaseHelper.addSystemLog("EMERGENCY_CALL_INITIATED", "Dialed phone number: " + phoneNumber, 
            sessionManager.getUserDetails() != null ? sessionManager.getUserDetails().getEmail() : "Guest");
    }

    private void openDirectionsMap(double lat, double lng, String name) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + lat + "," + lng + "(" + Uri.encode(name) + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback to web browser directions
            Intent webIntent = new Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng));
            startActivity(webIntent);
        }

        firebaseHelper.addSystemLog("DIRECTIONS_OPENED", "Opened directions navigation for: " + name, 
            sessionManager.getUserDetails() != null ? sessionManager.getUserDetails().getEmail() : "Guest");
    }
}
