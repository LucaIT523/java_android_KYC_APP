package com.miniai.miniai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    // Constants to manage permission requests
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int PICK_IMAGE = 1;
    private int sourceType = 0;         // Type of source selected by the user(0:Camera, 1:Gallery)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainlayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setEvent();
    }
    private void setEvent(){
        RadioGroup sourceRadioGroup = findViewById(R.id.sourceRadioGroup);

        sourceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Find which RadioButton was selected
            RadioButton selectedScenarioButton = findViewById(checkedId);
            if (selectedScenarioButton.getId() == R.id.cameraRadioButton) {
                sourceType = 0;
            } else if (selectedScenarioButton.getId() == R.id.galleryRadioButton) {
                sourceType = 1;
            } else {
                sourceType = -1;
            }
        });
        // Set up a click listener on the confirm button
        ImageButton confirmImageButton = findViewById(R.id.confirmImageButton);
        confirmImageButton.setOnClickListener(v -> {
            if(sourceType < 0)
                return;
            if(sourceType == 0){
                // Check if the camera permission is already granted
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // If not, request the CAMERA permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                } else {
                    // If permission is granted, proceed to the CamActivity
                    Intent intent = new Intent(MainActivity.this, CameraXActivity.class);
                    intent.putExtra("apiType", "idCard");
                    startActivity(intent);
                }
            }else if(sourceType == 1){
                openGallery();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to the next activity
                Intent intent = new Intent(MainActivity.this, CameraXActivity.class);
                intent.putExtra("apiType", "idCard");
                startActivity(intent);
            } else {
                // Handle permission denial
            }
        }
    }
    // Method to open the gallery for image selection
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }
    // Handle the result of the gallery activity (image selection)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            // Get the selected image URI
            Uri imagePath = data.getData();
            // Pass the image URI to the GalleryActivity and start it
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            intent.putExtra("apiType", "idCard");
            intent.putExtra("imagePath", imagePath);
            startActivity(intent);
        }
    }
}