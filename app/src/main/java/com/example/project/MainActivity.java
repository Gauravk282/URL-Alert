package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_DATASET = "dataset";
    private static final String DATASET_PATH = "D:\\Downloads\\archive\\malicious_phish.csv";

    private List<Message> messages;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;
    private Button newChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        newChatButton = findViewById(R.id.new_chat_button);

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        // New message button click listener
        newChatButton.setOnClickListener(v -> openContacts());

        // Read dataset and store it in SharedPreferences
        processDataset();

        // Retrieve stored dataset
        String storedDataset = getDataset();
        if (storedDataset != null) {
            Log.d("MainActivity", "Stored Dataset: " + storedDataset);
        }

        // Handle intent that contains the message details if any
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("message_sender") && intent.hasExtra("message_body")) {
            String sender = intent.getStringExtra("message_sender");
            String body = intent.getStringExtra("message_body");
            addMessage(sender, body);
        }
    }

    // Read dataset file and store in SharedPreferences
    private void processDataset() {
        File datasetFile = new File(DATASET_PATH);
        if (!datasetFile.exists()) {
            Log.e("DatasetManager", "Dataset file not found!");
            return;
        }

        String datasetContent = readFile(datasetFile);
        if (datasetContent != null) {
            saveDataset(datasetContent);
        }
    }

    // Read file content
    private String readFile(File file) {
        StringBuilder text = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return text.toString();
    }

    // Save dataset in SharedPreferences
    private void saveDataset(String dataset) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_DATASET, dataset);
        editor.apply();
    }

    // Retrieve dataset from SharedPreferences
    private String getDataset() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_DATASET, null);
    }

    // Add the message to the list and show toast if the message contains a URL
    public void addMessage(String sender, String content) {
        Message message = new Message(sender, content);
        messages.add(message);
        messageAdapter.notifyItemInserted(messages.size() - 1);
        recyclerView.smoothScrollToPosition(messages.size() - 1);

        if (containsUrl(content)) {
            Toast.makeText(this, "This message contains a URL", Toast.LENGTH_SHORT).show();
        }
    }

    // Check if the message contains a URL
    private boolean containsUrl(String message) {
        return message.contains("http://") || message.contains("https://");
    }

    // Open contacts to start a new chat
    private void openContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        intent.setType("vnd.android.cursor.dir/contact");
        startActivityForResult(intent, 100);
    }

    // Show alert dialog when a URL is clicked
    private void showUrlWarningDialog(String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("URL Detected")
                .setMessage("This message contains a URL: " + url + ". Proceed with caution!")
                .setPositiveButton("Proceed", (dialog, id) -> openUrlInBrowser(url))
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                .setCancelable(false);
        builder.create().show();
    }

    // Open the URL in the browser
    private void openUrlInBrowser(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
