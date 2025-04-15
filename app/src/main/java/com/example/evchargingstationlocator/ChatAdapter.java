package com.example.evchargingstationlocator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ChatAdapter extends BaseAdapter {
    private Context context;
    private List<ChatMessage> chatMessages;
    private LayoutInflater inflater;

    public ChatAdapter(Context context, List<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return chatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ChatMessage message = chatMessages.get(position);

        if (message.isUser()) {
            view = inflater.inflate(R.layout.item_user_message, null);
        } else {
            view = inflater.inflate(R.layout.item_bot_message, null);
        }

        TextView messageText = view.findViewById(R.id.messageText);
        messageText.setText(message.getMessageText());

        return view;
    }
}