package com.miniai.miniai;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
import com.miniai.miniai.confirmfragment.ConfirmTextDataFragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CompareActivity extends AppCompatActivity {
    private Bitmap idcardImage = null;
    private Bitmap faceImage = null;
    private View loadingOverlay;
    private static String responseData = null;
    ViewPager mViewPager;
    private final Fragment[] PAGES = new Fragment[]{
            new ConfirmTextDataFragment(),
            new ConfirmTextDataFragment(),
    };
    private final String[] PAGE_TITLES = new String[] {
            "Face 1",
            "Face 2"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compare);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.compare_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setLoadingOverlay();
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_panel);
        tabLayout.setupWithViewPager(mViewPager);
        faceImage = FaceConfirmActivity.portraitImage;
        idcardImage = FaceConfirmActivity.idcardImage;
        drawImages();
        // Send the image data to the server asynchronously
        new SendPostRequest().execute();
    }
    private void drawImages(){
        if(faceImage != null || idcardImage != null){
            ImageView idcardImageView = findViewById(R.id.idcardImageView);
            ImageView faceImageView = findViewById(R.id.faceImageView);
            faceImageView.setImageBitmap(faceImage);   // Display the face Image in the faceImageView
            idcardImageView.setImageBitmap(idcardImage);
        }
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
    // Convert Bitmap to byte[]
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); // You can choose the format: PNG, JPEG, etc.
            return stream.toByteArray();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    // AsyncTask to send the image data to a server via POST request
    private class SendPostRequest extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            showLoadingOverlay(); // Show the loading overlay
            String response = "";
            String boundary = "------Boundary"; // Boundary used for multipart/form-data
            int retries = 3;                    // Number of retries in case of failure
            while (retries > 0) {
                try {
                    // URL of the server endpoint
                    URL url = new URL(ApiConfig.FACE_SERVER_ADDRESS + ApiConfig.FACE_COMPARE);
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
                    dos.writeBytes("Content-Disposition: form-data; name=\"file1\"; filename=\"image1.jpg\"\r\n");
                    dos.writeBytes("Content-Type: image/jpeg\r\n");
                    dos.writeBytes("\r\n");
                    dos.write(getBytesFromBitmap(idcardImage));// Write the id card image data
                    dos.writeBytes("\r\n");

                    dos.writeBytes("--" + boundary + "\r\n");
                    dos.writeBytes("Content-Disposition: form-data; name=\"file2\"; filename=\"image2.jpg\"\r\n");
                    dos.writeBytes("Content-Type: image/jpeg\r\n");
                    dos.writeBytes("\r\n");
                    dos.write(getBytesFromBitmap(faceImage));// Write the face image data
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
            ArrayList<TextData> idcardTextData = jsonParser.getTextDataList(responseData,  "face1");
            ((ConfirmTextDataFragment)PAGES[0]).setTextData(idcardTextData);
            ArrayList<TextData> faceTextData = jsonParser.getTextDataList(responseData,  "face2");
            ((ConfirmTextDataFragment)PAGES[1]).setTextData(faceTextData);
            setBaseData();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void setBaseData() {
        try{
            JSONParser jsonParser = new JSONParser();
            String compare_result = jsonParser.getKeyValue(new JSONObject(responseData), "compare_result");
            if (compare_result == null) {
                compare_result = "Unknown Result";
            }
            compare_result = compare_result.replaceAll("\\.$", "");
            TextView resultTextView = findViewById(R.id.resultTextView);
            resultTextView.setText(compare_result);   // Display the result in the resultTextView
            String compare_similarity = jsonParser.getKeyValue(new JSONObject(responseData), "compare_similarity");
            if (compare_similarity == null) {
                compare_similarity = "0.0";
            }
            double value = Double.parseDouble(compare_similarity);
            compare_similarity = String.format("%.5f", value); // Format to 4 decimal places
            TextView similarityTextView = findViewById(R.id.similarityTextView);
            similarityTextView.setText(compare_similarity);   // Display the similarity in the similarityTextView
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void setEvent(){
        ImageView homeImageView = findViewById(R.id.homeButton);
        homeImageView.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
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
}