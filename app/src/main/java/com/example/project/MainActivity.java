package com.example.project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;
    private List<Message> messages;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        searchBar = findViewById(R.id.search_bar);
        Button newChatButton = findViewById(R.id.new_chat_button);

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        newChatButton.setOnClickListener(v -> openContacts());

        if (checkPermissions()) {
            loadMessages();
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        int smsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
        int contactPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        return smsPermission == PackageManager.PERMISSION_GRANTED && contactPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_CONTACTS
        }, SMS_PERMISSION_CODE);
    }

    private void loadMessages() {
        if (!checkPermissions()) {
            Toast.makeText(this, "SMS and Contacts permissions are required to load messages.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (messages.isEmpty()) {
            displayNoMessages();
        } else {
            messageAdapter.notifyDataSetChanged();
        }
    }

    private void displayNoMessages() {
        TextView noMessagesView = findViewById(R.id.no_messages_view);
        noMessagesView.setVisibility(View.VISIBLE);
    }

    private void openContacts() {
        if (!checkPermissions()) {
            Toast.makeText(this, "Permissions are required to access contacts.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    public void addMessage(String sender, String content) {
        messages.add(new Message(sender, content));
        messageAdapter.notifyItemInserted(messages.size() - 1);
        recyclerView.smoothScrollToPosition(messages.size() - 1);

        // Check if the message contains a URL
        if (containsUrl(content)) {
            String url = extractUrl(content);
            showUrlWarningDialog(url);
        }
    }

    private boolean containsUrl(String message) {
        Pattern urlPattern = Pattern.compile("(http|https)://[\\w\\-\\._~:/?#[\\]@!$&'()*+,;=%]+");
        Matcher matcher = urlPattern.matcher(message);
        return matcher.find();
    }

    private String extractUrl(String message) {
        Pattern urlPattern = Pattern.compile("(http|https)://[\\w\\-\\._~:/?#[\\]@!$&'()*+,;=%]+");
        Matcher matcher = urlPattern.matcher(message);
        return matcher.find() ? matcher.group() : null;
    }

    private void showUrlWarningDialog(String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("URL Detected")
                .setMessage("This message contains a URL:\n\n" + url + "\n\nIt might be harmful. Do you want to proceed?")
                .setPositiveButton("Proceed", (dialog, id) -> openUrlInBrowser(url))
                .setNegativeButton("Exit", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private void openUrlInBrowser(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                loadMessages();
            } else {
                Toast.makeText(this, "Permissions are required to use this app.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
