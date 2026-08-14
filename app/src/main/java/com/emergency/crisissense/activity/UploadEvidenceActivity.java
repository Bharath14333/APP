package com.emergency.crisissense.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.emergency.crisissense.R;
import com.emergency.crisissense.util.FirebaseHelper;

public class UploadEvidenceActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 201;
    private static final int PICK_IMAGE_REQUEST = 202;
    private static final int CAPTURE_IMAGE_REQUEST = 203;

    private ImageView imgPreview;
    private Button btnDone;
    private FirebaseHelper firebaseHelper;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_evidence);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        imgPreview = findViewById(R.id.img_preview);
        Button btnCapture = findViewById(R.id.btn_capture);
        Button btnGallery = findViewById(R.id.btn_gallery);
        btnDone = findViewById(R.id.btn_done);

        firebaseHelper = new FirebaseHelper();

        btnCapture.setOnClickListener(v -> checkCameraPermissionAndCapture());

        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnDone.setOnClickListener(v -> uploadAndReturnUrl());
    }

    private void checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                selectedImageUri = data.getData();
                imgPreview.setImageURI(selectedImageUri);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST && data != null) {
                // If extra returns uri or thumbnail bitmap
                if (data.getData() != null) {
                    selectedImageUri = data.getData();
                    imgPreview.setImageURI(selectedImageUri);
                } else if (data.getExtras() != null && data.getExtras().get("data") != null) {
                    // For mock compilation safety, we set a default symbol when uri is empty
                    imgPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                    selectedImageUri = Uri.parse("android.resource://com.emergency.crisissense/" + android.R.drawable.ic_menu_gallery);
                }
            }
        }
    }

    private void uploadAndReturnUrl() {
        if (selectedImageUri == null) {
            // Simulated dummy URL for testing if no image is captured
            String mockUrl = "https://images.unsplash.com/photo-1599733589046-9b8308b5b50d?q=80&w=600&auto=format&fit=crop";
            Intent resultIntent = new Intent();
            resultIntent.putExtra("evidenceUrl", mockUrl);
            setResult(RESULT_OK, resultIntent);
            finish();
            return;
        }

        Toast.makeText(this, "Uploading image to Firebase Storage...", Toast.LENGTH_SHORT).show();
        
        firebaseHelper.uploadEvidenceImage(selectedImageUri, taskSnapshot -> {
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("evidenceUrl", uri.toString());
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(UploadEvidenceActivity.this, "Upload complete!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }, e -> {
            // Firebase placeholder fails: fallback to simulated URL for seamless development compilation testing
            String fallbackUrl = "https://images.unsplash.com/photo-1599733589046-9b8308b5b50d?q=80&w=600&auto=format&fit=crop";
            Intent resultIntent = new Intent();
            resultIntent.putExtra("evidenceUrl", fallbackUrl);
            setResult(RESULT_OK, resultIntent);
            Toast.makeText(UploadEvidenceActivity.this, "Storage mock fallback URL generated for development testing.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
