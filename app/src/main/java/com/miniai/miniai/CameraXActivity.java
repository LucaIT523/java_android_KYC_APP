package com.miniai.miniai;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executors;
import android.widget.ImageView;

public class CameraXActivity extends AppCompatActivity {
    private static String apiType = null;
    public static Bitmap idcardImage = null;
    public static Bitmap faceImage = null;
    // cameraX variable.
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camerax);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.camerax_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initialize();
    }
    private void initialize(){
        try {
            apiType = getIntent().getStringExtra("apiType");
            if("face".equals(apiType)){
                idcardImage = IDCardConfirmActivity.portraitImage;
                ImageView cameraImageMask = findViewById(R.id.cameraImageMask);
                if (cameraImageMask != null) {
                    cameraImageMask.setImageDrawable(getDrawable(R.drawable.circular_mask));
                }
            }
            previewView = findViewById(R.id.cameraPreviewView);
            ImageButton captureButton = findViewById(R.id.cameraConfirmBtn);
            cameraExecutor = Executors.newSingleThreadExecutor();
            captureButton.setOnClickListener(v -> captureImage());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void startIntent(){
        Intent intent = null;
        if("face".equals(apiType)){
            intent = new Intent(this, FaceConfirmActivity.class);
        }else {
            intent = new Intent(this, IDCardConfirmActivity.class);
        }
        intent.putExtra("imageSource", "Camera");
        startActivity(intent);
    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        try {
            Preview preview = new Preview.Builder().build();
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();
            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                    .build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());
            Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void captureImage() {
        if (imageCapture != null) {
            imageCapture.takePicture(cameraExecutor, new ImageCapture.OnImageCapturedCallback() {
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy image) {
                    Bitmap bitmap = null;
                    try{
                        // Convert ImageProxy to Bitmap
                        bitmap = imageToBitmap(image);
                        // Adjust the orientation of the Bitmap if needed
                        int rotationDegrees = image.getImageInfo().getRotationDegrees();
                        if (rotationDegrees != 0) {
                            Matrix matrix = new Matrix();
                            matrix.postRotate(rotationDegrees);
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        }
                        else if(bitmap.getHeight() != image.getHeight()){
                            Matrix matrix = new Matrix();
                            matrix.postRotate(90);
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        }
                    } finally {
                        // Ensure that the ImageProxy is closed after processing
                        image.close();
                        if (bitmap != null) {
                            faceImage = bitmap;
                            startIntent();
                        }else{
                            faceImage = null;
                        }
                    }
                }

                @Override
                public void onError(@NonNull ImageCaptureException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    // Helper method to convert ImageProxy to Bitmap
    private Bitmap imageToBitmap(ImageProxy image) {
        // Get the image buffer
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        // Convert byte array to Bitmap
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    @Override
    protected void onResume() {
        super.onResume();
        faceImage = null;
        startCamera(); // Re-initialize camera on resume
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        idcardImage = null;
        faceImage = null;
        if (cameraExecutor != null) {
            cameraExecutor.shutdown(); // Properly shut down the executor
        }
    }
    private void stopCamera() {
        // Obtain an instance of the ProcessCameraProvider
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                // Get the camera provider
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                // Unbind all use cases to stop the camera
                cameraProvider.unbindAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
}