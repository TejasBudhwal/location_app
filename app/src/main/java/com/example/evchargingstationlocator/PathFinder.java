package com.example.evchargingstationlocator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutionException;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.*;

//import com.graphhopper.directions.api.client.ApiException;
//import com.graphhopper.directions.api.client.api.RoutingApi;
//import com.graphhopper.directions.api.client.model.ResponseInstruction;
//import com.graphhopper.directions.api.client.model.RouteResponse;
//import com.graphhopper.directions.api.client.model.RouteResponsePath;
//import com.graphhopper.directions.api.client.GraphHopperDirections;

import java.util.*;
import okhttp3.*;
import org.json.*;


public class PathFinder extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, NavigationView.OnNavigationItemSelectedListener, UniversalChatbotDialog.OnChatbotCommandListener {

    static {
        System.loadLibrary("evchargingstationlocator");
    }

    private ImageButton chatbotButton;
    ImageView imageViewSearch, imageViewSearch1;
    EditText inputlocation, inputlocation1;

    private Marker marker,marker1;

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    double mylat, mylong;

    DatabaseReference databaseReference;
    DatabaseReference requestReference;

    String userMail;

    private GoogleMap myMap;

    private MenuItem item;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_finder);

        chatbotButton = findViewById(R.id.chatbotButton);
        chatbotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChatbot();
            }
        });

        // Get the coordinates from intent extras
        double startLat = getIntent().getDoubleExtra("START_LAT", 0);
        double startLng = getIntent().getDoubleExtra("START_LNG", 0);
        double destLat = getIntent().getDoubleExtra("DEST_LAT", 0);
        double destLng = getIntent().getDoubleExtra("DEST_LNG", 0);

        // Set up map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                myMap = googleMap;

                // Now that the map is ready, create PathPlanner and draw the path
                PathPlanner pathPlanner = new PathPlanner();
                pathPlanner.drawPath(startLat, startLng, destLat, destLng);
            }
        });

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String user_id = currentUser.getUid();
        userMail = currentUser.getEmail();
        //Toast.makeText(this, ""+user_id, Toast.LENGTH_SHORT).show();
        databaseReference = firebaseDatabase.getReference("Users").child(user_id).child("Locations");

        requestReference = firebaseDatabase.getReference("Charging Requests");

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
                    //marker.getPosition()
                    LatLong location = new LatLong(marker.getPosition().latitude, marker.getPosition().longitude);
                    LatLng loc = marker.getPosition();
                    double latitude = location.lat;
                    double longitude = location.lng;
                    PathPlanner planner = new PathPlanner();
                    planner.drawPath(marker.getPosition().latitude, marker.getPosition().longitude, marker1.getPosition().latitude, marker1.getPosition().longitude);
//                    new OverpassAPITask().execute(latitude, longitude);
                    float zoomLevel = 13.0f; // You can adjust the zoom level as needed
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(loc, zoomLevel);
                    myMap.animateCamera(cameraUpdate);
                } else {
                    Toast.makeText(PathFinder.this, "No location to save", Toast.LENGTH_SHORT).show();
                }
            }
        });



        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Path Finder");

        toolbar.inflateMenu(R.menu.menu);
//        mapView.findViewById(R.id.mapView);
        checkPermission();

        if (isPermissionGranter) {
            if (checkGooglePlayServices()) {
//                mapView.getMapAsync(this);
//                mapView.onCreate(savedInstanceState);
                Toast.makeText(this, "Google Play service Available", Toast.LENGTH_SHORT).show();

                SupportMapFragment mapFragment1 = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment1.getMapAsync(this);
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
        imageViewSearch= (ImageView) findViewById(R.id.imageViewSearch);
        inputlocation= (EditText) findViewById(R.id.inputLocation);



        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location =inputlocation.getText().toString();
                if(location==null){
                    Toast.makeText(PathFinder.this, "Type any location", Toast.LENGTH_SHORT).show();
                }else{
                    Geocoder geocoder=new Geocoder(PathFinder.this, Locale.getDefault());
                    try {
                        List<Address> listAddress=geocoder.getFromLocationName(location,1);
                        if(listAddress.size()>0){
                            LatLng latLong=new LatLng(listAddress.get(0).getLatitude(),listAddress.get(0).getLongitude());
                            // Add a marker on the map coordinates.
                            marker = myMap.addMarker(new MarkerOptions().position(latLong).title("Current Location"));

                            myMap.animateCamera(CameraUpdateFactory.newLatLng(latLong));
                        }
                    } catch (IOException e) {
                        Toast.makeText(PathFinder.this, "Type any location", Toast.LENGTH_SHORT).show();
//                        throw new RuntimeException(e);
                    }
                }
            }
        });

        imageViewSearch1= (ImageView) findViewById(R.id.imageViewSearch1);
        inputlocation1= (EditText) findViewById(R.id.inputLocation1);



        imageViewSearch1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location =inputlocation1.getText().toString();
                if(location==null){
                    Toast.makeText(PathFinder.this, "Type any location", Toast.LENGTH_SHORT).show();
                }else{
                    Geocoder geocoder=new Geocoder(PathFinder.this, Locale.getDefault());
                    try {
                        List<Address> listAddress=geocoder.getFromLocationName(location,1);
                        if(listAddress.size()>0){
                            LatLng latLong=new LatLng(listAddress.get(0).getLatitude(),listAddress.get(0).getLongitude());
                            // Add a marker on the map coordinates.
                            marker1 = myMap.addMarker(new MarkerOptions().position(latLong).title("Current Location"));

                            myMap.animateCamera(CameraUpdateFactory.newLatLng(latLong));
                        }
                    } catch (IOException e) {
                        Toast.makeText(PathFinder.this, "Type any location", Toast.LENGTH_SHORT).show();
//                        throw new RuntimeException(e);
                    }
                }
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.PathFinder);
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
                    Toast.makeText(PathFinder.this, "User Canceled Dialoge", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PathFinder.this, "Permission Granted", Toast.LENGTH_SHORT).show();
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
        LatLong latLong = new LatLong(35.00116, 135.7681);
        LatLng latLng = new LatLng(35.00116, 135.7681);
        Geocoder geocoder=new Geocoder(PathFinder.this, Locale.getDefault());
        try {
            List<Address> listAddress=geocoder.getFromLocation(latLong.lat,latLong.lng,1);
            if(listAddress.size()>0){
                myMap.clear();
                mylat = latLong.lat;
                mylong = latLong.lng;
                myMap.setOnMarkerClickListener(this);
                myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        } catch (IOException e) {
            Toast.makeText(PathFinder.this, "Type any location", Toast.LENGTH_SHORT).show();
        }

        myMap.setOnMarkerClickListener(this);
        myMap.setOnMapClickListener(this);
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.setTrafficEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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
        return false;
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        LatLong latLong = new LatLong(latLng.latitude, latLng.longitude);
        Geocoder geocoder = new Geocoder(PathFinder.this, Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocation(latLong.lat, latLong.lng, 1);
            if (listAddress.size() > 0) {
                mylat = latLong.lat;
                mylong = latLong.lng;
                myMap.setOnMarkerClickListener(this);
                myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        } catch (IOException e) {
            Toast.makeText(PathFinder.this, "Type any location", Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(PathFinder.this,MapsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId()==R.id.Saved_Locations){
            Intent intent = new Intent(PathFinder.this,SavedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId()==R.id.MarkedLocation){
            Intent intent = new Intent(PathFinder.this,EVChargerActivity.class);
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
            Intent intent = new Intent(PathFinder.this,ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId()==R.id.logout_main){
            FirebaseAuth.getInstance().signOut(); // Sign out the user from Firebase

            Intent intent = new Intent(PathFinder.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Logout Successful", Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return super.onOptionsItemSelected(item);

    }

    public class PathPlanner {

        private final List<PathSegment> segmentList = new ArrayList<>();
        private static final double EARTH_RADIUS = 6371.0; // Earth radius in kilometers
        private static final double EARTH_RADIUS_MILES = 3963.19; // Earth's radius in mile
        private static final double SEGMENT_LENGTH = 62.14; // 100 km in miles segment length

        // OkHttpClient instance
        private final OkHttpClient client = new OkHttpClient();
        public PathPlanner() {
        }

        // Function to calculate intermediate points between two coordinates
        private LatLong interpolate(LatLong start, LatLong end, double fraction) {
            double lat1 = Math.toRadians(start.lat);
            double lng1 = Math.toRadians(start.lng);
            double lat2 = Math.toRadians(end.lat);
            double lng2 = Math.toRadians(end.lng);

            double d = calculateDistance(start, end) / EARTH_RADIUS;
            double a = Math.sin((1 - fraction) * d) / Math.sin(d);
            double b = Math.sin(fraction * d) / Math.sin(d);

            double x = a * Math.cos(lat1) * Math.cos(lng1) + b * Math.cos(lat2) * Math.cos(lng2);
            double y = a * Math.cos(lat1) * Math.sin(lng1) + b * Math.cos(lat2) * Math.sin(lng2);
            double z = a * Math.sin(lat1) + b * Math.sin(lat2);

            double interpolatedLat = Math.atan2(z, Math.sqrt(x * x + y * y));
            double interpolatedLng = Math.atan2(y, x);

            return new LatLong(Math.toDegrees(interpolatedLat), Math.toDegrees(interpolatedLng));
        }

        // Function to divide the path into segments of fixed length (e.g., 100 km)
//        public List<LatLong> dividePath(LatLong start, LatLong goal) {
//            List<LatLong> pathPoints = new ArrayList<>();
//            // Calculate the total distance between the start and goal points
//            double totalDistance = calculateDistance(start, goal);
//            // Add the start point to the list
//            pathPoints.add(start);
//
//            // Divide the path into segments
//            double numSegments = Math.floor(totalDistance / SEGMENT_LENGTH);
//            for (int i = 1; i <= numSegments; i++) {
//                double fraction = i / numSegments;
//                LatLong intermediatePoint = interpolate(start, goal, fraction);
//                pathPoints.add(intermediatePoint);
//            }
//
//            // Add the goal point to the list
//            pathPoints.add(goal);
//
//            return pathPoints;
//        }
        // Above code is previous working code

        // Put this interface at the class level (not inside any method)
//        public interface PathDivisionCallback {
//            void onPathDivided(List<LatLong> pathPoints);
//        }
//
//        public void dividePath(final LatLong start, final LatLong goal, final PathDivisionCallback callback) {
//            final double SEGMENT_LENGTH = 200000; // 200 km in meters
//
//            // Format coordinates for GraphHopper API
//            String startPoint = start.lat + "," + start.lng;
//            String endPoint = goal.lat + "," + goal.lng;
//
//            // Create and execute the GraphHopperPolylineTask
//            GraphHopperPolylineTask routeTask = new GraphHopperPolylineTask(
//                    "car",
//                    startPoint,
//                    endPoint,
//                    new GraphHopperPolylineTask.RoutingCallback() {
//                        @Override
//                        public void onRoutingCompleted(List<LatLong> coordinates) {
//                            List<LatLong> pathPoints = new ArrayList<>();
//
//                            if (coordinates != null && !coordinates.isEmpty()) {
//                                double accumulatedDistance = 0.0;
//                                LatLong previous = null;
//
//                                for (LatLong current : coordinates) {
//                                    if (previous != null) {
//                                        accumulatedDistance += calculateDistance(previous, current); // in meters
//
//                                        if (accumulatedDistance >= SEGMENT_LENGTH) {
//                                            pathPoints.add(current);
//                                            accumulatedDistance = 0.0;
//                                        }
//                                    } else {
//                                        pathPoints.add(current); // Add the start point
//                                    }
//
//                                    previous = current;
//                                }
//
//                                // Ensure the goal point is added if not already in the list
//                                if (!pathPoints.contains(goal)) {
//                                    pathPoints.add(goal);
//                                }
//                            }
//
//                            // Return the result through the callback
//                            callback.onPathDivided(pathPoints);
//                        }
//                    }
//            );
//
//            routeTask.execute();
//        }
        public void dividePath(LatLong start, LatLong goal,Callback callback) {


            String vehicle = "car";
            String apiKey = "be914761-9004-4ad2-8553-643db2fb596b"; // Replace with your key

            String startPoint = start.lat + "," + start.lng;
            String endPoint = goal.lat + "," + goal.lng;

            String apiUrl = "https://graphhopper.com/api/1/route?" +
                    "point=" + startPoint +
                    "&point=" + endPoint +
                    "&snap_prevention=motorway&snap_prevention=ferry&snap_prevention=tunnel" +
                    "&details=road_class&details=surface" +
                    "&profile=" + vehicle +
                    "&locale=en&instructions=true&calc_points=true&points_encoded=false" +
                    "&key=" + apiKey;

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(apiUrl).get().build();
            client.newCall(request).enqueue(callback);

//                if (!response.isSuccessful() || response.body() == null) {
//                    Log.e("GH_DIVIDE_PATH", "Request failed: " + response.code());
//                    return pathPoints;
//                }
//
//                String responseString = response.body().string();
//                JSONObject jsonResponse = new JSONObject(responseString);
//                JSONArray paths = jsonResponse.getJSONArray("paths");
//                if (paths.length() == 0) return pathPoints;
//
//                JSONArray coordinates = paths.getJSONObject(0)
//                        .getJSONObject("points")
//                        .getJSONArray("coordinates");
//
//                double accumulatedDistance = 0.0;
//                LatLong previous = null;
//
//                for (int i = 0; i < coordinates.length(); i++) {
//                    JSONArray coord = coordinates.getJSONArray(i);
//                    double lon = coord.getDouble(0);
//                    double lat = coord.getDouble(1);
//                    LatLong current = new LatLong(lat, lon);
//
//                    if (previous != null) {
//                        double dist = calculateDistance(previous, current);
//                        accumulatedDistance += dist;
//
//                        if (accumulatedDistance >= SEGMENT_LENGTH) {
//                            pathPoints.add(current);
//                            accumulatedDistance = 0.0;
//                        }
//                    } else {
//                        pathPoints.add(current); // Add start point
//                    }
//
//                    previous = current;
//                }
//
//                // Always add the goal point
//                if (pathPoints.size() == 0 || !pathPoints.get(pathPoints.size() - 1).equals(goal)) {
//                    pathPoints.add(goal);
//                }
        }




        // Function to query nearby petrol stations from OverpassAPI for a given LatLong
        public void queryNearbyStations(LatLong start, LatLong end, Callback callback) {
            String overpassQuery = createOverpassQuery(start, end); // Implement this function
            Request request = new Request.Builder()
                    .url(overpassQuery)
                    .build();

            client.newCall(request).enqueue(callback);
        }

        // Create an OverpassAPI query to find petrol stations
        private String createOverpassQuery(LatLong start, LatLong end) {
            // Calculate the distance between the start and end points in kilometers
            double distance = calculateDistance(start, end);

            // Convert the distance into meters for use as the radius in the Overpass API query
            double radius = distance * 1000;

            // Generate the Overpass API query using the calculated radius
            String query = "https://overpass-api.de/api/interpreter?data=[out:json];node[amenity=fuel](around:"
                    + radius + "," + start.lat + "," + start.lng + ");out;";

            return query;
        }

        // Function to calculate optimal petrol station for a segment
        public LatLong selectOptimalStation(List<LatLong> stations, LatLong start, LatLong end) {
            LatLong optimalStation = null;
            double minDistance = Double.MAX_VALUE;

            for (LatLong station : stations) {
                double distance = calculateDistance(start, station) + calculateDistance(station, end);
                if (distance < minDistance) {
                    minDistance = distance;
                    optimalStation = station;
                }
            }
            return optimalStation;
        }

        // Haversine formula to calculate the great-circle distance between two points
        private double calculateDistance(LatLong point1, LatLong point2) {
            double PI = Math.PI;
            double dlat1 = point1.lat * (PI / 180);
            double dlong1 = point1.lng * (PI / 180);
            double dlat2 = point2.lat * (PI / 180);
            double dlong2 = point2.lng * (PI / 180);

            double dLong = dlong1 - dlong2;
            double dLat = dlat1 - dlat2;

            double aHarv = Math.pow(Math.sin(dLat / 2.0), 2.0) +
                    Math.cos(dlat1) * Math.cos(dlat2) * Math.pow(Math.sin(dLong / 2), 2);
            double cHarv = 2 * Math.atan2(Math.sqrt(aHarv), Math.sqrt(1.0 - aHarv));

            double distance = EARTH_RADIUS_MILES * cHarv;
            return 2 * distance;
        }

        public void drawPath(double sx, double sy, double gx, double gy) {
            LatLong startPoint = new LatLong(sx, sy);
            LatLong endPoint = new LatLong(gx, gy);
            dividePath(startPoint, endPoint, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException{
                    if (!response.isSuccessful() || response.body() == null) {
                        Log.e("GH_DIVIDE_PATH", "Request failed: " + response.code());
                        return ;
                    }
                    List<LatLong> pathPoints = new ArrayList<>();

                    String responseString = response.body().string();
                    Log.e("Jason Response", responseString);
                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(responseString);
                        JSONArray paths = null;
                        paths = jsonResponse.getJSONArray("paths");
                        if (paths.length() == 0) return;

                        JSONArray coordinates = paths.getJSONObject(0)
                                .getJSONObject("points")
                                .getJSONArray("coordinates");

                        double accumulatedDistance = 0.0;
                        LatLong previous = null;

                        for (int i = 0; i < coordinates.length(); i++) {
                            JSONArray coord = coordinates.getJSONArray(i);
                            double lon = coord.getDouble(0);
                            double lat = coord.getDouble(1);
                            LatLong current = new LatLong(lat, lon);
                            Log.e("Path Point ",current.lat+" "+current.lng);

                            if (previous != null) {
                                double dist = calculateDistance(previous, current);
                                accumulatedDistance += dist;

                                if (accumulatedDistance >= SEGMENT_LENGTH) {

                                    pathPoints.add(current);
                                    accumulatedDistance = 0.0;
                                }
                            } else {
                                pathPoints.add(current); // Add start point
                            }

                            previous = current;
                        }

                        // Always add the goal point
                        if (pathPoints.size() == 0 || !pathPoints.get(pathPoints.size() - 1).equals(endPoint)) {
                            pathPoints.add(endPoint);
                        }
                        Log.e("Final Points ",pathPoints.toString());

                        final List<IndexedLatLong> petrolStations = new ArrayList<>();
                        final CountDownLatch latch = new CountDownLatch(pathPoints.size() - 1);

                        for (int i = 0; i < pathPoints.size() - 1; i++) {
                            LatLong start = pathPoints.get(i);
                            LatLong end = pathPoints.get(i + 1);

                            int finalI = i;
                            queryNearbyStations(start, end, new Callback() {
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String responseData = response.body().string();
                                    List<LatLong> stations = parseOverpassResponse(responseData);  // Parse response
                                    LatLong optimalStation = selectOptimalStation(stations, start, end);

                                    runOnUiThread(() -> {
                                        if (optimalStation != null) {
                                            Log.d("Station:", "" + optimalStation.lat + " hhe " + optimalStation.lng);
                                            // Store the optimal station with its index
                                            petrolStations.add(new IndexedLatLong(optimalStation, finalI));
                                        }
                                    });

                                    latch.countDown();
                                }

                                @Override
                                public void onFailure(Call call, IOException e) {
                                    e.printStackTrace();
                                    latch.countDown(); // Count down even if there's a failure
                                }
                            });
                        }

                        new Thread(() -> {
                            try {
                                latch.await(1, TimeUnit.MINUTES); // Wait up to 1 minute for all queries to complete

                                runOnUiThread(() -> {
                                    // Sort petrolStations based on the index
                                    petrolStations.sort(Comparator.comparingInt(station -> station.index));
                                    petrolStations.add(0, new IndexedLatLong(startPoint, -1));

                                    // Filter stations based on distance
                                    List<IndexedLatLong> filteredStations = new ArrayList<>();
                                    filteredStations.add(petrolStations.get(0)); // Add starting point

                                    for (int i = 0; i < petrolStations.size() - 1; i++) {
                                        IndexedLatLong currentStation = filteredStations.get(filteredStations.size() - 1);
                                        IndexedLatLong nextStation = petrolStations.get(i + 1);

                                        // Check if the distance between the current and next station is less than 50 km
                                        double distance = calculateDistance(currentStation.latLong, nextStation.latLong);

                                        if (distance >= 100) {
                                            filteredStations.add(nextStation);
                                        }
                                    }

                                    // Add end point as the last station if it's not already included
                                    if (filteredStations.isEmpty() || !filteredStations.get(filteredStations.size() - 1).latLong.equals(endPoint)) {
                                        filteredStations.add(new IndexedLatLong(endPoint, filteredStations.size()));
                                    }

                                    // Draw path based on sorted petrol stations
                                    if (filteredStations.isEmpty()) {
                                        Toast.makeText(PathFinder.this, "No petrol stations found. Routing directly.", Toast.LENGTH_SHORT).show();

                                        String startPoints = sx + "," + sy;
                                        String endPoints = gx + "," + gy;

                                        GraphHopperRoutingTask routingTask = new GraphHopperRoutingTask("car", startPoints, endPoints, myMap, PathFinder.this, new GraphHopperRoutingTask.RoutingCallback() {
                                            @Override
                                            public void onRoutingCompleted(String result) {
                                                if (result != null) {
                                                    Log.e("result = ", result);
                                                } else {
                                                    Log.e("result = ", "no result obtained");
                                                }
                                            }
                                        });

                                        routingTask.execute();

                                        LatLng startLatLng = new LatLng(sx, sy);
                                        MarkerOptions markerOptions = new MarkerOptions()
                                                .position(startLatLng)
                                                .title("Start");

                                        myMap.addMarker(markerOptions).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                                    } else {

                                        // Routing from start to the first petrol station
                                        IndexedLatLong firstStation = filteredStations.get(0);
                                        String startPoints = sx + "," + sy;
                                        String endPoints = firstStation.latLong.lat + "," + firstStation.latLong.lng;

                                        GraphHopperRoutingTask routingTask = new GraphHopperRoutingTask("car", startPoints, endPoints, myMap, PathFinder.this, new GraphHopperRoutingTask.RoutingCallback() {
                                            @Override
                                            public void onRoutingCompleted(String result) {
                                                if (result != null) {
                                                    Log.e("result = ", result);
                                                } else {
                                                    Log.e("result = ", "no result obtained");
                                                }
                                            }
                                        });

                                        routingTask.execute();

                                        LatLng firstLatLng = new LatLng(firstStation.latLong.lat, firstStation.latLong.lng);
                                        MarkerOptions markerOptions = new MarkerOptions()
                                                .position(firstLatLng)
                                                .title("Start");

                                        myMap.addMarker(markerOptions).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                                        // Routing between petrol stations
                                        for (int i = 0; i < filteredStations.size() - 1; i++) {
                                            IndexedLatLong currentStation = filteredStations.get(i);
                                            IndexedLatLong nextStation = filteredStations.get(i + 1);

                                            startPoints = currentStation.latLong.lat + "," + currentStation.latLong.lng;
                                            endPoints = nextStation.latLong.lat + "," + nextStation.latLong.lng;

                                            routingTask = new GraphHopperRoutingTask("car", startPoints, endPoints, myMap, PathFinder.this, new GraphHopperRoutingTask.RoutingCallback() {
                                                @Override
                                                public void onRoutingCompleted(String result) {
                                                    if (result != null) {
                                                        Log.e("result = ", result);
                                                    } else {
                                                        Log.e("result = ", "no result obtained");
                                                    }
                                                }
                                            });

                                            routingTask.execute();

                                            LatLng currentLatLng = new LatLng(currentStation.latLong.lat, currentStation.latLong.lng);
                                            markerOptions = new MarkerOptions()
                                                    .position(currentLatLng)
                                                    .title("Station " + i);

                                            myMap.addMarker(markerOptions).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                        }

                                        // Routing from the last petrol station to the end
                                        IndexedLatLong lastStation = filteredStations.get(filteredStations.size() - 1);
                                        startPoints = lastStation.latLong.lat + "," + lastStation.latLong.lng;
                                        endPoints = gx + "," + gy;

                                        routingTask = new GraphHopperRoutingTask("car", startPoints, endPoints, myMap, PathFinder.this, new GraphHopperRoutingTask.RoutingCallback() {
                                            @Override
                                            public void onRoutingCompleted(String result) {
                                                if (result != null) {
                                                    Log.e("result = ", result);
                                                } else {
                                                    Log.e("result = ", "no result obtained");
                                                }
                                            }
                                        });

                                        routingTask.execute();

                                        LatLng lastLatLng = new LatLng(lastStation.latLong.lat, lastStation.latLong.lng);
                                        markerOptions = new MarkerOptions()
                                                .position(lastLatLng)
                                                .title("End");

                                        myMap.addMarker(markerOptions).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                    }
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
//            List<LatLong> segmentPoints = dividePath(startPoint, endPoint);  // Divide path into 100km segments


        }

        // Helper method to parse Overpass API response
        public List<LatLong> parseOverpassResponse(String jsonData) {
            List<LatLong> stations = new ArrayList<>();

            try {
                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(jsonData);

                // Get the array of nodes (petrol stations)
                JSONArray elements = jsonResponse.getJSONArray("elements");

                // Iterate through each element
                for (int i = 0; i < elements.length(); i++) {
                    JSONObject element = elements.getJSONObject(i);

                    // Check if the element has the required fields
                    if (element.has("lat") && element.has("lon")) {
                        double lat = element.getDouble("lat");
                        double lon = element.getDouble("lon");

                        // Create a LatLong object and add it to the list
                        LatLong station = new LatLong(lat, lon);
                        stations.add(station);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // Handle JSON parsing errors
            }

            return stations;
        }
    }

    // Class to represent latitude and longitude points
    static class LatLong {
        double lat, lng;

        public LatLong(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LatLong latLong = (LatLong) o;

            if (Double.compare(latLong.lat, lat) != 0) return false;
            return Double.compare(latLong.lng, lng) == 0;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(lat);
            result = (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(lng);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }

    public static class PathSegment {
        List<LatLng> pathPoints;

        public PathSegment(List<LatLng> pathPoints) {
            this.pathPoints = pathPoints;
        }

        public List<LatLng> getPathPoints() {
            return pathPoints;
        }
    }

    public class IndexedLatLong {
        public LatLong latLong;
        public int index;

        public IndexedLatLong(LatLong latLong, int index) {
            this.latLong = latLong;
            this.index = index;
        }
    }

    private void showChatbot() {
        UniversalChatbotDialog chatbotDialog = new UniversalChatbotDialog(this, this);
        chatbotDialog.show();
    }

    // UniversalChatbotDialog.OnChatbotCommandListener implementations
    @Override
    public void findEVChargers(String location, String vehicleType, int radius) {
        // Implementation from your original code
        double sx = 0, sy = 0;
        Geocoder geocoder = new Geocoder(PathFinder.this, Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocationName(location, 1);
            if (listAddress.size() > 0) {
                sx = listAddress.get(0).getLatitude();
                sy = listAddress.get(0).getLongitude();
                LatLng latLng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
            }
        } catch (IOException e) {
            Toast.makeText(PathFinder.this, "Type any location", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(PathFinder.this, EVChargerActivity.class);

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
        Geocoder geocoder = new Geocoder(PathFinder.this, Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocationName(source, 1);
            if (listAddress.size() > 0) {
                sx = listAddress.get(0).getLatitude();
                sy = listAddress.get(0).getLongitude();
                LatLng latLng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
            }
        } catch (IOException e) {
            Toast.makeText(PathFinder.this, "Type any location", Toast.LENGTH_SHORT).show();
        }
        try {
            List<Address> listAddress = geocoder.getFromLocationName(destination, 1);
            if (listAddress.size() > 0) {
                dx = listAddress.get(0).getLatitude();
                dy = listAddress.get(0).getLongitude();
                LatLng latLng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
            }
        } catch (IOException e) {
            Toast.makeText(PathFinder.this, "Type any location", Toast.LENGTH_SHORT).show();
        }
        launchPathFinderWithCoordinates(sx, sy, dx, dy);
    }

    @Override
    public void saveLocation(String location, String label) {
        // Implementation from your original code
        double sx = 0, sy = 0;
        Geocoder geocoder = new Geocoder(PathFinder.this, Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocationName(location, 1);
            if (listAddress.size() > 0) {
                sx = listAddress.get(0).getLatitude();
                sy = listAddress.get(0).getLongitude();
                LatLng latLng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
            }
        } catch (IOException e) {
            Toast.makeText(PathFinder.this, "Type any location", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(PathFinder.this, MapsActivity.class);
        intent.putExtra("Location", "Location: "+sx +" "+sy);
        intent.putExtra("Latitude", sx);
        intent.putExtra("Longitude", sy);
        startActivity(intent);

        Toast.makeText(PathFinder.this, "Location saved to Firebase", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void viewSavedLocations() {
        // Implementation from your original code
        Intent intent = new Intent(PathFinder.this, SavedActivity.class);
        startActivity(intent);
    }

    @Override
    public void viewLocationPosts(String location) {
        // Implementation from your original code
        double sx = 0, sy = 0;
        Geocoder geocoder = new Geocoder(PathFinder.this, Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocationName(location, 1);
            if (listAddress.size() > 0) {
                sx = listAddress.get(0).getLatitude();
                sy = listAddress.get(0).getLongitude();
                LatLng latLng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
            }
        } catch (IOException e) {
            Toast.makeText(PathFinder.this, "Type any location", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(PathFinder.this, CheckPostsActivity.class);
        intent.putExtra("Location", "Location");
        intent.putExtra("Latitude",sx);
        intent.putExtra("Longitude",sy);
        startActivity(intent);
    }

    @Override
    public void createPost(String location, String content) {
        // Implementation from your original code
        double sx = 0, sy = 0;
        Geocoder geocoder = new Geocoder(PathFinder.this, Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocationName(location, 1);
            if (listAddress.size() > 0) {
                sx = listAddress.get(0).getLatitude();
                sy = listAddress.get(0).getLongitude();
                LatLng latLng = new LatLng(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
            }
        } catch (IOException e) {
            Toast.makeText(PathFinder.this, "Type any location", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(PathFinder.this, PostActivity.class);
        intent.putExtra("Location", "Location: "+sx +" "+sy);
        intent.putExtra("Latitude", sx);
        intent.putExtra("Longitude", sy);
        startActivity(intent);
    }

    // Your helper methods
    private void launchPathFinderWithCoordinates(double sx, double sy, double dx, double dy) {
        Intent intent = new Intent(PathFinder.this, PathFinder.class);

        // Pass the coordinates as extras
        intent.putExtra("START_LAT", sx);
        intent.putExtra("START_LNG", sy);
        intent.putExtra("DEST_LAT", dx);
        intent.putExtra("DEST_LNG", dy);

        startActivity(intent);
    }
}

//{"hints":{"visited_nodes.sum":4005,"visited_nodes.average":4005.0},"info":{"copyrights":["GraphHopper","OpenStreetMap contributors"],"took":20,"road_data_timestamp":"2025-04-03T01:00:00Z"},"paths":[{"distance":900021.698,"weight":60938.439661,"time":47438136,"transfers":0,"points_encoded":false,"bbox":[75.74059,26.832987,80.946325,30.89527],"points":{"type":"LineString","coordinates":[[75.744036,30.858883],[75.744192,30.859238],[75.743722,30.859362],[75.743523,30.859445],[75.743335,30.859542],[75.743113,30.859673],[75.742935,30.85979],[75.74285,30.859885],[75.742817,30.859971],[75.742815,30.860114],[75.742841,30.860249],[75.742884,30.86038],[75.742946,30.860539],[75.743094,30.860845],[75.743211,30.860966],[75.743349,30.861068],[75.743569,30.861213],[75.743638,30.861285],[75.74378,30.861543],[75.743858,30.861669],[75.743982,30.861826],[75.744309,30.862211],[75.744069,30.862835],[75.744051,30.862903],[75.744024,30.863227],[75.744014,30.863459],[75.743993,30.864524],[75.744003,30.866405],[75.744013,30.866637],[75.744019,30.870384],[75.740653,30.869179],[75.74059,30.869294],[75.748187,30.872032],[75.752809,30.873733],[75.756297,30.874991],[75.759915,30.876263],[75.762735,30.877297],[75.763911,30.877698],[75.764518,30.877919],[75.765055,30.878143],[75.766099,30.878556],[75.768715,30.879487],[75.769901,30.879918],[75.772673,30.88091],[75.774541,30.881607],[75.774492,30.881691],[75.775916,30.882247],[75.776835,30.882579],[75.777883,30.882926],[75.77958,30.883524],[75.782227,30.884493],[75.783409,30.884892],[75.786111,30.885872],[75.787823,30.886467],[75.789434,30.886993],[75.789728,30.887102],[75.789889,30.887153],[75.790334,30.887328],[75.790949,30.887533],[75.791201,30.887638],[75.791744,30.887823],[75.79201,30.887932],[75.793963,30.888627],[75.795393,30.889123],[75.797,30.889719],[75.797098,30.889763],[75.798676,30.890326],[75.800392,30.890892],[75.801361,30.891242],[75.802219,30.89158],[75.803346,30.892048],[75.803864,30.892236],[75.805504,30.892886],[75.805501,30.892985],[75.805491,30.893026],[75.805469,30.893064],[75.805436,30.893096],[75.805395,30.89312],[75.803744,30.893736],[75.802108,30.894384],[75.80169,30.894503],[75.800827,30.89482],[75.800766,30.894857],[75.800715,30.894904],[75.800676,30.894959],[75.800656,30.895004],[75.800648,30.895053],[75.800654,30.895102],[75.800673,30.895148],[75.800703,30.89519],[75.800744,30.895224],[75.800793,30.895249],[75.800847,30.895264],[75.800898,30.89527],[75.801,30.895258],[75.803023,30.894486],[75.80362,30.894239],[75.804933,30.893762],[75.806235,30.893265],[75.806813,30.893033],[75.807557,30.892717],[75.808307,30.89241],[75.809062,30.892114],[75.809822,30.891828],[75.810299,30.891678],[75.810765,30.8915],[75.810837,30.891461],[75.810899,30.891418],[75.810948,30.89136],[75.810973,30.891311],[75.810982,30.891257],[75.810978,30.891213],[75.810959,30.891166],[75.81095,30.891116],[75.810954,30.891056],[75.810974,30.891007],[75.811012,30.890966],[75.811071,30.890926],[75.811149,30.89089],[75.811493,30.890781],[75.812543,30.890392],[75.812781,30.890249],[75.816832,30.888661],[75.827008,30.884837],[75.827527,30.884624],[75.827833,30.884532],[75.828464,30.884251],[75.830095,30.883604],[75.831345,30.883139],[75.831602,30.883053],[75.831751,30.882988],[75.834747,30.881845],[75.836107,30.881298],[75.837029,30.880964],[75.838306,30.880467],[75.839303,30.880092],[75.840571,30.879593],[75.841207,30.879371],[75.841841,30.879112],[75.842746,30.878771],[75.843233,30.878565],[75.84335,30.878464],[75.843962,30.878148],[75.845719,30.877148],[75.846078,30.876908],[75.846717,30.87642],[75.847252,30.875982],[75.848155,30.875197],[75.848233,30.875171],[75.848279,30.875146],[75.849313,30.87422],[75.849883,30.873721],[75.849955,30.873644],[75.849968,30.873587],[75.850016,30.87354],[75.852091,30.871705],[75.852399,30.871412],[75.85483,30.869276],[75.85494,30.86924],[75.855181,30.869036],[75.856275,30.868058],[75.85664,30.867762],[75.856676,30.867721],[75.856697,30.867633],[75.857374,30.867009],[75.858577,30.865949],[75.859,30.865592],[75.861387,30.863472],[75.8
//2025-04-10 03:41:52.446  6648-7735  System.err              com...mple.evchargingstationlocator  W