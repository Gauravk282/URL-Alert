package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_DATASET = "dataset";
    private static final String DATASET_FILE_NAME = "malicious_phish.csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(this::processDataset).start(); // Load dataset in background
        handleIncomingIntent(getIntent());
    }

    private void processDataset() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try (InputStream inputStream = getAssets().open(DATASET_FILE_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                editor.putString(KEY_DATASET + "_" + count, line); // Store in smaller chunks
                count++;

                if (count % 100 == 0) { // Apply in batches to reduce memory usage
                    editor.apply();
                }
            }
            editor.apply(); // Final apply after loop
            Log.d("DatasetManager", "Dataset loaded successfully in chunks!");

        } catch (IOException e) {
            Log.e("DatasetManager", "Error reading dataset!", e);
        }
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null && intent.hasExtra("message_body")) {
            String body = intent.getStringExtra("message_body");
            if (containsUrl(body)) {
                Toast.makeText(this, "Message contains a URL", Toast.LENGTH_SHORT).show();
                showUrlWarningDialog(body);
            }
        }
    }

    private boolean containsUrl(String message) {
        return message.contains("http://") || message.contains("https://");
    }

    private void showUrlWarningDialog(String url) {
        new AlertDialog.Builder(this)
                .setTitle("URL Detected")
                .setMessage("This message contains a URL: " + url)
                .setPositiveButton("Proceed", (dialog, id) -> openUrl(url))
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
