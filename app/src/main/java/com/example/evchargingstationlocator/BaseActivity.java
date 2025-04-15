package com.example.evchargingstationlocator;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public abstract class BaseActivity extends AppCompatActivity implements ChatbotDialog.OnChatbotCommandListener {

    protected FloatingActionButton chatFab;
    protected ChatbotDialog chatbotDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setupChatFab();
    }

    protected void setupChatFab() {
        // Get the root view
        View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

        // Add FAB programmatically to avoid modifying all layout files
        if (rootView instanceof ViewGroup) {
            // Create FAB
            chatFab = new FloatingActionButton(this);
            chatFab.setId(View.generateViewId());
            chatFab.setImageResource(R.drawable.ic_chat);
            chatFab.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));

            // Create layout params
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            int margin = (int) getResources().getDimension(R.dimen.fab_margin);
            params.setMargins(margin, margin, margin, margin);

            // Add FAB to the root view if it's a RelativeLayout
            if (rootView instanceof RelativeLayout) {
                ((RelativeLayout) rootView).addView(chatFab, params);
            }
            // If not a RelativeLayout, we wrap the content in one
            else {
                // Remove the rootView from its parent
                ((ViewGroup) rootView.getParent()).removeView(rootView);

                // Create a new RelativeLayout
                RelativeLayout newRoot = new RelativeLayout(this);
                newRoot.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                // Add the original rootView to the new RelativeLayout
                RelativeLayout.LayoutParams rootParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                newRoot.addView(rootView, rootParams);

                // Add the FAB to the new RelativeLayout
                newRoot.addView(chatFab, params);

                // Set the new RelativeLayout as the content view
                ((ViewGroup) findViewById(android.R.id.content)).addView(newRoot);
            }

            // Set click listener for FAB
            chatFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChatbot();
                }
            });
        }
    }

    private void showChatbot() {
        if (chatbotDialog == null) {
            chatbotDialog = new ChatbotDialog(this, this);
        }
        chatbotDialog.show();
    }

    // Implement the required methods from the OnChatbotCommandListener interface
    @Override
    public void findEVChargers(String location, String vehicleType, int radius) {
        // To be implemented in EVChargerActivity
        // Default implementation does nothing
    }

    @Override
    public void getDirections(String source, String destination) {
        // To be implemented in NavigationActivity
        // Default implementation does nothing
    }

    @Override
    public void saveLocation(String location, String label) {
        // To be implemented in LocationSavingActivity
        // Default implementation does nothing
    }

    @Override
    public void viewSavedLocations() {
        // To be implemented in SavedLocationsActivity
        // Default implementation does nothing
    }

    @Override
    public void viewLocationPosts(String location) {
        // To be implemented in SavedLocationsActivity
        // Default implementation does nothing
    }

    @Override
    public void createPost(String location, String content) {
        // To be implemented in SavedLocationsActivity
        // Default implementation does nothing
    }
}