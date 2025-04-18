package com.example.evchargingstationlocator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import com.example.evchargingstationlocator.PathFinder;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, NavigationView.OnNavigationItemSelectedListener, UniversalChatbotDialog.OnChatbotCommandListener{

    private ImageButton chatbotButton;

    ImageView imageViewSearch;
    EditText inputlocation;
    Button mapClicked;
    private Marker marker;
    protected FloatingActionButton chatFab;
    protected ChatbotDialog chatbotDialog;

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    FirebaseDatabase firebaseDatabase;

    // creating a variable for our Database
    // Reference for Firebase.
    DatabaseReference databaseReference;


    private GoogleMap myMap;
    LatLng delhi = new LatLng(28.644800, 77.216721);
    private Button hybridMapBtn, terrainMapBtn, satelliteMapBtn;

    private MenuItem item;

    //    MapView mapView;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize chatbot button
        chatbotButton = findViewById(R.id.chatbotButton);
        chatbotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChatbot();
            }
        });

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String user_id = currentUser.getUid();
        //Toast.makeText(this, ""+user_id, Toast.LENGTH_SHORT).show();
        databaseReference = firebaseDatabase.getReference("Users").child(user_id).child("Locations");



//        Toolbar toolbar = (Toolbar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        Intent intent = getIntent();

// Retrieve the String passed from ActivityA using the key
        if (intent != null) {
            String dd = intent.getStringExtra("Location");
            double sx = intent.getDoubleExtra("Latitude", 0.0);
            double sy = intent.getDoubleExtra("Longitude", 0.0);
            saveLocationToFirebase(sx,sy);
            // Now, 'receivedMessage' contains the String passed from ActivityA
            // You can use this string as needed in ActivityB
        }


        Button saveLocationButton = findViewById(R.id.button); // Replace with the ID of your button
        saveLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current marker's position
                if (marker != null) {
                    LatLng location = marker.getPosition();
                    double latitude = location.latitude;
                    double longitude = location.longitude;

                    // Save the location to Firebase
                    saveLocationToFirebase(latitude, longitude);
                    Toast.makeText(MapsActivity.this, "Location saved to Firebase", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity.this, "No location to save", Toast.LENGTH_SHORT).show();
                }
            }
        });



        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("View Map");

        toolbar.inflateMenu(R.menu.menu);
//        mapView.findViewById(R.id.mapView);
        checkPermission();

        if (isPermissionGranter) {
            if (checkGooglePlayServices()) {
//                mapView.getMapAsync(this);
//                mapView.onCreate(savedInstanceState);
                Toast.makeText(this, "Google Play service Available", Toast.LENGTH_SHORT).show();
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            } else {
                Toast.makeText(this, "Google Play service Not Available", Toast.LENGTH_SHORT).show();
            }
        }

        Menu menu= navigationView.getMenu();

//        menu.findItem(R.id.logout_main).setVisible(false);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
//        hybridMapBtn = findViewById(R.id.idBtnHybridMap);
//        terrainMapBtn = findViewById(R.id.idBtnTerrainMap);
//        satelliteMapBtn = findViewById(R.id.idBtnSatelliteMap);
        imageViewSearch= (ImageView) findViewById(R.id.imageViewSearch);
        inputlocation= (EditText) findViewById(R.id.inputLocation);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location =inputlocation.getText().toString();
                if(location==null){
                    Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
                }else{
                    Geocoder geocoder=new Geocoder(MapsActivity.this, Locale.getDefault());
                    try {
                        List<Address> listAddress=geocoder.getFromLocationName(location,1);
                        if(listAddress.size()>0){
                            myMap.clear();
                            LatLng latLng=new LatLng(listAddress.get(0).getLatitude(),listAddress.get(0).getLongitude());
                            // Add a marker on the map coordinates.
                            marker = myMap.addMarker(new MarkerOptions().position(latLng).title(""+listAddress.get(0).getCountryName()));

//                            myMap.setOnMarkerClickListener(this);
                            // Move the camera to the map coordinates and zoom in closer.
                            myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                            //Toast.makeText(MapsActivity.this, listAddress.get(0).getCountryName(), Toast.LENGTH_SHORT).show();
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                        }
                    } catch (IOException e) {
                        Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
//                        throw new RuntimeException(e);
                    }
                }
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.SimpleMap);
//        hybridMapBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//            }
//        });
//
//        terrainMapBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // below line is to change
//                // the type of terrain map.
//                myMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
//            }
//        });
//        satelliteMapBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // below line is to change the
//                // type of satellite map.
//                myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//            }
//        });
    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApiAvailability.isUserResolvableError((result))) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, result, 201, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(MapsActivity.this, "User Canceled Dialoge", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
            ;
        }
        return false;
    }

    boolean isPermissionGranter;

    private void checkPermission() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                isPermissionGranter = true;
                Toast.makeText(MapsActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        myMap = googleMap;


        LatLng latLng = new LatLng(35.00116, 135.7681);
        Geocoder geocoder=new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            List<Address> listAddress=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(listAddress.size()>0){
                myMap.clear();
                // Add a marker on the map coordinates.
                marker = myMap.addMarker(new MarkerOptions().position(latLng).title(""+listAddress.get(0).getCountryName()));

                myMap.setOnMarkerClickListener(this);
                // Move the camera to the map coordinates and zoom in closer.
                myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                //Toast.makeText(MapsActivity.this, listAddress.get(0).getCountryName(), Toast.LENGTH_SHORT).show();
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
        } catch (IOException e) {
            Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
//                        throw new RuntimeException(e);
        }

        myMap.setOnMarkerClickListener(this);
        myMap.setOnMapClickListener(this);
        // Add a marker on the map coordinates.
//        googleMap.addMarker(new MarkerOptions()
//                .position(kyoto)
//                .title("Kyoto"));
        // Move the camera to the map coordinates and zoom in closer.
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        // Display traffic.
        googleMap.setTrafficEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);

    }
//    @Override
//    protected void onStart(){
//        super.onStart();
//        mapView.onStart();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mapView.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mapView.onPause();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        mapView.onStop();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mapView.onDestroy();
//    }
//
//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
//        super.onSaveInstanceState(outState, outPersistentState);
//        mapView.onSaveInstanceState(outState);
//    }
//
//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//        mapView.onLowMemory();
//    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu,menu);
//
//        return true;
//    }
//    @Override


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    //    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        inflater.inflate(R.menu.menu, menu);
//    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.noneMap){
            myMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
        if(item.getItemId()==R.id.MapHybrid){
            myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
        if(item.getItemId()==R.id.MapTerrain){
            myMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }
        if(item.getItemId()==R.id.NormalMap){
            myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        if(item.getItemId()==R.id.SatelliteMap){
            myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        //Toast.makeText(this, "My Position"+marker.getPosition(), Toast.LENGTH_SHORT).show();

        return false;
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (listAddress.size() > 0) {
                myMap.clear();
                // Add a marker on the map coordinates.
                marker = myMap.addMarker(new MarkerOptions().position(latLng).title("" + listAddress.get(0).getCountryName()));

                myMap.setOnMarkerClickListener(this);
                // Move the camera to the map coordinates and zoom in closer.
                myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                //Toast.makeText(MapsActivity.this, listAddress.get(0).getCountryName(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.SimpleMap){

        }
        else if(item.getItemId()==R.id.Saved_Locations){
            Intent intent = new Intent(MapsActivity.this,SavedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId()==R.id.MarkedLocation){
            Intent intent = new Intent(MapsActivity.this,EVChargerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId()==R.id.PathFinder){
            Intent intent = new Intent(MapsActivity.this,PathFinder.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId()==R.id.share){
            Toast.makeText(this, "Please Share", Toast.LENGTH_SHORT).show();
        }
        else if(item.getItemId()==R.id.rate_us){
            Toast.makeText(this, "Please Rate US", Toast.LENGTH_SHORT).show();
        }
        else if(item.getItemId()==R.id.profile_main){
            Intent intent = new Intent(MapsActivity.this,ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId()==R.id.logout_main){
            FirebaseAuth.getInstance().signOut(); // Sign out the user from Firebase

            Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Logout Successful", Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return super.onOptionsItemSelected(item);

    }

    private void saveLocationToFirebase(double latitude, double longitude) {
        // Create a new location entry
        DatabaseReference newLocationRef = databaseReference.push();
        newLocationRef.child("Latitude").setValue(latitude);
        newLocationRef.child("Longitude").setValue(longitude);
    }

//    @Override
//    public void setContentView(int layoutResID) {
//        super.setContentView(layoutResID);
//        setupChatFab();
//    }
//
//    protected void setupChatFab() {
//        // Get the root view
//        View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
//
//        // Add FAB programmatically to avoid modifying all layout files
//        if (rootView instanceof ViewGroup) {
//            // Create FAB
//            chatFab = new FloatingActionButton(this);
//            chatFab.setId(View.generateViewId());
//            chatFab.setImageResource(R.drawable.ic_chat);
//            chatFab.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
//
//            // Create layout params
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.WRAP_CONTENT,
//                    RelativeLayout.LayoutParams.WRAP_CONTENT);
//            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//            params.addRule(RelativeLayout.ALIGN_PARENT_END);
//            int margin = (int) getResources().getDimension(R.dimen.fab_margin);
//            params.setMargins(margin, margin, margin, margin);
//
//            // Add FAB to the root view if it's a RelativeLayout
//            if (rootView instanceof RelativeLayout) {
//                ((RelativeLayout) rootView).addView(chatFab, params);
//            }
//            // If not a RelativeLayout, we wrap the content in one
//            else {
//                // Remove the rootView from its parent
//                ((ViewGroup) rootView.getParent()).removeView(rootView);
//
//                // Create a new RelativeLayout
//                RelativeLayout newRoot = new RelativeLayout(this);
//                newRoot.setLayoutParams(new ViewGroup.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT));
//
//                // Add the original rootView to the new RelativeLayout
//                RelativeLayout.LayoutParams rootParams = new RelativeLayout.LayoutParams(
//                        RelativeLayout.LayoutParams.MATCH_PARENT,
//                        RelativeLayout.LayoutParams.MATCH_PARENT);
//                newRoot.addView(rootView, rootParams);
//
//                // Add the FAB to the new RelativeLayout
//                newRoot.addView(chatFab, params);
//
//                // Set the new RelativeLayout as the content view
//                ((ViewGroup) findViewById(android.R.id.content)).addView(newRoot);
//            }
//
//            // Set click listener for FAB
//            chatFab.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    showChatbot();
//                }
//            });
//        }
//    }
//
//    private void showChatbot() {
//        if (chatbotDialog == null) {
//            chatbotDialog = new ChatbotDialog(this, this);
//        }
//        chatbotDialog.show();
//    }
//
//    @Override
//    public void findEVChargers(String location, String vehicleType, int radius) {
//        double sx = 0, sy = 0, dx = 0, dy = 0;
//        Geocoder geocoder=new Geocoder(MapsActivity.this, Locale.getDefault());
//        try {
//            List<Address> listAddress=geocoder.getFromLocationName(location,1);
//            if(listAddress.size()>0){
//                sx = listAddress.get(0).getLatitude();
//                sy = listAddress.get(0).getLongitude();
//                LatLng latLng=new LatLng(listAddress.get(0).getLatitude(),listAddress.get(0).getLongitude());
//            }
//        } catch (IOException e) {
//            Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
////                        throw new RuntimeException(e);
//        }
//        Intent intent = new Intent(MapsActivity.this, EVChargerActivity.class);
//
//        Log.e("MapsInfo", "radius="+radius);
//
//        // Pass the coordinates as extras
//        intent.putExtra("START_LAT", sx);
//        intent.putExtra("START_LNG", sy);
//        intent.putExtra("DEST_LAT", vehicleType);
//        intent.putExtra("DEST_LNG", radius);
//
//        startActivity(intent);
//    }
//
//    @Override
//    public void getDirections(String source, String destination) {
//        double sx = 0, sy = 0, dx = 0, dy = 0;
//        Geocoder geocoder=new Geocoder(MapsActivity.this, Locale.getDefault());
//        try {
//            List<Address> listAddress=geocoder.getFromLocationName(source,1);
//            if(listAddress.size()>0){
//                sx = listAddress.get(0).getLatitude();
//                sy = listAddress.get(0).getLongitude();
//                LatLng latLng=new LatLng(listAddress.get(0).getLatitude(),listAddress.get(0).getLongitude());
//            }
//        } catch (IOException e) {
//            Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
////                        throw new RuntimeException(e);
//        }
//        try {
//            List<Address> listAddress=geocoder.getFromLocationName(destination,1);
//            if(listAddress.size()>0){
//                dx = listAddress.get(0).getLatitude();
//                dy = listAddress.get(0).getLongitude();
//                LatLng latLng=new LatLng(listAddress.get(0).getLatitude(),listAddress.get(0).getLongitude());
//            }
//        } catch (IOException e) {
//            Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
////                        throw new RuntimeException(e);
//        }
//        launchPathFinderWithCoordinates(sx, sy, dx, dy);
//    }
//
//    @Override
//    public void saveLocation(String location, String label) {
//        double sx = 0, sy = 0;
//        Geocoder geocoder=new Geocoder(MapsActivity.this, Locale.getDefault());
//        try {
//            List<Address> listAddress=geocoder.getFromLocationName(location,1);
//            if(listAddress.size()>0){
//                sx = listAddress.get(0).getLatitude();
//                sy = listAddress.get(0).getLongitude();
//                LatLng latLng=new LatLng(listAddress.get(0).getLatitude(),listAddress.get(0).getLongitude());
//            }
//        } catch (IOException e) {
//            Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
////                        throw new RuntimeException(e);
//        }
//
//        saveLocationToFirebase(sx, sy);
//        Toast.makeText(MapsActivity.this, "Location saved to Firebase", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void viewSavedLocations() {
//        Intent intent = new Intent(MapsActivity.this, SavedActivity.class);
//        startActivity(intent);
//    }
//
//    @Override
//    public void viewLocationPosts(String location) {
//        double sx = 0, sy = 0;
//        Geocoder geocoder=new Geocoder(MapsActivity.this, Locale.getDefault());
//        try {
//            List<Address> listAddress=geocoder.getFromLocationName(location,1);
//            if(listAddress.size()>0){
//                sx = listAddress.get(0).getLatitude();
//                sy = listAddress.get(0).getLongitude();
//                LatLng latLng=new LatLng(listAddress.get(0).getLatitude(),listAddress.get(0).getLongitude());
//            }
//        } catch (IOException e) {
//            Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
////                        throw new RuntimeException(e);
//        }
//        Intent intent = new Intent(MapsActivity.this, CheckPostsActivity.class);
//        intent.putExtra("Location", "Location");
//        intent.putExtra("Latitude",sx);
//        intent.putExtra("Longitude",sy);
//        startActivity(intent);
//    }
//
//    @Override
//    public void createPost(String location, String content) {
//        double sx = 0, sy = 0;
//        Geocoder geocoder=new Geocoder(MapsActivity.this, Locale.getDefault());
//        try {
//            List<Address> listAddress=geocoder.getFromLocationName(location,1);
//            if(listAddress.size()>0){
//                sx = listAddress.get(0).getLatitude();
//                sy = listAddress.get(0).getLongitude();
//                LatLng latLng=new LatLng(listAddress.get(0).getLatitude(),listAddress.get(0).getLongitude());
//            }
//        } catch (IOException e) {
//            Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
////                        throw new RuntimeException(e);
//        }
//        Intent intent = new Intent(MapsActivity.this, PostActivity.class);
//        intent.putExtra("Location", "Location");
//        intent.putExtra("Latitude",sx);
//        intent.putExtra("Longitude",sy);
//        startActivity(intent);
//    }
//
//    // In MapsActivity.java
//    private void launchPathFinderWithCoordinates(double sx, double sy, double dx, double dy) {
//        Intent intent = new Intent(MapsActivity.this, PathFinder.class);
//
//        // Pass the coordinates as extras
//        intent.putExtra("START_LAT", sx);
//        intent.putExtra("START_LNG", sy);
//        intent.putExtra("DEST_LAT", dx);
//        intent.putExtra("DEST_LNG", dy);
//
//        startActivity(intent);
//    }

    private void showChatbot() {
        UniversalChatbotDialog chatbotDialog = new UniversalChatbotDialog(this, this);
        chatbotDialog.show();
    }

    // UniversalChatbotDialog.OnChatbotCommandListener implementations
    @Override
    public void findEVChargers(String location, String vehicleType, int radius) {
        // Implementation from your original code
        double sx = 0, sy = 0;
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocationName(location, 1);
            if (listAddress.size() > 0) {
                sx = listAddress.get(0).getLatitude();
                sy = listAddress.get(0).getLongitude();
                LatLng latLng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
            }
        } catch (IOException e) {
            Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(MapsActivity.this, EVChargerActivity.class);

        Log.e("MapsInfo", "radius=" + radius);

        // Pass the coordinates as extras
        intent.putExtra("START_LAT", sx);
        intent.putExtra("START_LNG", sy);
        intent.putExtra("DEST_LAT", vehicleType);
        intent.putExtra("DEST_LNG", radius);

        startActivity(intent);
    }

    @Override
    public void getDirections(String source, String destination) {
        // Implementation from your original code
        double sx = 0, sy = 0, dx = 0, dy = 0;
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocationName(source, 1);
            if (listAddress.size() > 0) {
                sx = listAddress.get(0).getLatitude();
                sy = listAddress.get(0).getLongitude();
                LatLng latLng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
            }
        } catch (IOException e) {
            Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
        }
        try {
            List<Address> listAddress = geocoder.getFromLocationName(destination, 1);
            if (listAddress.size() > 0) {
                dx = listAddress.get(0).getLatitude();
                dy = listAddress.get(0).getLongitude();
                LatLng latLng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
            }
        } catch (IOException e) {
            Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
        }
        launchPathFinderWithCoordinates(sx, sy, dx, dy);
    }

    @Override
    public void saveLocation(String location, String label) {
        // Implementation from your original code
        double sx = 0, sy = 0;
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocationName(location, 1);
            if (listAddress.size() > 0) {
                sx = listAddress.get(0).getLatitude();
                sy = listAddress.get(0).getLongitude();
                LatLng latLng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
            }
        } catch (IOException e) {
            Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
        }

        saveLocationToFirebase(sx, sy);
        Toast.makeText(MapsActivity.this, "Location saved to Firebase", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void viewSavedLocations() {
        // Implementation from your original code
        Intent intent = new Intent(MapsActivity.this, SavedActivity.class);
        startActivity(intent);
    }

    @Override
    public void viewLocationPosts(String location) {
        // Implementation from your original code
        double sx = 0, sy = 0;
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocationName(location, 1);
            if (listAddress.size() > 0) {
                sx = listAddress.get(0).getLatitude();
                sy = listAddress.get(0).getLongitude();
                LatLng latLng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
            }
        } catch (IOException e) {
            Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(MapsActivity.this, CheckPostsActivity.class);
        intent.putExtra("Location", "Location");
        intent.putExtra("Latitude",sx);
        intent.putExtra("Longitude",sy);
        startActivity(intent);
    }

    @Override
    public void createPost(String location, String content) {
        // Implementation from your original code
        double sx = 0, sy = 0;
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocationName(location, 1);
            if (listAddress.size() > 0) {
                sx = listAddress.get(0).getLatitude();
                sy = listAddress.get(0).getLongitude();
                LatLng latLng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
            }
        } catch (IOException e) {
            Toast.makeText(MapsActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(MapsActivity.this, PostActivity.class);
        intent.putExtra("Location", "Location: "+sx +" "+sy);
        intent.putExtra("Latitude", sx);
        intent.putExtra("Longitude", sy);
        startActivity(intent);
    }

    // Your helper methods
    private void launchPathFinderWithCoordinates(double sx, double sy, double dx, double dy) {
        Intent intent = new Intent(MapsActivity.this, PathFinder.class);

        // Pass the coordinates as extras
        intent.putExtra("START_LAT", sx);
        intent.putExtra("START_LNG", sy);
        intent.putExtra("DEST_LAT", dx);
        intent.putExtra("DEST_LNG", dy);

        startActivity(intent);
    }
}