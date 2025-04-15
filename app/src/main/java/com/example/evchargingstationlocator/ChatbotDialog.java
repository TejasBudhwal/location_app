package com.example.evchargingstationlocator;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatbotDialog extends Dialog {

    private ListView chatListView;
    private EditText userInputEditText;
    private ImageButton sendButton;
    private ImageButton closeButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private OnChatbotCommandListener commandListener;

    // Interface for handling command callbacks
    public interface OnChatbotCommandListener {
        void findEVChargers(String location, String vehicleType, int radius);
        void getDirections(String source, String destination);
        // Add new methods for activities 1 and 2
        void saveLocation(String location, String label);
        void viewSavedLocations();
        void viewLocationPosts(String location);
        void createPost(String location, String content);
    }

    public ChatbotDialog(@NonNull Context context, OnChatbotCommandListener listener) {
        super(context);
        this.commandListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove title bar and set custom layout
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_chatbot);

        // Set dialog size (80% of width, 60% of height)
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = (int)(getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
        params.height = (int)(getContext().getResources().getDisplayMetrics().heightPixels * 0.6);
        getWindow().setAttributes(params);

        initViews();
        setupChat();

        // Initial welcome message with updated options
        addBotMessage("Hi! I can help you with:\n" +
                "- Saving a location\n" +
                "- Viewing saved locations\n" +
                "- Finding EV charging stations\n" +
                "- Getting directions between locations\n\n" +
                "What would you like assistance with?");
    }

    private void initViews() {
        chatListView = findViewById(R.id.chatListView);
        userInputEditText = findViewById(R.id.userInputEditText);
        sendButton = findViewById(R.id.sendButton);
        closeButton = findViewById(R.id.closeButton);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(), chatMessages);
        chatListView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = userInputEditText.getText().toString().trim();
                if (!userMessage.isEmpty()) {
                    processUserMessage(userMessage);
                    userInputEditText.setText("");
                }
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void setupChat() {
        // State variables for multi-turn conversations
        chatState = ChatState.INITIAL;
        currentConversation = Conversation.NONE;

        // Fields to collect for EV charger search
        evLocation = "";
        evVehicleType = "";
        evRadius = 0;

        // Fields for navigation
        navSource = "";
        navDestination = "";

        // Fields for saving location
        locationToSave = "";
        locationLabel = "";

        // Fields for viewing posts
        locationForPosts = "";
        postContent = "";
    }

    // Enum to track conversation states
    private enum ChatState {
        INITIAL,
        COLLECTING_INFO,
        CONFIRMING
    }

    // Enum to track which conversation flow we're in
    private enum Conversation {
        NONE,
        EV_CHARGER,
        NAVIGATION,
        SAVE_LOCATION,
        VIEW_LOCATIONS,
        VIEW_POSTS
    }

    // State tracking variables
    private ChatState chatState;
    private Conversation currentConversation;

    // EV Charger variables
    private String evLocation;
    private String evVehicleType;
    private int evRadius;

    // Navigation variables
    private String navSource;
    private String navDestination;

    // Save location variables
    private String locationToSave;
    private String locationLabel;

    // View posts variables
    private String locationForPosts;
    private String postContent;

    private void processUserMessage(String message) {
        // Add user message to chat
        addUserMessage(message);

        String lowercaseMsg = message.toLowerCase();

        // Handle based on current state and conversation flow
        if (chatState == ChatState.INITIAL) {
            // Determine which conversation to start
            if (lowercaseMsg.contains("save") &&
                    (lowercaseMsg.contains("location") || lowercaseMsg.contains("place"))) {

                currentConversation = Conversation.SAVE_LOCATION;
                chatState = ChatState.COLLECTING_INFO;
                locationToSave = "";
                locationLabel = "";

                addBotMessage("I'll help you save a location. What location would you like to save? (Type an address or place name)");

            } else if ((lowercaseMsg.contains("view") || lowercaseMsg.contains("show") || lowercaseMsg.contains("see")) &&
                    lowercaseMsg.contains("saved") &&
                    (lowercaseMsg.contains("location") || lowercaseMsg.contains("place"))) {

                currentConversation = Conversation.VIEW_LOCATIONS;

                addBotMessage("I'll show you your saved locations on the map.");

                // Call the listener to show saved locations
                if (commandListener != null) {
                    commandListener.viewSavedLocations();
                }

                // Reset state
                chatState = ChatState.INITIAL;
                currentConversation = Conversation.NONE;

            } else if ((lowercaseMsg.contains("post") || lowercaseMsg.contains("feed")) &&
                    (lowercaseMsg.contains("location") || lowercaseMsg.contains("place"))) {

                currentConversation = Conversation.VIEW_POSTS;
                chatState = ChatState.COLLECTING_INFO;
                locationForPosts = "";

                addBotMessage("I'll help you view posts for a location. Which saved location's posts would you like to see?");

            } else if (lowercaseMsg.contains("ev") ||
                    lowercaseMsg.contains("charger") ||
                    lowercaseMsg.contains("charging") ||
                    lowercaseMsg.contains("station")) {

                currentConversation = Conversation.EV_CHARGER;
                chatState = ChatState.COLLECTING_INFO;
                evLocation = "";
                evVehicleType = "";
                evRadius = 0;

                addBotMessage("I'll help you find EV charging stations. Let's collect some information:\n\n" +
                        "What location would you like to search near? (You can name a place or address)");

            } else if (lowercaseMsg.contains("direction") ||
                    lowercaseMsg.contains("navigate") ||
                    lowercaseMsg.contains("route") ||
                    lowercaseMsg.contains("path") ||
                    lowercaseMsg.contains("way")) {

                currentConversation = Conversation.NAVIGATION;
                chatState = ChatState.COLLECTING_INFO;
                navSource = "";
                navDestination = "";

                addBotMessage("I'll help you get directions. Let's start:\n\n" +
                        "What's your starting location? (You can name a place or address)");

            } else {
                // Generic response for unclear requests
                addBotMessage("I can help you save locations, view saved locations, find EV charging stations, or get directions. Which would you like assistance with?");
            }
        } else if (chatState == ChatState.COLLECTING_INFO) {
            if (currentConversation == Conversation.EV_CHARGER) {
                handleEVChargerConversation(lowercaseMsg);
            } else if (currentConversation == Conversation.NAVIGATION) {
                handleNavigationConversation(lowercaseMsg);
            } else if (currentConversation == Conversation.SAVE_LOCATION) {
                handleSaveLocationConversation(lowercaseMsg);
            } else if (currentConversation == Conversation.VIEW_POSTS) {
                handleViewPostsConversation(lowercaseMsg);
            }
        } else if (chatState == ChatState.CONFIRMING) {
            if (lowercaseMsg.contains("yes") || lowercaseMsg.contains("confirm") || lowercaseMsg.contains("correct")) {
                if (currentConversation == Conversation.EV_CHARGER) {
                    addBotMessage("Great! Finding EV charging stations near " + evLocation +
                            " for a " + evVehicleType + " within " + evRadius + " km.");

                    // Call the listener to execute the search
                    if (commandListener != null) {
                        commandListener.findEVChargers(evLocation, evVehicleType, evRadius);
                    }

                    // Reset state
                    chatState = ChatState.INITIAL;
                    currentConversation = Conversation.NONE;

                } else if (currentConversation == Conversation.NAVIGATION) {
                    addBotMessage("Perfect! Getting directions from " + navSource + " to " + navDestination + ".");

                    // Call the listener to execute navigation
                    if (commandListener != null) {
                        commandListener.getDirections(navSource, navDestination);
                    }

                    // Reset state
                    chatState = ChatState.INITIAL;
                    currentConversation = Conversation.NONE;
                } else if (currentConversation == Conversation.SAVE_LOCATION) {
                    addBotMessage("Great! Saving location: " + locationToSave + " with label: " + locationLabel);

                    // Call the listener to save the location
                    if (commandListener != null) {
                        commandListener.saveLocation(locationToSave, locationLabel);
                    }

                    // Reset state
                    chatState = ChatState.INITIAL;
                    currentConversation = Conversation.NONE;
                } else if (currentConversation == Conversation.VIEW_POSTS) {
                    addBotMessage("Opening post feed for: " + locationForPosts);

                    // Call the listener to view the location posts
                    if (commandListener != null) {
                        commandListener.viewLocationPosts(locationForPosts);
                    }

                    // Reset state
                    chatState = ChatState.INITIAL;
                    currentConversation = Conversation.NONE;
                }
            } else if (lowercaseMsg.contains("no") || lowercaseMsg.contains("wrong") || lowercaseMsg.contains("incorrect")) {
                // Go back to collecting information
                chatState = ChatState.COLLECTING_INFO;

                if (currentConversation == Conversation.EV_CHARGER) {
                    addBotMessage("Let's try again. What location would you like to search near?");
                    evLocation = "";
                    evVehicleType = "";
                    evRadius = 0;
                } else if (currentConversation == Conversation.NAVIGATION) {
                    addBotMessage("Let's try again. What's your starting location?");
                    navSource = "";
                    navDestination = "";
                } else if (currentConversation == Conversation.SAVE_LOCATION) {
                    addBotMessage("Let's try again. What location would you like to save?");
                    locationToSave = "";
                    locationLabel = "";
                } else if (currentConversation == Conversation.VIEW_POSTS) {
                    addBotMessage("Let's try again. Which saved location's posts would you like to see?");
                    locationForPosts = "";
                }
            } else {
                addBotMessage("I didn't understand. Please confirm with yes or no.");
            }
        }
    }

    private void handleEVChargerConversation(String lowercaseMsg) {
        if (evLocation.isEmpty()) {
            // Collect location
            evLocation = lowercaseMsg;
            addBotMessage("What type of vehicle do you have? Options are: car, truck, small truck, foot, scooter, or bike.");
        } else if (evVehicleType.isEmpty()) {
            // Validate and collect vehicle type
            if (lowercaseMsg.contains("car") ||
                    lowercaseMsg.contains("truck") ||
                    lowercaseMsg.contains("foot") ||
                    lowercaseMsg.contains("scooter") ||
                    lowercaseMsg.contains("bike")) {

                // Extract the vehicle type
                if (lowercaseMsg.contains("small truck")) {
                    evVehicleType = "small truck";
                } else if (lowercaseMsg.contains("truck")) {
                    evVehicleType = "truck";
                } else if (lowercaseMsg.contains("car")) {
                    evVehicleType = "car";
                } else if (lowercaseMsg.contains("foot")) {
                    evVehicleType = "foot";
                } else if (lowercaseMsg.contains("scooter")) {
                    evVehicleType = "scooter";
                } else if (lowercaseMsg.contains("bike")) {
                    evVehicleType = "bike";
                }

                addBotMessage("What search radius would you like to use? (in kilometers)");
            } else {
                addBotMessage("I didn't recognize that vehicle type. Please choose from: car, truck, small truck, foot, scooter, or bike.");
            }
        } else if (evRadius == 0) {
            // Try to parse the radius
            try {
                // Extract numbers from the message
                String numberStr = lowercaseMsg.replaceAll("[^0-9]", "");
                if (!numberStr.isEmpty()) {
                    evRadius = Integer.parseInt(numberStr);

                    // Move to confirmation
                    chatState = ChatState.CONFIRMING;

                    addBotMessage("I'll search for EV chargers with these details:\n" +
                            "- Near: " + evLocation + "\n" +
                            "- Vehicle type: " + evVehicleType + "\n" +
                            "- Radius: " + evRadius + " km\n\n" +
                            "Is this correct? (yes/no)");
                } else {
                    addBotMessage("I need a number for the radius. How many kilometers around " +
                            evLocation + " should I search?");
                }
            } catch (NumberFormatException e) {
                addBotMessage("I need a number for the radius. Please enter a distance in kilometers.");
            }
        }
    }

    private void handleNavigationConversation(String lowercaseMsg) {
        if (navSource.isEmpty()) {
            // Collect source location
            navSource = lowercaseMsg;
            addBotMessage("What's your destination?");
        } else if (navDestination.isEmpty()) {
            // Collect destination
            navDestination = lowercaseMsg;

            // Move to confirmation
            chatState = ChatState.CONFIRMING;

            addBotMessage("I'll get directions with these details:\n" +
                    "- From: " + navSource + "\n" +
                    "- To: " + navDestination + "\n\n" +
                    "Is this correct? (yes/no)");
        }
    }

    private void handleSaveLocationConversation(String lowercaseMsg) {
        if (locationToSave.isEmpty()) {
            // Collect location
            locationToSave = lowercaseMsg;
            addBotMessage("What label would you like to give this location? (e.g. Home, Work, Favorite Restaurant)");
        } else if (locationLabel.isEmpty()) {
            // Collect label
            locationLabel = lowercaseMsg;

            // Move to confirmation
            chatState = ChatState.CONFIRMING;

            addBotMessage("I'll save this location with these details:\n" +
                    "- Location: " + locationToSave + "\n" +
                    "- Label: " + locationLabel + "\n\n" +
                    "Is this correct? (yes/no)");
        }
    }

    private void handleViewPostsConversation(String lowercaseMsg) {
        if (locationForPosts.isEmpty()) {
            // Collect location
            locationForPosts = lowercaseMsg;

            // Move to confirmation
            chatState = ChatState.CONFIRMING;

            addBotMessage("I'll show you posts for this location:\n" +
                    "- Location: " + locationForPosts + "\n\n" +
                    "Is this correct? (yes/no)");
        }
    }

    private void addUserMessage(String message) {
        chatMessages.add(new ChatMessage(message, true));
        updateChatList();
    }

    private void addBotMessage(String message) {
        chatMessages.add(new ChatMessage(message, false));
        updateChatList();
    }

    private void updateChatList() {
        chatAdapter.notifyDataSetChanged();
        chatListView.smoothScrollToPosition(chatMessages.size() - 1);
    }
}