package com.example.evchargingstationlocator;

import java.util.Calendar;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    FirebaseAuth fAuth;
    String userID;
    Button selectImage, post, back;
    ImageView selected_image;
    TextView location;
    EditText message;
    ProgressDialog progressDialog;
    EditText Image_name;
    Uri imageUri;
    String url, Location;
    public static final int PICK_IMAGE=100;
    Double Latitude, Longitude;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    StorageReference storageReference;

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    int requestCode = activityResult.getResultCode();
                    int resultCode = activityResult.getResultCode();
                    Intent data = activityResult.getData();

                    if(data != null)
                    {
                        imageUri = data.getData();
                        selected_image.setImageURI(imageUri);
                    }
                    else
                    {
                        Toast.makeText(PostActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
                        selected_image.setImageResource(R.drawable.ic_launcher_background);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        back = findViewById(R.id.backButton);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SavedActivity.class);
                startActivity(intent);
                finish();
            }
        });

        init();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference().child("Images/");

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
//                startActivityForResult(intent, PICK_IMAGE);
                activityResultLauncher.launch(intent);
            }
        });

        // Get the Intent that started this activity
        Intent intent = getIntent();

// Retrieve the String passed from ActivityA using the key
        if (intent != null) {
            Location = intent.getStringExtra("Location");
            //Toast.makeText(this, "Location: " + Location, Toast.LENGTH_SHORT).show();
            if( Location != null ) location.setText(Location);
            Latitude = intent.getDoubleExtra("Latitude", 0.0);
            Longitude = intent.getDoubleExtra("Longitude", 0.0);
            // Now, 'receivedMessage' contains the String passed from ActivityA
            // You can use this string as needed in ActivityB
        }




        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( Image_name == null ){
                    Toast.makeText(PostActivity.this, "Give image a name",Toast.LENGTH_SHORT).show();
                }
                if(imageUri == null)
                {
                    Toast.makeText(PostActivity.this, "Select an image", Toast.LENGTH_SHORT).show();
                }
                else if(message.getText().toString().isEmpty()) {
                    Toast.makeText(PostActivity.this, "Enter a valid description", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    uploadPost();
                    Intent intent = new Intent(PostActivity.this, CheckPostsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        Image_name = findViewById(R.id.pfli_name);

        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        Log.i("USERID", userID);

        DocumentReference document = FirebaseFirestore.getInstance().collection("users").document(userID);
        document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    Image_name.setText(documentSnapshot.getString("imageName"));
                }
                else
                {
                    Toast.makeText(PostActivity.this, "Document not found", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Toast.makeText(PostActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
            }
        });

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data.getData() != null)
//        {
//            imageUri = data.getData();
//            selected_image.setImageURI(imageUri);
//        }
//        else
//        {
//            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
//            selected_image.setImageResource(R.drawable.ic_launcher_background);
//        }
//    }

    private void init()
    {
        selectImage = findViewById(R.id.pfli_image_select);
        post = findViewById(R.id.pfli_post);
        selected_image = findViewById(R.id.pfli_image);
        message = findViewById(R.id.pfli_message);
        progressDialog = new ProgressDialog(this);
        location = findViewById(R.id.pfli_location);
    }

    private void uploadPost()
    {
        progressDialog.setTitle("Uploading Post");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        try {
            // Your upload code here
            // ...
            if (imageUri != null)
            {
                StorageReference sRef = storageReference.child(System.currentTimeMillis()+"."+getExtensionFile(imageUri));
                sRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                url = uri.toString();

                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                String user_id = currentUser.getUid();
                                // Reference to the user's locations node (replace "userId" with the actual user ID)
                                DatabaseReference userLocationsRef = databaseReference.child("Users").child(user_id).child("Locations");

                                userLocationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        // Clear existing markers from the map

                                        for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                            // Retrieve latitude and longitude from the database
                                            Double latitude = locationSnapshot.child("Latitude").getValue(Double.class);
                                            Double longitude = locationSnapshot.child("Longitude").getValue(Double.class);

                                            if(Math.abs(latitude - Latitude) > 0.001){
                                                continue;
                                            }
                                            if(Math.abs(longitude - Longitude) > 0.001){
                                                continue;
                                            }

                                            String newSnapshot = String.valueOf(locationSnapshot);
                                            String sanitizedKey = newSnapshot.replace(".", "_");
                                            DatabaseReference userLocationsRef = databaseReference.child("Users").child(user_id).child("Locations").child(sanitizedKey).child("Posts");

                                            String postid = userLocationsRef.push().getKey();
                                            HashMap<String, Object> map = new HashMap<>();

                                            Calendar calendar = Calendar.getInstance();
                                            int year = calendar.get(Calendar.YEAR);
                                            int month = calendar.get(Calendar.MONTH); // Note: January is 0, February is 1, ...
                                            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                                            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                                            int minute = calendar.get(Calendar.MINUTE);
                                            String Check = String.valueOf(minute);
                                            String currentDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                                            if(Check.length() == 1){
                                                Check = "0" + Check;
                                            }
                                            String currentTime = hourOfDay + ":" + Check ;
                                            map.put("postid", postid);
                                            map.put("postImage", url);
                                            map.put("date",currentDate);
                                            map.put("time",currentTime);
                                            map.put("message", message.getText().toString());
                                            map.put("location", Location);
                                            map.put("latitude", Latitude);
                                            map.put("longitude", Longitude);
                                            map.put("publisher", userID);
                                            map.put("imageName", Image_name.getText().toString());

                                            progressDialog.dismiss();
                                            userLocationsRef.child(postid).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        Toast.makeText(PostActivity.this, "Post Uploaded", Toast.LENGTH_SHORT).show();
                                                        //startActivity(new Intent(PostActivity.this, ImagesActivity.class));
                                                        //finish();
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(PostActivity.this, "Failed"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });


                                            break;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Handle any errors when reading from the database
                                        // You can add error handling code here
                                    }
                                });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PostActivity.this, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions or errors here
            // ...
        } finally {
            // Dismiss the progress dialog in the finally block
            progressDialog.dismiss();
        }


    }

    public String getExtensionFile(Uri uri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap map = MimeTypeMap.getSingleton();
        return map.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}