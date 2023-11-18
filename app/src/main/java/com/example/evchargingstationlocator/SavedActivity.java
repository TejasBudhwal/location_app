package com.example.evchargingstationlocator;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import java.util.List;
import java.util.Locale;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class SavedActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, NavigationView.OnNavigationItemSelectedListener {

    ImageView imageViewSearch;
    EditText inputlocation;
    Button mapClicked;
    private Marker marker;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);


        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String user_id = currentUser.getUid();
        Toast.makeText(this, ""+user_id, Toast.LENGTH_SHORT).show();
        databaseReference = firebaseDatabase.getReference("Users").child(user_id).child("Locations");



//        Toolbar toolbar = (Toolbar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);


        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Saved Locations");

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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.Saved_Locations);

        markAllMarkers();
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

    private void markAllMarkers() {
        // Reference to your Firebase database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String user_id = currentUser.getUid();
// Reference to the user's locations node (replace "userId" with the actual user ID)
        DatabaseReference userLocationsRef = databaseReference.child("Users").child(user_id).child("Locations");

        userLocationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear existing markers from the map
                myMap.clear();

                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve latitude and longitude from the database
                    Double latitude = locationSnapshot.child("Latitude").getValue(Double.class);
                    Double longitude = locationSnapshot.child("Longitude").getValue(Double.class);

                    if (latitude != null && longitude != null) {
                        double la = latitude; // Convert Double object to double
                        double lo = longitude; // Convert Double object to double

                        // Create a LatLng object from latitude and longitude
                        LatLng locationLatLng = new LatLng(la, lo);

                        // Add a marker on the map for each location
                        myMap.addMarker(new MarkerOptions()
                                        .position(locationLatLng)
                                        .title("Marker Title")
                                        .snippet("Marker Snippet")
                                // You can customize the marker icon, colors, etc. here
                        );
                    } else {
                        // Handle the case where latitude or longitude is null
                        // Log an error, show a message, or skip adding the marker
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors when reading from the database
                // You can add error handling code here
            }
        });

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
                    Toast.makeText(SavedActivity.this, "User Canceled Dialoge", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SavedActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
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
        Geocoder geocoder=new Geocoder(SavedActivity.this, Locale.getDefault());
        try {
            List<Address> listAddress=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(listAddress.size()>0){
                myMap.clear();
                // Add a marker on the map coordinates.
                marker = myMap.addMarker(new MarkerOptions().position(latLng).title(""+listAddress.get(0).getCountryName()));

                myMap.setOnMarkerClickListener(this);
                // Move the camera to the map coordinates and zoom in closer.
                myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                Toast.makeText(SavedActivity.this, listAddress.get(0).getCountryName(), Toast.LENGTH_SHORT).show();
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
        } catch (IOException e) {
            Toast.makeText(SavedActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, "My Position"+marker.getPosition(), Toast.LENGTH_SHORT).show();
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SavedActivity.this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_menu, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Show the bottom sheet menu
        bottomSheetDialog.show();

        // Assuming you've already inflated the bottomSheetView
        Button photosButton = bottomSheetView.findViewById(R.id.photosButton);

        photosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to CheckPostActivity when the "Photos" button is clicked
                String message = String.valueOf(marker.getPosition());

                Intent intent = new Intent(SavedActivity.this, CheckPostsActivity.class);
                intent.putExtra("Location", message);
                Double lat = marker.getPosition().latitude;
                Double lon = marker.getPosition().longitude;
                intent.putExtra("Latitude",lat);
                intent.putExtra("Longitude",lon);
                startActivity(intent);


                // Dismiss the bottom sheet dialog if needed
                bottomSheetDialog.dismiss();
            }
        });


        return false;
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

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
            Intent intent = new Intent(SavedActivity.this,MapsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId()==R.id.Saved_Locations){
//            Intent intent = new Intent(SimpleMapViewActivity.this,ShortestActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
        }
        else if(item.getItemId()==R.id.MarkedLocation){
//            Intent intent = new Intent(SimpleMapViewActivity.this,MarkedActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
        }
        else if(item.getItemId()==R.id.share){
            Toast.makeText(this, "Please Share", Toast.LENGTH_SHORT).show();
        }
        else if(item.getItemId()==R.id.rate_us){
            Toast.makeText(this, "Please Rate US", Toast.LENGTH_SHORT).show();
        }
        else if(item.getItemId()==R.id.logout_main){
            Intent intent = new Intent(SavedActivity.this,LoginActivity.class);
            Toast.makeText(this, "LogOut Successfull", Toast.LENGTH_SHORT).show();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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

}