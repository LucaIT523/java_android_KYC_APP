package com.miniai.miniai;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GalleryActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;// Request code for picking an image from the gallery
    private static String apiType = null;
    private static Uri imagePath = null;           // URI to hold the image location
    public static Bitmap idcardImage = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gallery);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.gallerylayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Get the image URI passed from the previous activity
        imagePath = getIntent().getParcelableExtra("imagePath");
        apiType = getIntent().getStringExtra("apiType");
        if("face".equals(apiType)){
            //idcardImage =getIntent().getParcelableExtra("idcardImage");
            idcardImage = IDCardConfirmActivity.portraitImage;
            TextView titleTextView = findViewById(R.id.titleTextView);
            titleTextView.setText("Face Liveness Verify");
        }

        // Display the image using the URI
        displayImage(imagePath);
        // Set up a click listener on the FrameLayout to open the gallery for selecting a new image
        FrameLayout galleryFrame = findViewById(R.id.galleryImageFrame);
        galleryFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();// Open the gallery when the FrameLayout is clicked
            }
        });
        // Set up a click listener on the confirm button to send the image data to the server
        ImageButton confirmImageButton = findViewById(R.id.confirmImageButton);
        confirmImageButton.setOnClickListener(v -> {
            // Start the SelectAPIActivity and pass the image URI and type as extras
            Intent intent = null;
            if(apiType.equals("face")){
                intent = new Intent(this, FaceConfirmActivity.class);
            }else {
                intent = new Intent(this, IDCardConfirmActivity.class);
            }
            intent.putExtra("imageSource", "Gallery");
            intent.putExtra("imagePath", imagePath);
            startActivity(intent);
        });
    }
    // Method to open the gallery for selecting an image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);// Start the activity to pick an image
    }
    // Handle the result of the gallery activity (image selection)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imagePath = data.getData();      // Get the selected image URI
            displayImage(imagePath);         // Display the selected image
        }
    }
    // Method to display the selected image in an ImageView
    private void displayImage(Uri imageUri) {
        try {
            // Set the ImageView's URI to the selected image
            ImageView imageView = findViewById(R.id.galleryImageView);
            imageView.setImageURI(imageUri);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}