package com.example.project;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsMessage;
import android.util.Patterns;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
                    String msgBody = message.getMessageBody();
                    String sender = message.getDisplayOriginatingAddress();

                    // Check if the message contains a URL
                    if (containsUrl(msgBody)) {
                        // Pass the message to MainActivity (you could send a broadcast or use other methods)
                        Intent msgIntent = new Intent(context, MainActivity.class);
                        msgIntent.putExtra("message_sender", sender);
                        msgIntent.putExtra("message_body", msgBody);
                        context.startActivity(msgIntent);

                        // Show dialog if URL is present
                        String url = extractUrl(msgBody);
                        if (url != null) {
                            showUrlDialog(context, url);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("URL Detected")
                .setMessage("The message contains a URL: " + url)
                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Ensure it opens from a non-UI context
                        context.startActivity(browserIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false); // Prevent accidental dismiss
        builder.create().show();
    }
}
