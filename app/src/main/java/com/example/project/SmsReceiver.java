package com.example.project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                String format = bundle.getString("format"); // Get SMS format

                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu, format);
                        String msgBody = message.getMessageBody();
                        String sender = message.getDisplayOriginatingAddress();

                        if (containsUrl(msgBody)) {
                            Intent msgIntent = new Intent(context, MainActivity.class);
                            msgIntent.putExtra("message_sender", sender);
                            msgIntent.putExtra("message_body", msgBody);
                            msgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Required for BroadcastReceiver
                            context.startActivity(msgIntent);

                            String url = extractUrl(msgBody);
                            if (url != null) {
                                showUrlDialog(context, url);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean containsUrl(String message) {
        return Patterns.WEB_URL.matcher(message).find();
    }

    private String extractUrl(String message) {
        String urlRegex = "(http|https)://[\\w\\-\\._~:/?#[\\]@!$&'()*+,;=%]+";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(urlRegex);
        java.util.regex.Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group() : null;
    }

    private void showUrlDialog(Context context, String url) {
        new AlertDialog.Builder(context)
                .setTitle("URL Detected")
                .setMessage("The message contains a URL: " + url)
                .setPositiveButton("Proceed", (dialog, id) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(browserIntent);
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}
