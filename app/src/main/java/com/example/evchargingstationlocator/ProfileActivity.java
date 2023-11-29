package com.example.evchargingstationlocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference reference;

    private TextView emailTextView;
    private TextView fullNameTextView;
    private TextView phoneNumberTextView;
    private TextView userNameTextView;

    private Button back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // Initialize Firebase Realtime Database reference
        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        // Initialize TextViews
        emailTextView = findViewById(R.id.emailTextView);
        fullNameTextView = findViewById(R.id.fullNameTextView);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        userNameTextView = findViewById(R.id.userNameTextView);

        back = findViewById(R.id.backButton);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        if (user != null) {
            String userEmail = user.getEmail();

            Query query = reference.orderByChild("email").equalTo(userEmail);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String userID = snapshot.getKey();

                            // Now you have obtained the userID
                            //Toast.makeText(ProfileActivity.this, "User ID: " + userID, Toast.LENGTH_SHORT).show();

                            // Retrieve other user information using this userID if needed
                            // For example:
                            String fullName = snapshot.child("fullName").getValue(String.class);
                            String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                            String userName = snapshot.child("userName").getValue(String.class);
                            String email = snapshot.child("email").getValue(String.class);

                            emailTextView.setText(email);
                            fullNameTextView.setText(fullName);
                            phoneNumberTextView.setText(phoneNumber);
                            userNameTextView.setText(userName);
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                    Toast.makeText(ProfileActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // If the user is not logged in
            Toast.makeText(ProfileActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}