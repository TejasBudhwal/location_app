package com.example.evchargingstationlocator;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class CheckPostsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseReference myRef;

    private ArrayList<Posts> postsList;
    private PostAdapter postAdapter;
    private Context mContext;
    private Button add_post,back;
    String Location;
    Double Latitude, Longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_posts);
        add_post = findViewById(R.id.add_post);
        back = findViewById(R.id.back);
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        myRef = FirebaseDatabase.getInstance().getReference();

        postsList = new ArrayList<>();

        ClearAll();

        GetDataFromFirebase();

        Intent intent = getIntent();

// Retrieve the String passed from ActivityA using the key
        if (intent != null) {
            Location = intent.getStringExtra("Location");
            Latitude = intent.getDoubleExtra("Latitude", 0.0);
            Longitude = intent.getDoubleExtra("Longitude", 0.0);
            // Now, 'receivedMessage' contains the String passed from ActivityA
            // You can use this string as needed in ActivityB
        }

        add_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckPostsActivity.this, PostActivity.class);
                intent.putExtra("Location", Location);
                intent.putExtra("Latitude",Latitude);
                intent.putExtra("Longitude",Longitude);
                startActivity(intent);
                finish();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckPostsActivity.this, SavedActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void GetDataFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String user_id = currentUser.getUid();
            DatabaseReference userLocationsRef = databaseReference.child("Users").child(user_id).child("Locations");

            userLocationsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ClearAll();

                    for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot postsSnapshot : locationSnapshot.child("Posts").getChildren()) {
                            // Create a Posts object and populate its fields from the database
                            Posts posts = postsSnapshot.getValue(Posts.class);

                            // Add the retrieved post to the postsList
                            if (posts != null) {
                                postsList.add(posts);
                            }
                        }
                    }

                    // Set up the RecyclerView adapter
                    postAdapter = new PostAdapter(getApplicationContext(), postsList);
                    recyclerView.setAdapter(postAdapter);
                    postAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle onCancelled event
                }
            });
        }
    }


    private void ClearAll(){
        if(postsList != null)
        {
            postsList.clear();

            if(postAdapter != null){
                postAdapter.notifyDataSetChanged();
            }
        }

        postsList = new ArrayList<>();
    }
}