package com.example.evchargingstationlocator;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphHopperRoutingTask extends AsyncTask<Void, Void, String> {

    private final String apiKey = "be914761-9004-4ad2-8553-643db2fb596b"; // Replace with your actual API key
    private String startPoint; // Starting point in latitude,longitude format
    private String endPoint; // Ending point in latitude,longitude format
    private String apiUrl; // URL for GraphHopper API

    private RoutingCallback callback;
    private GoogleMap map;

    private Context mContext;


    public GraphHopperRoutingTask(String startPoint, String endPoint, GoogleMap map, Context context, RoutingCallback callback) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.callback = callback;
        this.map = map;

        this.mContext = context;

        this.apiUrl = "https://graphhopper.com/api/1/route?point=" + startPoint + "&point=" + endPoint + "&snap_prevention=motorway&snap_prevention=ferry&snap_prevention=tunnel&details=road_class&details=surface&profile=car&locale=de&instructions=true&calc_points=true&points_encoded=false&key=" + apiKey;
    }

    @Override
    protected String doInBackground(Void... voids) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (callback != null && result != null) {
            // Parse the JSON result and draw the route on the map
            drawRouteOnMap(result);
            callback.onRoutingCompleted(result);
        }
    }

    private void drawRouteOnMap(String result) {
        try {
            JSONObject jsonResponse = new JSONObject(result);
            JSONArray paths = jsonResponse.getJSONArray("paths");
            if (paths.length() > 0) {
                JSONObject path = paths.getJSONObject(0); // Get the first path (assuming only one path)
                JSONObject pointsObject = path.getJSONObject("points");

                // Get the coordinates array from points object
                JSONArray coordinates = pointsObject.getJSONArray("coordinates");

                // Decode coordinates to LatLng list
                List<LatLng> decodedPoints = new ArrayList<>();
                for (int i = 0; i < coordinates.length(); i++) {
                    JSONArray coordinate = coordinates.getJSONArray(i);
                    double latitude = coordinate.getDouble(1);
                    double longitude = coordinate.getDouble(0);
                    decodedPoints.add(new LatLng(latitude, longitude));
                }

                // Draw the polyline on the map
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(decodedPoints)
                        .color(Color.BLUE)
                        .width(8);

                map.addPolyline(polylineOptions);

                // Show distance of the path
                double distanceInMeters = path.getDouble("distance");
                double distanceInKm = distanceInMeters / 1000.0; // Convert meters to kilometers
                String distanceText = String.format("Distance: %.2f km", distanceInKm);

                // Display distance on the map or in a TextView, as needed
                // For example, displaying as a Toast message:
                Toast.makeText(mContext, "" + distanceText, Toast.LENGTH_SHORT).show();

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    private List<LatLng> decodePolyline(String encoded) {
//        List<LatLng> decoded = new ArrayList<>();
//        int index = 0;
//        int len = encoded.length();
//        int lat = 0, lng = 0;
//
//        while (index < len) {
//            int b, shift = 0, result = 0;
//            do {
//                b = encoded.charAt(index++) - 63;
//                result |= (b & 0x1F) << shift;
//                shift += 5;
//            } while (b >= 0x20);
//            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
//            lat += dlat;
//
//            shift = 0;
//            result = 0;
//            do {
//                b = encoded.charAt(index++) - 63;
//                result |= (b & 0x1F) << shift;
//                shift += 5;
//            } while (b >= 0x20);
//            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
//            lng += dlng;
//
//            double latitude = lat / 1e5;
//            double longitude = lng / 1e5;
//
//            decoded.add(new LatLng(latitude, longitude));
//        }
//
//        return decoded;
//    }

    public interface RoutingCallback {
        void onRoutingCompleted(String result);
    }
}
