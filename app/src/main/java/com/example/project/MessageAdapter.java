package com.example.project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;

    public MessageAdapter(List<Message> messageList, MainActivity mainActivity) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.senderTextView.setText(message.getSender());
        holder.contentTextView.setText(message.getContent());
        holder.contentTextView.setOnClickListener(v -> {
            if (containsUrl(message.getContent())) {
                String url = extractUrl(message.getContent());
                if (url != null) {
                    showUrlDialog(v.getContext(), url);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private boolean containsUrl(String message) {
        return message.contains("http://") || message.contains("https://");
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
                .setMessage("This message contains a URL: " + url)
                .setPositiveButton("Proceed", (dialog, id) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(browserIntent);
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                .setCancelable(false);
        builder.create().show();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView;
        TextView contentTextView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.senderTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
        }
    }
}
