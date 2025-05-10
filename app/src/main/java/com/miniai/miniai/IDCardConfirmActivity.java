package com.miniai.miniai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.miniai.miniai.confirmfragment.ConfirmImageFragment;
import com.miniai.miniai.confirmfragment.ConfirmTextDataFragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class IDCardConfirmActivity extends AppCompatActivity {
    // Constants to manage permission requests
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int PICK_IMAGE = 1;
    private int sourceType = 0;         // Type of source selected by the user(0:Camera, 1:Gallery)
    private String imageSource;              // Type of the image source ("Camera" or "Gallery")
    private Uri imagePath = null;            // Path of the image (if from gallery)
    private byte[] imageBytes = null;         // Byte array to hold the image data
    public static Bitmap portraitImage = null;
    private View loadingOverlay;
    private static String responseData = null;
    ViewPager mViewPager;
    private final Fragment[] PAGES = new Fragment[]{
            new ConfirmTextDataFragment(),
            new ConfirmImageFragment(),
    };
    private final String[] PAGE_TITLES = new String[] {
            "TEXT DATA",
            "IMAGES"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_idcard_confirm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.idcardconfirmlayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setLoadingOverlay();
        portraitImage = null;
        responseData = null;
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_panel);
        tabLayout.setupWithViewPager(mViewPager);

        // Get the type of the image source (either "Camera" or "Gallery")
        imageSource = getIntent().getStringExtra("imageSource");
        // Retrieve the image data based on the source type
        if(imageSource.equals("Camera")){
            imageBytes = cnvBitmapToByteArray(CameraXActivity.faceImage);// Get the image data if image is from camera
        } else{
            imagePath = getIntent().getParcelableExtra("imagePath");  // Get URI if image is from gallery
            imageBytes = cnvUriToByteArray(imagePath);   // Convert the image URI to byte data
        };
        // Send the image data to the server asynchronously
        new SendPostRequest().execute(imageBytes);
    }
    private void setLoadingOverlay(){
        // Inflate the loading overlay layout
        loadingOverlay = getLayoutInflater().inflate(R.layout.loading_overlay, null);

        // Add the overlay to the current view
        addContentView(loadingOverlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Initially hide the overlay
        loadingOverlay.setVisibility(View.GONE);
    }
    // Method to show the loading overlay
    private void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }
    }
    // Method to hide the loading overlay
    private void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
    }
    // Convert Bitmap to byte array
    private byte[] cnvBitmapToByteArray(Bitmap bitmap) {
        if(bitmap == null)
            return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); // You can choose other formats as needed
        return stream.toByteArray();
    }
    // convert the image URI to byte array
    private byte[] cnvUriToByteArray(Uri uri) {
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            // Open an input stream from the URI
            inputStream = getContentResolver().openInputStream(uri);
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            // Read the data from the input stream into the byte array output stream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                // Close streams
                if (inputStream != null) inputStream.close();
                if (byteArrayOutputStream != null) byteArrayOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        // Convert the byte array output stream to a byte array
        return byteArrayOutputStream.toByteArray();
    }
    // AsyncTask to send the image data to a server via POST request
    private class SendPostRequest extends AsyncTask<byte[], Void, String> {
        @Override
        protected String doInBackground(byte[]... params) {
            showLoadingOverlay(); // Show the loading overlay
            byte[] image = params[0];
            String response = "";
            String boundary = "------Boundary"; // Boundary used for multipart/form-data
            int retries = 3;                    // Number of retries in case of failure
            while (retries > 0) {
                try {
                    // URL of the server endpoint
                    URL url = new URL(ApiConfig.IDCARD_SERVER_ADDRESS + ApiConfig.IDCARD_CHECK);
                    // Open a connection to the server
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    // Configure the connection for POST
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);  // 10 seconds timeout for connection
                    conn.setReadTimeout(10000);     // 10 seconds timeout for reading the input
                    // Write the multipart form data
                    OutputStream os = conn.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);
                    // Start of multipart/form-data.
                    dos.writeBytes("--" + boundary + "\r\n");
                    dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n");
                    dos.writeBytes("Content-Type: image/jpeg\r\n");
                    dos.writeBytes("\r\n");
                    // Write the image data
                    dos.write(image);
                    dos.writeBytes("\r\n");
                    // End of multipart/form-data.
                    dos.writeBytes("--" + boundary + "--\r\n");
                    dos.flush();
                    dos.close();
                    // Read the response from the server
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    // Close the BufferedReader
                    in.close();
                    conn.disconnect();
                    // Set the response
                    response = content.toString();
                    break;// Exit the loop if the request is successful
                } catch (Exception e) {
                    retries--; // Decrement retry count
                    e.printStackTrace();
                }
            }
            return response;// Return the response from the server
        }
        @Override
        protected void onPostExecute(String result) {
            responseData = result;
            setConfirmData();
            hideLoadingOverlay();
            setEvent();
        }
    }
    private void setConfirmData(){
        try {
            JSONParser jsonParser = new JSONParser();
            ArrayList<TextData> textData = jsonParser.getTextDataList(new JSONObject(responseData),  null);
            ((ConfirmTextDataFragment)PAGES[0]).setTextData(textData);
            ArrayList<ImageData> imageData = jsonParser.getImageDataList(new JSONObject(responseData));
            ((ConfirmImageFragment)PAGES[1]).setImageData(imageData);
            setBaseData();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void setBaseData() {
        try{
            JSONParser jsonParser = new JSONParser();
            String name = jsonParser.getKeyValue(new JSONObject(responseData), "Full Name");
            if (name == null) {
                name = "Unknown Name";
            }
            TextView nameTextView = findViewById(R.id.nameTextView);
            nameTextView.setText(name);   // Display the name in the nameTextView
            String sex = jsonParser.getKeyValue(new JSONObject(responseData), "Sex");
            if (sex == null) {
                sex = "Unknown Sex";
            } else if(sex.equals("M")){
                sex = "Male";
            } else{
                sex = "Female";
            }
            TextView sexTextView = findViewById(R.id.sexTextView);
            sexTextView.setText(sex);   // Display the name in the nameTextView
            String age = jsonParser.getKeyValue(new JSONObject(responseData), "Age");
            if (age == null) {
                age = "?";
            }
            TextView ageTextView = findViewById(R.id.ageTextView);
            ageTextView.setText(age);   // Display the name in the nameTextView
            portraitImage = ((ConfirmImageFragment)PAGES[1]).getImage("Portrait111");
            if(portraitImage != null){
                ImageView portraitImageView = findViewById(R.id.portraitImageView);
                portraitImageView.setImageBitmap(portraitImage);   // Display the image in the portraitImageView
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /* PagerAdapter for supplying the ViewPager with the pages (fragments) to display. */
    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return PAGES[position];
        }

        @Override
        public int getCount() {
            return PAGES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return PAGE_TITLES[position];
        }

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
            if(portraitImage == null || sourceType < 0){
                Toast.makeText(this, "There is no face image. Try again...", Toast.LENGTH_SHORT).show();
                return;
            }
            if(sourceType == 0){
                // Check if the camera permission is already granted
                if (ContextCompat.checkSelfPermission(IDCardConfirmActivity.this, android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // If not, request the CAMERA permission
                    ActivityCompat.requestPermissions(IDCardConfirmActivity.this,
                            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                } else if(portraitImage != null){
                    // If permission is granted, proceed to the CamActivity
                    Intent intent = new Intent(IDCardConfirmActivity.this, CameraXActivity.class);
                    //intent.putExtra("idcardImage", portraitImage);
                    intent.putExtra("apiType", "face");
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && portraitImage != null) {
                // Permission granted, proceed to the next activity
                Intent intent = new Intent(this, CameraXActivity.class);
                //intent.putExtra("idcardImage", portraitImage);
                intent.putExtra("apiType", "face");
                startActivity(intent);
            } else {
                // Handle permission denial
                ;
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
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && portraitImage != null) {
            // Get the selected image URI for face liveness
            Uri imagePath = data.getData();
            // Pass the image URI to the GalleryActivity and start it
            Intent intent = new Intent(this, GalleryActivity.class);
            //intent.putExtra("idcardImage", portraitImage);
            intent.putExtra("apiType", "face");
            intent.putExtra("imagePath", imagePath);
            startActivity(intent);
        }
    }
}