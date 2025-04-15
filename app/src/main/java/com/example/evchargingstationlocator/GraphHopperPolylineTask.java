package com.example.evchargingstationlocator;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphHopperPolylineTask extends AsyncTask<Void, Void, List<PathFinder.LatLong>> {

    private final String apiKey = "be914761-9004-4ad2-8553-643db2fb596b";
    private String startPoint; // Starting point in latitude,longitude format
    private String endPoint; // Ending point in latitude,longitude format
    private String apiUrl; // URL for GraphHopper API

    private RoutingCallback callback;

    public GraphHopperPolylineTask(String vehicle, String startPoint, String endPoint, RoutingCallback callback) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.callback = callback;

        this.apiUrl = "https://graphhopper.com/api/1/route?point=" + startPoint + "&point=" + endPoint + "&snap_prevention=motorway&snap_prevention=ferry&snap_prevention=tunnel&details=road_class&details=surface&profile="+vehicle+"&locale=de&instructions=true&calc_points=true&points_encoded=false&key=" + apiKey;
    }

    @Override
    protected List<PathFinder.LatLong> doInBackground(Void... voids) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String result = response.body().string();
                return extractCoordinates(result);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<PathFinder.LatLong> coordinates) {
        if (callback != null && coordinates != null) {
            callback.onRoutingCompleted(coordinates);
        }
    }

    private List<PathFinder.LatLong> extractCoordinates(String result) throws JSONException {
        List<PathFinder.LatLong> coordinatesList = new ArrayList<>();

        JSONObject jsonResponse = new JSONObject(result);
        JSONArray paths = jsonResponse.getJSONArray("paths");

        if (paths.length() > 0) {
            JSONObject path = paths.getJSONObject(0);
            JSONObject pointsObject = path.getJSONObject("points");
            JSONArray coordinates = pointsObject.getJSONArray("coordinates");

            for (int i = 0; i < coordinates.length(); i++) {
                JSONArray coordinate = coordinates.getJSONArray(i);
                double longitude = coordinate.getDouble(0);
                double latitude = coordinate.getDouble(1);
                coordinatesList.add(new PathFinder.LatLong(latitude, longitude));
            }
        }

        return coordinatesList;
    }

    public interface RoutingCallback {
        void onRoutingCompleted(List<PathFinder.LatLong> coordinates);
    }
}