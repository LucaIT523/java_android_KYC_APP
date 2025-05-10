package com.miniai.miniai;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.miniai.miniai.confirmfragment.ConfirmFaceFragment;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FaceConfirmActivity extends AppCompatActivity {
    private static String imageSource;              // Type of the image source ("Camera" or "Gallery")
    public static Bitmap idcardImage = null;
    private static Uri imagePath = null;            // Path of the image (if from gallery)
    private static byte[] imageBytes = null;         // Byte array to hold the image data
    private static View loadingOverlay;
    private static String responseData = null;
    private static Bitmap sourceBitmap = null;
    public static Bitmap portraitImage = null;
    private static int faceCount = 1;
    private static ViewPager mViewPager = null;
    private static Fragment[] PAGES = null;
    private static String[] PAGE_TITLES = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_face_confirm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.face_confirm_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setLoadingOverlay();
        idcardImage = null;
        portraitImage = null;
        responseData = null;
        // Get the type of the image source (either "Camera" or "Gallery")
        imageSource = getIntent().getStringExtra("imageSource");
        // Retrieve the image data based on the source type
        if(imageSource.equals("Camera")){
            imageBytes = cnvBitmapToByteArray(CameraXActivity.faceImage);// Get the image data if image is from camera
            idcardImage =CameraXActivity.idcardImage;
        } else{
            imagePath = getIntent().getParcelableExtra("imagePath");  // Get URI if image is from gallery
            idcardImage =GalleryActivity.idcardImage;
            imageBytes = cnvUriToByteArray(imagePath);   // Convert the image URI to byte data
        }
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
                    URL url = new URL(ApiConfig.FACE_SERVER_ADDRESS + ApiConfig.FACE_LIVENESS_CHCECK);
                    // Open a connection to the server
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    // Configure the connection for POST
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(20000);  // 10 seconds timeout for connection
                    conn.setReadTimeout(20000);     // 10 seconds timeout for reading the input
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
    private void setEvent(){
        // Set up a click listener on the confirm button
        ImageButton confirmImageButton = findViewById(R.id.confirmImageButton);
        confirmImageButton.setOnClickListener(v -> {
            if(portraitImage == null || idcardImage == null){
                Toast.makeText(this, "There is no face image. Try again...", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CompareActivity.class);
            startActivity(intent);
        });
    }
    private void setConfirmData(){
        try {
            JSONParser jsonParser = new JSONParser();
            faceCount = jsonParser.getFaceCount(responseData);
            PAGES = new Fragment[faceCount];
            PAGE_TITLES = new String[faceCount];
            for(int i = 0 ; i <  faceCount; i++){
                PAGES[i] = new ConfirmFaceFragment();
                PAGE_TITLES[i] = "Face Detail Data(" + Integer.toString(i +1) + ")";
            }

            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_panel);
            tabLayout.setupWithViewPager(mViewPager);

            mViewPager.setOffscreenPageLimit(faceCount - 1);

            sourceBitmap = getImage();
            for(int i = 0 ; i <  faceCount; i++){
                ArrayList<TextData> textData = jsonParser.getFaceDetailData(responseData,  i+1);
                ((ConfirmFaceFragment)PAGES[i]).setTextData(textData);

                Rect faceRect = getPosition(i+1);
                ((ConfirmFaceFragment)PAGES[i]).setImage(drawRectangleOnBitmap(sourceBitmap, faceRect));
                ((ConfirmFaceFragment)PAGES[i]).setContext(this, i+1);
            }
            setBaseData(1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void setBaseData(int faceID) {
        try{
            JSONParser jsonParser = new JSONParser();
            String livenessCheck = jsonParser.getKeyValueFromFaceStateData(responseData, faceID, "LivenessCheck");
            if (livenessCheck == null) {
                livenessCheck = "Unknown";
            }
            livenessCheck += " Face";
            TextView livenessCheckTextView = findViewById(R.id.livenessCheckTextView);
            livenessCheckTextView.setText(livenessCheck);   // Display the name in the nameTextView
            String sex = jsonParser.getKeyValueFromFaceStateData(responseData, faceID, "Gender");
            if (sex == null) {
                sex = "Unknown Sex";
            }
            TextView sexTextView = findViewById(R.id.sexTextView);
            sexTextView.setText(sex);   // Display the name in the nameTextView
            String age = jsonParser.getKeyValueFromFaceStateData(responseData, faceID,"Age");
            if (age == null) {
                age = "?";
            }
            TextView ageTextView = findViewById(R.id.ageTextView);
            ageTextView.setText(age);   // Display the name in the nameTextView
            Rect faceRect = getPosition(faceID);
            if( faceRect.width() > 0 && faceRect.height() > 0){
                portraitImage = cropBitmap(sourceBitmap, faceRect);
                if(portraitImage != null){
                    ImageView portraitImageView = findViewById(R.id.portraitImageView);
                    portraitImageView.setImageBitmap(portraitImage);   // Display the image in the portraitImageView
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /* PagerAdapter for supplying the ViewPager with the pages (fragments) to display. */
    public class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager );
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
    public Rect getPosition(int faceID){
        Rect faceRect = new Rect();
        try{
            int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
            JSONParser jsonParser = new JSONParser();
            String pos = jsonParser.getKeyValueFromFaceDetailData(responseData, faceID,"x1");
            if(pos != null)
                x1 = Integer.parseInt(pos);
            pos = jsonParser.getKeyValueFromFaceDetailData(responseData, faceID,"x2");
            if(pos != null)
                x2 = Integer.parseInt(pos);
            pos = jsonParser.getKeyValueFromFaceDetailData(responseData, faceID,"y1");
            if(pos != null)
                y1 = Integer.parseInt(pos);
            pos = jsonParser.getKeyValueFromFaceDetailData(responseData, faceID,"y2");
            if(pos != null)
                y2 = Integer.parseInt(pos);
            faceRect = new Rect(x1, y1, x2, y2);
        }catch (Exception e){
            e.printStackTrace();
        }
        return faceRect;
    }
    public Bitmap drawRectangleOnBitmap(Bitmap bitmap, Rect rect) {
        // Create a mutable bitmap (if the original one isn't mutable)
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        // Create a Canvas object to draw on the bitmap
        Canvas canvas = new Canvas(mutableBitmap);

        // Create a Paint object to define the style of the rectangle
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);       // Set the color of the rectangle
        paint.setStyle(Paint.Style.STROKE); // Set the style to stroke (outline)
        paint.setStrokeWidth(3);          // Set the stroke width

        // Draw the rectangle on the canvas
        canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, paint);

        return mutableBitmap;
    }
    public Bitmap cropBitmap(Bitmap sourceBitmap, Rect rect) {
        if(sourceBitmap != null){
            int x, y, w, h;
            x = rect.left - rect.width() /3;
            y = rect.top - rect.height() /3;
            w = rect.width() * 5/3;
            h = rect.height() * 5/3;
            if( x < 0) x = 0;
            if(y < 0) y = 0;
            if(w > sourceBitmap.getWidth()) w = sourceBitmap.getWidth();
            if(h > sourceBitmap.getHeight()) h = sourceBitmap.getHeight();
            return Bitmap.createBitmap(sourceBitmap, x, y, w, h);
        }
        return null;
    }
    private Bitmap getImage(){
        try {
            if(imageSource.equals("Camera")){
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                return bitmap;
            }else{
                Bitmap bitmap = getBitmapFromUri(this, imagePath);
                return bitmap;
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        Bitmap bitmap = null;
        ContentResolver contentResolver = context.getContentResolver();
        InputStream inputStream = null;

        try {
            // Open the InputStream from the URI
            inputStream = contentResolver.openInputStream(uri);

            // Decode the InputStream into a Bitmap
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            bitmap = null;
            e.printStackTrace();
        } finally {
            // Always close the InputStream
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }
}