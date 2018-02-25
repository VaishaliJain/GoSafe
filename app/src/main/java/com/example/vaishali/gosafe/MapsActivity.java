package com.example.vaishali.gosafe;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener, View.OnClickListener {

    public static final int Police_Weight = 3;
    public static final int Light_Weight = 2;
    public static final int Accident_Weight = 2;
    public static final int Robbery_Weight = 1;
    public static final int Harassment_Weight = 3;


    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private FloatingActionButton harrassment_toggle;
    private FloatingActionButton police_toggle;
    private FloatingActionButton theft_toggle;
    private FloatingActionButton accident_toggle;
    private FloatingActionButton light_toggle;
    FloatingActionButton choice_toggle;
    private boolean choice_toggle_value = false;
    private Marker currentLocationMarker;
    private View placeAutocompleteClear;
    private View placeAutocompleteClearFrom;
    private Marker destinationMarker;
    private Map<String, List<Marker>> newspaperMarkers = new HashMap<>();
    private boolean[] showNewspaperMarkers = {false, false, false, false, false};
    LatLng currentLocation;
    LatLng destination;
    LatLng origin;
    private boolean isNavigate = false;
    private Polyline navigationRoute;
    private PlaceAutocompleteFragment autocompleteFragment;
    private PlaceAutocompleteFragment autocompleteFragmentFrom;
    private Polyline safeRoute;
    private Marker reviewMarker;
    private Marker originMarker;
    public static final int LOCATION_UPDATE_MIN_DISTANCE = 10;
    public static final int LOCATION_UPDATE_MIN_TIME = 5000;
    private static final int INITIAL_REQUEST = 1337;
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
        }

        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(placeSelectionListener);
        placeAutocompleteClear = autocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button);
        placeAutocompleteClear.setOnClickListener(this);

        autocompleteFragmentFrom = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_from);
        autocompleteFragmentFrom.setOnPlaceSelectedListener(fromPlaceSelectionListener);
        autocompleteFragmentFrom.getView().setVisibility(View.GONE);
        EditText fromPlace = autocompleteFragmentFrom.getView().findViewById(R.id.place_autocomplete_search_input);
        fromPlace.setHint("From Location");
        placeAutocompleteClearFrom = autocompleteFragmentFrom.getView().findViewById(R.id.place_autocomplete_clear_button);
        placeAutocompleteClearFrom.setOnClickListener(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getCurrentLocationAndMarkIt();

        try {
            harrassment_toggle = findViewById(R.id.harrassment_toggle);
            harrassment_toggle.setOnClickListener(this);

            accident_toggle = findViewById(R.id.accident_toggle);
            accident_toggle.setOnClickListener(this);

            theft_toggle = findViewById(R.id.theft_toggle);
            theft_toggle.setOnClickListener(this);

            police_toggle = findViewById(R.id.police_toggle);
            police_toggle.setOnClickListener(this);

            light_toggle = findViewById(R.id.light_toggle);
            light_toggle.setOnClickListener(this);

            Button navigate_button = findViewById(R.id.navigate_button);
            navigate_button.setVisibility(View.GONE);
            navigate_button.setOnClickListener(this);

            choice_toggle = findViewById(R.id.choice_toggle);
            choice_toggle.setOnClickListener(this);

            putCustomMarkers();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.equals(placeAutocompleteClear)) {
            autocompleteFragment.setText("");
            if (navigationRoute != null)
                navigationRoute.remove();
            navigationRoute = null;
            if (safeRoute != null)
                safeRoute.remove();
            safeRoute = null;
            if (destinationMarker != null)
                destinationMarker.remove();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
            view.setVisibility(View.GONE);
        } else if (view.equals(placeAutocompleteClearFrom)) {
            autocompleteFragmentFrom.setText("");
            if (navigationRoute != null)
                navigationRoute.remove();
            navigationRoute = null;
            if (safeRoute != null)
                safeRoute.remove();
            safeRoute = null;
            if (originMarker != null)
                originMarker.remove();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
            view.setVisibility(View.GONE);
        } else if (view.equals(findViewById(R.id.navigate_button))) {
            isNavigate = !isNavigate;
            if (isNavigate) {
                autocompleteFragmentFrom.getView().setVisibility(View.VISIBLE);
                Button navigation = (Button) findViewById(R.id.navigate_button);
                navigation.setText("Exit Navigation");
                EditText fromPlace = autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input);
                fromPlace.setHint("To Location");
                //view.setBackgroundColor(Color.BLUE);
            } else {
                autocompleteFragmentFrom.setText("");
                autocompleteFragmentFrom.getView().setVisibility(View.GONE);
                EditText fromPlace = autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input);
                autocompleteFragment.setText("");
                fromPlace.setHint("Search");
                //view.setBackgroundColor(Color.GREEN);
                if (navigationRoute != null)
                    navigationRoute.remove();
                navigationRoute = null;
                if (safeRoute != null)
                    safeRoute.remove();
                safeRoute = null;
                if (destinationMarker != null)
                    destinationMarker.remove();
                if (originMarker != null)
                    originMarker.remove();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
                view.setVisibility(View.GONE);
            }
        } else if (view.equals(findViewById(R.id.accident_toggle))) {
            showNewspaperMarkers[0] = !showNewspaperMarkers[0];
            toggleNewsMarkers(view);
        } else if (view.equals(findViewById(R.id.harrassment_toggle))) {
            showNewspaperMarkers[1] = !showNewspaperMarkers[1];
            toggleNewsMarkers(view);
        } else if (view.equals(findViewById(R.id.light_toggle))) {
            showNewspaperMarkers[2] = !showNewspaperMarkers[2];
            toggleNewsMarkers(view);
        } else if (view.equals(findViewById(R.id.police_toggle))) {
            showNewspaperMarkers[3] = !showNewspaperMarkers[3];
            toggleNewsMarkers(view);
        } else if (view.equals(findViewById(R.id.theft_toggle))) {
            showNewspaperMarkers[4] = !showNewspaperMarkers[4];
            toggleNewsMarkers(view);
        } else if (view.equals(findViewById(R.id.choice_toggle))) {
            choice_toggle_value = !choice_toggle_value;
            if (choice_toggle_value) {
                choice_toggle.setImageResource(R.drawable.hide_menu);
                accident_toggle.setVisibility(View.VISIBLE);
                harrassment_toggle.setVisibility(View.VISIBLE);
                light_toggle.setVisibility(View.VISIBLE);
                theft_toggle.setVisibility(View.VISIBLE);
                police_toggle.setVisibility(View.VISIBLE);
            } else {
                choice_toggle.setImageResource(R.drawable.show_menu);
                accident_toggle.setVisibility(View.GONE);
                harrassment_toggle.setVisibility(View.GONE);
                light_toggle.setVisibility(View.GONE);
                theft_toggle.setVisibility(View.GONE);
                police_toggle.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (reviewMarker != null) {
            reviewMarker.remove();
        }
        MarkerOptions reviewMarkerOptions = new MarkerOptions().position(latLng);
        reviewMarkerOptions.title(getAddressFromLatLng(latLng));
        reviewMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker());
        reviewMarker = mMap.addMarker(reviewMarkerOptions);
        Toast toast = Toast.makeText(getApplicationContext(), "Please click on the marker to submit a review.", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(reviewMarker)) {
            Intent intent = new Intent(MapsActivity.this, UserFormActivity.class);
            startActivity(intent);
            marker.remove();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        findViewById(R.id.navigate_button).setVisibility(View.GONE);
        if (isNavigate) {
            autocompleteFragmentFrom.getView().setVisibility(View.GONE);
            EditText fromPlace = autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input);
            fromPlace.setHint("Search");
            //view.setBackgroundColor(Color.GREEN);
            if (navigationRoute != null)
                navigationRoute.remove();
            navigationRoute = null;
            if (safeRoute != null)
                safeRoute.remove();
            safeRoute = null;
            if (destinationMarker != null)
                destinationMarker.remove();
            if (originMarker != null)
                originMarker.remove();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
            isNavigate = !isNavigate;
        }
        if (reviewMarker != null)
            reviewMarker.remove();
        autocompleteFragment.setText("");
        if (navigationRoute != null)
            navigationRoute.remove();
        navigationRoute = null;
        if (safeRoute != null)
            safeRoute.remove();
        safeRoute = null;
        if (destinationMarker != null)
            destinationMarker.remove();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
    }

    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this);

        String address = "";
        try {
            address = geocoder
                    .getFromLocation(latLng.latitude, latLng.longitude, 1)
                    .get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    private PlaceSelectionListener fromPlaceSelectionListener = new PlaceSelectionListener() {
        @Override
        public void onPlaceSelected(Place place) {
            origin = place.getLatLng();
            originMarker = mMap.addMarker(new MarkerOptions()
                    .position(origin)
                    .icon(getMarkerIcon("#DB7093"))
                    .title("Origin"));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(originMarker.getPosition());
            builder.include(destinationMarker.getPosition());
            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 7));

            if (isNavigate && (destination != null)) {
                navigation(origin, destination);
            }
        }

        @Override
        public void onError(Status status) {
            System.out.println("An error occurred: " + status);
        }
    };

    private PlaceSelectionListener placeSelectionListener = new PlaceSelectionListener() {
        @Override
        public void onPlaceSelected(Place place) {
            destination = place.getLatLng();
            if (isNavigate) {
                if (origin == null)
                    origin = currentLocation;
                navigation(origin, destination);
                drawMarkerAtDestination(destination);

            } else {
                findViewById(R.id.navigate_button).setVisibility(View.VISIBLE);
                Button navigate = findViewById(R.id.navigate_button);
                navigate.setText("Navigate");
                LatLng dest = place.getLatLng();
                drawMarkerAtDestination(dest);
            }
        }

        @Override
        public void onError(Status status) {
            System.out.println("An error occurred: " + status);
        }
    };

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                if (currentLocationMarker != null)
                    currentLocationMarker.setPosition(currentLocation);
                else
                    currentLocationMarker = mMap.addMarker(new MarkerOptions()
                            .position(currentLocation)
                            .title("Current Position")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.locationicon)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
                mLocationManager.removeUpdates(mLocationListener);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    private void drawMarkerAtDestination(LatLng location) {
        destinationMarker = mMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Destination")
                .icon(getMarkerIcon("#DB7093"))
                .draggable(true));
        if (!isNavigate) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
        } else {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(originMarker.getPosition());
            builder.include(location);
            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 7));
        }
    }

    private void getCurrentLocationAndMarkIt() {
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location location = null;
        if ((isGPSEnabled || isNetworkEnabled)) {
            if (isNetworkEnabled) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (isGPSEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        if (location != null) {
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            currentLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .title("Current Position")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.locationicon)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
        }
    }

    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    private void putCustomMarkers() throws IOException {
        Map<String, List<String>> issuesAndAddresses = getIssuesAndAddresses();
        List<Marker> accidentMarkers = new ArrayList<>();
        List<Marker> harrassmentMarkers = new ArrayList<>();
        List<Marker> lightMarkers = new ArrayList<>();
        List<Marker> policeMarkers = new ArrayList<>();
        List<Marker> theftMarkers = new ArrayList<>();
        for (String issue : issuesAndAddresses.keySet()) {
            for (String address : issuesAndAddresses.get(issue)) {
                LatLng coordinates = getCoordinatesFromAddress(address);
                System.out.println("Printing markers " + coordinates.latitude + " : " + coordinates.longitude);
                Marker marker = mMap.addMarker(new MarkerOptions().position(coordinates).visible(false));
                switch (issue) {
                    case "accident":
                        marker.setTitle("Accident");
                        marker.setIcon(getMarkerIcon("#ffaa66cc"));
                        accidentMarkers.add(marker);
                        break;
                    case "harrassment":
                        marker.setTitle("Harrassment");
                        marker.setIcon(getMarkerIcon("#FF4560F2"));
                        harrassmentMarkers.add(marker);
                        break;
                    case "light":
                        marker.setTitle("No street lights");
                        marker.setIcon(getMarkerIcon("#FFFF00"));
                        lightMarkers.add(marker);
                        break;
                    case "police":
                        marker.setTitle("Police present");
                        marker.setIcon(getMarkerIcon("#ff669900"));
                        policeMarkers.add(marker);
                        break;
                    case "theft":
                        marker.setTitle("Theft");
                        marker.setIcon(getMarkerIcon("#8B4513"));
                        theftMarkers.add(marker);
                        break;
                    default:
                        System.out.println("Invalid issue found");
                }
            }
        }
        newspaperMarkers.put("accident", accidentMarkers);
        newspaperMarkers.put("harrassment", harrassmentMarkers);
        newspaperMarkers.put("light", lightMarkers);
        newspaperMarkers.put("police", policeMarkers);
        newspaperMarkers.put("theft", theftMarkers);
    }

    private Map<String, List<String>> getIssuesAndAddresses() throws IOException {
        Map<String, List<String>> result = new HashMap<>();
        Map<Integer, String> resourceIdToIssue = new HashMap<>();
        resourceIdToIssue.put(R.raw.accident, "accident");
        resourceIdToIssue.put(R.raw.police, "police");
        resourceIdToIssue.put(R.raw.harrassment, "harrassment");
        resourceIdToIssue.put(R.raw.theft, "theft");
        resourceIdToIssue.put(R.raw.light, "light");
        Resources r = getResources();
        for (int id : resourceIdToIssue.keySet()) {
            InputStream in = r.openRawResource(id);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            List<String> addressList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                addressList.add(line);
            }
            result.put(resourceIdToIssue.get(id), addressList);
        }
        return result;
    }

    private LatLng getCoordinatesFromAddress(String address) throws IOException {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        addresses = geocoder.getFromLocationName(address, 1);
        if (addresses.size() > 0) {
            double latitude = addresses.get(0).getLatitude();
            double longitude = addresses.get(0).getLongitude();
            return new LatLng(latitude, longitude);
        }
        return new LatLng(0, 0);  // set it to some default value accordingly.
    }

    private void navigation(LatLng origin, LatLng dest) {
//        if (!safePoints.isEmpty())
//            safePoints = new ArrayList<>();
        String url = getDirectionsUrl(origin, dest);
        DownloadTask downloadTask = new DownloadTask();
        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&alternatives=true";
        String output = "json";
        return ("https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters);
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection(); // Creating an http connection to communicate with url
            urlConnection.connect();  // Connecting to url
            iStream = urlConnection.getInputStream();  // Reading data from url

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("While downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> safePoints = new ArrayList<>();
            PolylineOptions lineOptions = new PolylineOptions();
            PolylineOptions safeOptions = new PolylineOptions();
            HashMap<Integer, Integer> route_dangerLevel = new HashMap<>();
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                Log.d("Route: ", result.get(i).toString());
                ArrayList<LatLng> points = new ArrayList<>();
                // Fetching i-th route
                int danger_count = 0;
                int safety_count = 0;
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                    danger_count += isInsideCircle(position, 500);
                    safety_count += isOutsideCircle(position, 1000);

                }

                //Best Path returned by Google Map API
                if (i == 0) {
                    lineOptions.addAll(points);
                    lineOptions.width(20);
                    lineOptions.color(Color.parseColor("#4a80f5"));

                    // Drawing polyline in the Google Map for the 0th route
                    if (lineOptions.getPoints().size() > 0)
                        navigationRoute = mMap.addPolyline(lineOptions);
                }
                route_dangerLevel.put(i, safety_count - danger_count);
            }

            //Get the Safest Route
            Map<Integer, Integer> sortedMap = sortByValue(route_dangerLevel);
            Integer route_number = (Integer) sortedMap.keySet().toArray()[0];
            List<HashMap<String, String>> path_route = result.get(route_number);
            for (int j = 0; j < path_route.size(); j++) {
                HashMap<String, String> point = path_route.get(j);
                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                safePoints.add(position);
            }
            safeOptions.addAll(safePoints);
            safeOptions.width(10);
            safeOptions.color(Color.GREEN);
            if (safeOptions.getPoints().size() > 0 && safeRoute == null)
                safeRoute = mMap.addPolyline(safeOptions);
        }
    }

    private void toggleNewsMarkers(View view) {
        switch (view.getId()) {
            case R.id.accident_toggle: {
                if (showNewspaperMarkers[0])
                    for (Marker marker : newspaperMarkers.get("accident"))
                        marker.setVisible(true);
                else
                    for (Marker marker : newspaperMarkers.get("accident"))
                        marker.setVisible(false);
                break;
            }
            case R.id.harrassment_toggle: {
                if (showNewspaperMarkers[1])
                    for (Marker marker : newspaperMarkers.get("harrassment"))
                        marker.setVisible(true);
                else
                    for (Marker marker : newspaperMarkers.get("harrassment"))
                        marker.setVisible(false);
                break;
            }
            case R.id.light_toggle: {
                if (showNewspaperMarkers[2])
                    for (Marker marker : newspaperMarkers.get("light"))
                        marker.setVisible(true);
                else
                    for (Marker marker : newspaperMarkers.get("light"))
                        marker.setVisible(false);
                break;
            }
            case R.id.police_toggle: {
                if (showNewspaperMarkers[3])
                    for (Marker marker : newspaperMarkers.get("police"))
                        marker.setVisible(true);
                else
                    for (Marker marker : newspaperMarkers.get("police"))
                        marker.setVisible(false);
                break;
            }
            case R.id.theft_toggle: {
                if (showNewspaperMarkers[4])
                    for (Marker marker : newspaperMarkers.get("theft"))
                        marker.setVisible(true);
                else
                    for (Marker marker : newspaperMarkers.get("theft"))
                        marker.setVisible(false);
                break;
            }
            default:
                System.out.println("Incorrect id calls toggle markers");
        }
    }

    private int isInsideCircle(LatLng queryPoint, double radius) {
        int accidents = 0, theft = 0, harassment = 0, light = 0;

        for (int i = 0; i < newspaperMarkers.get("accident").size(); i++) {
            LatLng center = newspaperMarkers.get("accident").get(i).getPosition();
            float[] results = new float[1];
            Location.distanceBetween(center.latitude, center.longitude, queryPoint.latitude, queryPoint.longitude, results);
            if (results[0] <= radius)
                accidents++;
        }
        for (int i = 0; i < newspaperMarkers.get("theft").size(); i++) {
            LatLng center = newspaperMarkers.get("theft").get(i).getPosition();
            float[] results = new float[1];
            Location.distanceBetween(center.latitude, center.longitude, queryPoint.latitude, queryPoint.longitude, results);
            if (results[0] <= radius)
                theft++;
        }
        for (int i = 0; i < newspaperMarkers.get("harrassment").size(); i++) {
            LatLng center = newspaperMarkers.get("harrassment").get(i).getPosition();
            float[] results = new float[1];
            Location.distanceBetween(center.latitude, center.longitude, queryPoint.latitude, queryPoint.longitude, results);
            if (results[0] <= radius)
                harassment++;
        }
        for (int i = 0; i < newspaperMarkers.get("light").size(); i++) {
            LatLng center = newspaperMarkers.get("light").get(i).getPosition();
            float[] results = new float[1];
            Location.distanceBetween(center.latitude, center.longitude, queryPoint.latitude, queryPoint.longitude, results);
            if (results[0] < radius)
                light++;
        }
        return (accidents * Accident_Weight + theft * Robbery_Weight + harassment * Harassment_Weight + light*Light_Weight);
    }

    private int isOutsideCircle(LatLng queryPoint, double radius) {

        int police = 0;

        for (int i = 0; i < newspaperMarkers.get("police").size(); i++) {
            LatLng center = newspaperMarkers.get("police").get(i).getPosition();
            float[] results = new float[1];
            Location.distanceBetween(center.latitude, center.longitude, queryPoint.latitude, queryPoint.longitude, results);
            if (results[0] < radius)
                police++;
        }


        return (police * Police_Weight);
    }

    private static Map<Integer, Integer> sortByValue(Map<Integer, Integer> unsortMap) {

        List<Map.Entry<Integer, Integer>> list = new LinkedList<>(unsortMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1,
                               Map.Entry<Integer, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<Integer, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}