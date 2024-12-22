package com.example.moodt_tracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner moodSpinner;
    private EditText notesEditText;
    private Button saveButton, viewHistoryButton, shareButton, openCameraButton, openMapButton;
    private ArrayList<String> moodEntries;
    private String currentPhotoPath;

    // Request camera permission
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera(); // Proceed to open the camera
                } else {
                    Toast.makeText(MainActivity.this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                }
            });

    // Handle the result of the camera intent
    private final ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Photo successfully captured
                    Toast.makeText(MainActivity.this, "Photo saved: " + currentPhotoPath, Toast.LENGTH_SHORT).show();
                    Log.d("Camera", "Photo saved at: " + currentPhotoPath);
                } else {
                    Toast.makeText(MainActivity.this, "Failed to capture photo", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        moodSpinner = findViewById(R.id.moodSpinner);
        notesEditText = findViewById(R.id.notesEditText);
        saveButton = findViewById(R.id.saveButton);
        viewHistoryButton = findViewById(R.id.viewHistoryButton);
        shareButton = findViewById(R.id.shareButton);
        openCameraButton = findViewById(R.id.openCameraButton);
        openMapButton = findViewById(R.id.openMapButton);

        moodEntries = new ArrayList<>();

        // Set up listeners for buttons
        saveButton.setOnClickListener(v -> saveMoodEntry());
        viewHistoryButton.setOnClickListener(v -> viewMoodHistory());
        shareButton.setOnClickListener(v -> shareMoodEntry());

        openCameraButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Request camera permission
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            } else {
                openCamera(); // Permission already granted for older versions
            }
        });

        openMapButton.setOnClickListener(v -> openMap());
    }

    // Save mood entry to the list
    private void saveMoodEntry() {
        String mood = moodSpinner.getSelectedItem().toString();
        String notes = notesEditText.getText().toString().trim();
        if (notes.isEmpty()) {
            Toast.makeText(this, "Please enter some notes", Toast.LENGTH_SHORT).show();
        } else {
            String entry = "Mood: " + mood + ", Notes: " + notes;
            moodEntries.add(entry);
            Toast.makeText(this, "Mood saved", Toast.LENGTH_SHORT).show();
            Log.d("MoodEntry", entry);
            notesEditText.setText("");
        }
    }

    // Open the Mood History Activity
    private void viewMoodHistory() {
        Intent intent = new Intent(this, MoodHistoryActivity.class);
        intent.putStringArrayListExtra("moodEntries", moodEntries);
        startActivity(intent);
    }

    // Share mood entry using an intent
    private void shareMoodEntry() {
        String mood = moodSpinner.getSelectedItem().toString();
        String notes = notesEditText.getText().toString().trim();
        if (notes.isEmpty()) {
            Toast.makeText(this, "Please enter some notes", Toast.LENGTH_SHORT).show();
        } else {
            String shareText = "I'm feeling " + mood + ". Notes: " + notes;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share Mood"));
        }
    }

    // Open the camera to take a photo
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
                return; // Exit if file creation failed
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.moodt_tracker.provider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureLauncher.launch(cameraIntent);
            }
        } else {
            Toast.makeText(this, "No Camera App Found", Toast.LENGTH_SHORT).show();
        }
    }

    // Create a file for the photo
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir("Pictures");
        if (storageDir == null) {
            throw new IOException("Storage directory not available");
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Open map to view mood tracking locations
    private void openMap() {
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        mapIntent.setData(Uri.parse("geo:37.7749,-122.4194?q=mood+tracker+locations")); // Example coordinates
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "No map application found", Toast.LENGTH_SHORT).show();
        }
    }
}
