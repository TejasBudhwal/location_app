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

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String user_id = currentUser.getUid();
// Reference to the user's locations node (replace "userId" with the actual user ID)
        DatabaseReference userLocationsRef = databaseReference.child("Users").child(user_id).child("Locations");
        Query query = userLocationsRef;

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ClearAll();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Double latitude = snapshot.child("Latitude").getValue(Double.class);
                    Double longitude = snapshot.child("Longitude").getValue(Double.class);

                    // Check if latitude and longitude are not null
                    if (latitude != null && longitude != null) {
                        if (Math.abs(latitude - Latitude) > 0.001 || Math.abs(longitude - Longitude) > 0.001) {
                            continue; // Skip further processing if the difference is more than 0.001
                        }
                    } else {
                        // Handle the case where latitude or longitude is null (optional)
                        // For example, log a message or take necessary action
                        Log.e("Null Values", "Latitude or Longitude is null for this snapshot");
                        continue;
                    }

                    Posts posts = new Posts();

                    String newSnapshot = String.valueOf(snapshot);
                    String sanitizedKey = newSnapshot.replace(".", "_");

                    DataSnapshot postsSnapshot = snapshot.child(sanitizedKey).child("Posts");
                    if (postsSnapshot.exists()) {
                        posts.setPostImage(postsSnapshot.child("postImage").getValue(String.class));
                        posts.setImageName(postsSnapshot.child("imageName").getValue(String.class));
                        posts.setLocation(postsSnapshot.child("location").getValue(String.class));
                        posts.setMessage(postsSnapshot.child("message").getValue(String.class));
                        posts.setDate(postsSnapshot.child("date").getValue(String.class));
                        posts.setTime(postsSnapshot.child("time").getValue(String.class));
                        postsList.add(posts);
                    }
                    break;
                }

                postAdapter = new PostAdapter(getApplicationContext(), postsList);
                recyclerView.setAdapter(postAdapter);
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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