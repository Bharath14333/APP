package com.emergency.crisissense.activity;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.emergency.crisissense.R;
import com.emergency.crisissense.model.EmergencyService;
import com.emergency.crisissense.util.FirebaseHelper;
import com.emergency.crisissense.util.SessionManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double userLatitude;
    private double userLongitude;

    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    private List<EmergencyService> servicesList;
    private Map<Marker, EmergencyService> markerServiceMap;
    private String currentCategoryFilter = "All";

    // UI overlays
    private View layoutDetails;
    private TextView txtDetailsName, txtDetailsCategory, txtDetailsAddress, txtDetailsDistance, txtDetailsPhone;
    private Button btnCall, btnDirections;
    private ImageButton btnCloseDetails;
    private FloatingActionButton fabRecenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_map);

        // Get user lat/lng from intent
        userLatitude = getIntent().getDoubleExtra("user_lat", 34.0522);
        userLongitude = getIntent().getDoubleExtra("user_lng", -118.2437);

        firebaseHelper = new FirebaseHelper();
        sessionManager = new SessionManager(this);
        servicesList = new ArrayList<>();
        markerServiceMap = new HashMap<>();

        // Bind layouts
        layoutDetails = findViewById(R.id.layout_marker_details);
        txtDetailsName = findViewById(R.id.txt_details_name);
        txtDetailsCategory = findViewById(R.id.txt_details_category);
        txtDetailsAddress = findViewById(R.id.txt_details_address);
        txtDetailsDistance = findViewById(R.id.txt_details_distance);
        txtDetailsPhone = findViewById(R.id.txt_details_phone);
        btnCall = findViewById(R.id.btn_details_call);
        btnDirections = findViewById(R.id.btn_details_directions);
        btnCloseDetails = findViewById(R.id.btn_close_details);
        fabRecenter = findViewById(R.id.fab_recenter);

        // Back button
        findViewById(R.id.btn_map_back).setOnClickListener(v -> finish());

        // Close Bottom details
        btnCloseDetails.setOnClickListener(v -> layoutDetails.setVisibility(View.GONE));

        // Recenter click
        fabRecenter.setOnClickListener(v -> recenterCamera());

        // Setup filter button listeners
        setupFilterButtons();

        // Load mock services database (matching Dashboard)
        loadMockServices();

        // Obtain SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupFilterButtons() {
        findViewById(R.id.btn_filter_all).setOnClickListener(v -> applyCategoryFilter("All"));
        findViewById(R.id.btn_filter_hospitals).setOnClickListener(v -> applyCategoryFilter("Hospital"));
        findViewById(R.id.btn_filter_police).setOnClickListener(v -> applyCategoryFilter("Police"));
        findViewById(R.id.btn_filter_fire).setOnClickListener(v -> applyCategoryFilter("Fire"));
        findViewById(R.id.btn_filter_pharmacies).setOnClickListener(v -> applyCategoryFilter("Pharmacy"));
        findViewById(R.id.btn_filter_shelters).setOnClickListener(v -> applyCategoryFilter("Shelter"));
    }

    private void applyCategoryFilter(String category) {
        currentCategoryFilter = category;
        if (mMap != null) {
            renderMarkers();
        }
    }

    private void loadMockServices() {
        servicesList.clear();
        servicesList.add(new EmergencyService("s_h1", "City Central General Hospital", "Hospital", "102 Healthcare Ave, Center City", "108", 34.0522, -118.2437, true, true, "System"));
        servicesList.add(new EmergencyService("s_h2", "St. Jude Emergency Center", "Hospital", "405 Mercy Blvd, South District", "108", 34.0622, -118.2337, true, true, "System"));
        servicesList.add(new EmergencyService("s_p1", "District 1 Police Headquarters", "Police", "12 Civic Center Plaza", "112", 34.0532, -118.2417, true, true, "System"));
        servicesList.add(new EmergencyService("s_f1", "Fire Station 9 (Downtown)", "Fire", "223 Responder St, Downtown", "101", 34.0512, -118.2457, true, true, "System"));
        servicesList.add(new EmergencyService("s_a1", "Metro Life Support Ambulance", "Ambulance", "Central Dispatch Hub", "108", 34.0500, -118.2400, true, true, "System"));
        servicesList.add(new EmergencyService("s_ph1", "Apex 24/7 Pharmacy", "Pharmacy", "55 Medication Way", "8888888888", 34.0550, -118.2460, false, true, "System"));
        servicesList.add(new EmergencyService("s_sh1", "Community Red Cross Shelter", "Shelter", "77 Safety Boulevard", "112", 34.0480, -118.2380, true, true, "System"));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Custom map configurations
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Add markers
        renderMarkers();
        recenterCamera();

        // Marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            EmergencyService service = markerServiceMap.get(marker);
            if (service != null) {
                showMarkerDetails(service);
            }
            return false; // let default behavior occur (camera centers on marker)
        });
    }

    private void renderMarkers() {
        if (mMap == null) return;
        mMap.clear();
        markerServiceMap.clear();

        // 1. User Location marker
        LatLng userLatLng = new LatLng(userLatitude, userLongitude);
        mMap.addMarker(new MarkerOptions()
                .position(userLatLng)
                .title("My Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        // 2. Services markers
        for (EmergencyService s : servicesList) {
            // Apply category filter
            if (!"All".equals(currentCategoryFilter) && !s.getCategory().equalsIgnoreCase(currentCategoryFilter)) {
                continue;
            }

            float hueColor = BitmapDescriptorFactory.HUE_RED;
            String category = s.getCategory();
            if ("Hospital".equalsIgnoreCase(category)) {
                hueColor = BitmapDescriptorFactory.HUE_RED;
            } else if ("Police".equalsIgnoreCase(category)) {
                hueColor = BitmapDescriptorFactory.HUE_BLUE;
            } else if ("Fire".equalsIgnoreCase(category)) {
                hueColor = BitmapDescriptorFactory.HUE_ORANGE;
            } else if ("Ambulance".equalsIgnoreCase(category)) {
                hueColor = BitmapDescriptorFactory.HUE_CYAN;
            } else if ("Pharmacy".equalsIgnoreCase(category)) {
                hueColor = BitmapDescriptorFactory.HUE_GREEN;
            } else if ("Shelter".equalsIgnoreCase(category)) {
                hueColor = BitmapDescriptorFactory.HUE_VIOLET;
            }

            LatLng serviceLatLng = new LatLng(s.getLatitude(), s.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(serviceLatLng)
                    .title(s.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(hueColor)));

            if (marker != null) {
                markerServiceMap.put(marker, s);
            }
        }
    }

    private void recenterCamera() {
        if (mMap == null) return;
        LatLng userLatLng = new LatLng(userLatitude, userLongitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
    }

    private void showMarkerDetails(EmergencyService service) {
        txtDetailsName.setText(service.getName());
        txtDetailsCategory.setText(service.getCategory().toUpperCase());
        txtDetailsAddress.setText(service.getAddress());
        txtDetailsPhone.setText("Phone: " + service.getPhone());

        // Calculate distance dynamically
        float[] results = new float[1];
        Location.distanceBetween(userLatitude, userLongitude, service.getLatitude(), service.getLongitude(), results);
        float distanceInMeters = results[0];
        txtDetailsDistance.setText(String.format(Locale.getDefault(), "%.2f km away", distanceInMeters / 1000f));

        // Call button action
        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + service.getPhone()));
            startActivity(intent);

            firebaseHelper.addSystemLog("EMERGENCY_CALL_INITIATED", "Dialed phone number from Map: " + service.getPhone(), 
                sessionManager.getUserDetails() != null ? sessionManager.getUserDetails().getEmail() : "Guest");
        });

        // Directions button action
        btnDirections.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + service.getLatitude() + "," + service.getLongitude() + "(" + Uri.encode(service.getName()) + ")");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, 
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=" + service.getLatitude() + "," + service.getLongitude()));
                startActivity(webIntent);
            }

            firebaseHelper.addSystemLog("DIRECTIONS_OPENED", "Opened directions from Map for: " + service.getName(), 
                sessionManager.getUserDetails() != null ? sessionManager.getUserDetails().getEmail() : "Guest");
        });

        layoutDetails.setVisibility(View.VISIBLE);
    }
}
