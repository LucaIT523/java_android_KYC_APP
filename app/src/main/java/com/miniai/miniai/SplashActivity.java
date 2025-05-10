package com.miniai.miniai;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends Activity {
    TextView textViewStatus = null;
    ProgressBar progressBarInitialize = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        textViewStatus = findViewById(R.id.text_status);
        progressBarInitialize = findViewById(R.id.progress_initialize);
        waitLogo();
    }
    // Method to wait for the Logo page
    private void waitLogo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(350);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewStatus.setText("Please Wait ...");
                        }
                    });
                    Thread.sleep(350);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewStatus.setText("OK. You can start now!");
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBarInitialize.setVisibility(View.GONE);
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }
                });
            }
        }).start();
    }
}