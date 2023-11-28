package com.example.evchargingstationlocator;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class EVChargerActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, NavigationView.OnNavigationItemSelectedListener {

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
        setContentView(R.layout.activity_evcharger);


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


        Button saveLocationButton = findViewById(R.id.button); // Replace with the ID of your button
        saveLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current marker's position
                if (marker != null) {
                    LatLng location = marker.getPosition();
                    double latitude = location.latitude;
                    double longitude = location.longitude;

                    // show hospital using Google API (old code)
//                    StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
//                    stringBuilder.append("location="+latitude+","+longitude);
//                    stringBuilder.append("&radius=20000");
//                    stringBuilder.append("&type=hospital");
//                    stringBuilder.append("&sensor=true");
//                    stringBuilder.append("&key="+getResources().getString(R.string.google_map_key));
//
//                    String url1 = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=[AIzaSyCTwiwmIB9c5fkCs7bTiDzv2u6PyjgCkdY]&sensor=false&location=51.52864165,-0.10179430&radius=47022&keyword=%22london%20eye%22";
//
//                    String url = stringBuilder.toString();
//
//                    Toast.makeText(EVChargerActivity.this, "url = "+url1, Toast.LENGTH_SHORT).show();
//
//                    Object dataFetch[] = new Object[2];
//                    dataFetch[0] = myMap;
//                    dataFetch[1] = url1;
//
//                    FetchData fetchData = new FetchData(EVChargerActivity.this);
//                    fetchData.execute(dataFetch);

                    // NEW CODE (Overpass API)
                    // Start AsyncTask to perform network operation
                    new OverpassAPITask().execute(latitude, longitude);

                } else {
                    Toast.makeText(EVChargerActivity.this, "No location to save", Toast.LENGTH_SHORT).show();
                }
            }
        });



        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("EV Charger");

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
                    Toast.makeText(EVChargerActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
                }else{
                    Geocoder geocoder=new Geocoder(EVChargerActivity.this, Locale.getDefault());
                    try {
                        List<Address> listAddress=geocoder.getFromLocationName(location,1);
                        if(listAddress.size()>0){
                            myMap.clear();
                            LatLng latLng=new LatLng(listAddress.get(0).getLatitude(),listAddress.get(0).getLongitude());
                            // Add a marker on the map coordinates.
                            marker = myMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));

//                            myMap.setOnMarkerClickListener(this);
                            // Move the camera to the map coordinates and zoom in closer.
                            myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                            //Toast.makeText(EVChargerActivity.this, listAddress.get(0).getCountryName(), Toast.LENGTH_SHORT).show();
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                        }
                    } catch (IOException e) {
                        Toast.makeText(EVChargerActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
//                        throw new RuntimeException(e);
                    }
                }
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.MarkedLocation);
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
                    Toast.makeText(EVChargerActivity.this, "User Canceled Dialoge", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(EVChargerActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
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
        Geocoder geocoder=new Geocoder(EVChargerActivity.this, Locale.getDefault());
        try {
            List<Address> listAddress=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(listAddress.size()>0){
                myMap.clear();
                // Add a marker on the map coordinates.
                marker = myMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));

                myMap.setOnMarkerClickListener(this);
                // Move the camera to the map coordinates and zoom in closer.
                myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                //Toast.makeText(EVChargerActivity.this, listAddress.get(0).getCountryName(), Toast.LENGTH_SHORT).show();
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
        } catch (IOException e) {
            Toast.makeText(EVChargerActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

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
        Geocoder geocoder = new Geocoder(EVChargerActivity.this, Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (listAddress.size() > 0) {
                myMap.clear();
                // Add a marker on the map coordinates.
                marker = myMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));

                myMap.setOnMarkerClickListener(this);
                // Move the camera to the map coordinates and zoom in closer.
                myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                //Toast.makeText(EVChargerActivity.this, listAddress.get(0).getCountryName(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(EVChargerActivity.this, "Type any location", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(EVChargerActivity.this,MapsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId()==R.id.Saved_Locations){
            Intent intent = new Intent(EVChargerActivity.this,SavedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId()==R.id.MarkedLocation){
//            Intent intent = new Intent(EVChargerActivity.this,EVChargerActivity.class);
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
            Intent intent = new Intent(EVChargerActivity.this,LoginActivity.class);
            Toast.makeText(this, "LogOut Successfull", Toast.LENGTH_SHORT).show();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return super.onOptionsItemSelected(item);

    }

    // AsyncTask to perform network operation
    private class OverpassAPITask extends AsyncTask<Double, Void, String> {
        @Override
        protected String doInBackground(Double... params) {
            double latitude = params[0];
            double longitude = params[1];

            try {
                String query = "[out:json];" +
                        "node(around:10000," + latitude + "," + longitude + ")[amenity=fuel];out;";

                String url = "https://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(query, "UTF-8");

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");

                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                inputStream.close();
                conn.disconnect();

                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            if (jsonResponse != null) {
                // Process the JSON response here
                Log.d("JSON_RESPONSE", jsonResponse); // Logging the jsonResponse
                //Toast.makeText(EVChargerActivity.this, "json = " + jsonResponse, Toast.LENGTH_SHORT).show();
                try {
                    JSONObject jsonResponseObj = new JSONObject(jsonResponse);
                    JSONArray elementsArray = jsonResponseObj.getJSONArray("elements");

                    for (int i = 0; i < elementsArray.length(); i++) {
                        JSONObject element = elementsArray.getJSONObject(i);
                        if (element.has("lat") && element.has("lon")) {
                            double latitude = element.getDouble("lat");
                            double longitude = element.getDouble("lon");
                            String fuelName = "EV Charger Station"; // Default value if name is not available
                            JSONObject tags = element.optJSONObject("tags");
                            if (tags != null && tags.has("name")) {
                                fuelName = tags.getString("name");
                            }


                            // Create LatLng object for EV charger station location
                            LatLng fuelLatLng = new LatLng(latitude, longitude);

                            // Add marker for each EV charger station location
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(fuelLatLng)
                                    .title(fuelName); // Get EV charger station name if available, else use default "EV Charger Station" as title

                            // Add the marker to the map
                            Marker fuelMarker = myMap.addMarker(markerOptions);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(EVChargerActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        }
    }

}