package com.example.evchargingstationlocator;

public class ChatMessage {
    private String messageText;
    private boolean isUser;

    public ChatMessage(String messageText, boolean isUser) {
        this.messageText = messageText;
        this.isUser = isUser;
    }

    public String getMessageText() {
        return messageText;
    }

    public boolean isUser() {
        return isUser;
    }
}
