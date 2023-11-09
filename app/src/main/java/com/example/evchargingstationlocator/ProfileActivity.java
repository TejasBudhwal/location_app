package com.example.evchargingstationlocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference reference;

    private TextView emailTextView;
    private TextView fullNameTextView;
    private TextView phoneNumberTextView;
    private TextView userNameTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // Initialize Firebase Realtime Database reference
        reference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize TextViews
        emailTextView = findViewById(R.id.emailTextView);
        fullNameTextView = findViewById(R.id.fullNameTextView);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        userNameTextView = findViewById(R.id.userNameTextView);

        if (user != null) {
            String userID = user.getUid();

            // Retrieve user data from Firebase Realtime Database
            reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Users userProfile = dataSnapshot.getValue(Users.class);

                        if (userProfile != null) {
                            String fullName = userProfile.getFullName();
                            String phoneNumber = userProfile.getPhoneNumber();
                            String userName = userProfile.getUserName();

                            // Display user details in TextViews
                            emailTextView.setText(user.getEmail());
                            fullNameTextView.setText("Full Name: " + fullName);
                            phoneNumberTextView.setText("Phone Number: " + phoneNumber);
                            userNameTextView.setText("Username: " + userName);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database read error if needed
                    Toast.makeText(ProfileActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}