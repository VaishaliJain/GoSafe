package com.example.vaishali.gosafe;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Button searchLocationButton;
    private Button exitNavigation;
    private FloatingActionButton harrassment_toggle;
    private FloatingActionButton choice_toggle;
    private FloatingActionButton police_toggle;
    private FloatingActionButton theft_toggle;
    private FloatingActionButton accident_toggle;
    private FloatingActionButton light_toggle;
    private boolean choice_toggle_value = false;
    private Marker destinationMarker;
    private Map<String, List<Marker>> newspaperMarkers = new HashMap<>();
    private boolean[] showNewspaperMarkers = {false, false, false, false, false};
    LatLng currentLocation;
    private boolean isNavigate = false;
    private Polyline navigationRoute;
    private Integer THRESHOLD = 2;
    private DelayAutoCompleteTextView geo_autocomplete;
    private ImageView geo_autocomplete_clear;
    private MarkerOptions reviewMarker;
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
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
        }

        geo_autocomplete_clear = (ImageView) findViewById(R.id.geo_autocomplete_clear);

        geo_autocomplete = (DelayAutoCompleteTextView) findViewById(R.id.geo_autocomplete);
        geo_autocomplete.setThreshold(THRESHOLD);
        geo_autocomplete.setAdapter(new GeoAutoCompleteAdapter(this)); // 'this' is Activity instance

        geo_autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                GeoSearchResult result = (GeoSearchResult) adapterView.getItemAtPosition(position);
                geo_autocomplete.setText(result.getAddress());
            }
        });
        geo_autocomplete.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    geo_autocomplete_clear.setVisibility(View.VISIBLE);
                } else {
                    geo_autocomplete_clear.setVisibility(View.GONE);
                }
            }
        });

        geo_autocomplete_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                geo_autocomplete.setText("");
            }
        });

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
            searchLocationButton = (Button) findViewById(R.id.search_button);
            searchLocationButton.setOnClickListener(this);

            exitNavigation = (Button) findViewById(R.id.exitNavigation);
            exitNavigation.setOnClickListener(this);

            harrassment_toggle = (FloatingActionButton) findViewById(R.id.harrassment_toggle);
            harrassment_toggle.setOnClickListener(this);

            accident_toggle = (FloatingActionButton) findViewById(R.id.accident_toggle);
            accident_toggle.setOnClickListener(this);

            theft_toggle = (FloatingActionButton) findViewById(R.id.theft_toggle);
            theft_toggle.setOnClickListener(this);

            police_toggle = (FloatingActionButton) findViewById(R.id.police_toggle);
            police_toggle.setOnClickListener(this);

            light_toggle = (FloatingActionButton) findViewById(R.id.light_toggle);
            light_toggle.setOnClickListener(this);

            choice_toggle = (FloatingActionButton) findViewById(R.id.choice_toggle);
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
        if (view.equals(findViewById(R.id.search_button))) {
            LatLng origin = currentLocation;
            LatLng dest = null;
            try {
                TextView address = findViewById(R.id.geo_autocomplete);
                if(address.getText().toString() != "") {
                    dest = getCoordinatesFromAddress(address.getText().toString());
                    isNavigate = true;
                    navigation(origin, dest);
                    drawMarkerAtCurrentLocation(dest);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (view.equals(findViewById(R.id.exitNavigation))) {
            isNavigate = false;
            navigationRoute.remove();
            exitNavigation.setVisibility(View.GONE);
            destinationMarker.remove();
            geo_autocomplete.setText("");
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12));
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
                accident_toggle.setVisibility(View.VISIBLE);
                harrassment_toggle.setVisibility(View.VISIBLE);
                light_toggle.setVisibility(View.VISIBLE);
                theft_toggle.setVisibility(View.VISIBLE);
                police_toggle.setVisibility(View.VISIBLE);
            } else {
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

        reviewMarker = new MarkerOptions().position(latLng);
        String addressFromLatLng = getAddressFromLatLng(latLng);
        reviewMarker.title("Fill Review");

        reviewMarker.icon(BitmapDescriptorFactory.defaultMarker());
        mMap.addMarker(reviewMarker);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getTitle().equals(reviewMarker.getTitle())) {
            Intent intent = new Intent(MapsActivity.this, UserFormActivity.class);
            startActivity(intent);
            marker.remove();
            return true;
        }
        return false;
    }

    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this);

        String address = "";
        try {
            address = geocoder
                    .getFromLocation(latLng.latitude, latLng.longitude, 1)
                    .get(0).getAddressLine(0);
        } catch (IOException e) {
        }

        return address;
    }


    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                drawMarkerAtCurrentLocation(currentLocation);
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

    private void drawMarkerAtCurrentLocation(LatLng location) {
        if (isNavigate == false) {
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Current Position")
                    .draggable(true));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
        } else {
            destinationMarker = mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Current Position")
                    .draggable(true));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(currentLocation);
            builder.include(location);
            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
        }
    }

    private void getCurrentLocationAndMarkIt() {
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location location = null;
        if ((isGPSEnabled || isNetworkEnabled)) {
            if (isNetworkEnabled) {
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
            drawMarkerAtCurrentLocation(currentLocation);
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
                Marker marker = mMap.addMarker(new MarkerOptions().position(coordinates).title(issue + " markers").visible(false));
                switch (issue) {
                    case "accident":
                        accidentMarkers.add(marker);
                        marker.setIcon(getMarkerIcon("#ffaa66cc"));
                        break;
                    case "harrassment":
                        harrassmentMarkers.add(marker);
                        marker.setIcon(getMarkerIcon("#ff00ddff"));
                        break;
                    case "light":
                        lightMarkers.add(marker);
                        marker.setIcon(getMarkerIcon("#ffffbb33"));
                        break;
                    case "police":
                        policeMarkers.add(marker);
                        marker.setIcon(getMarkerIcon("#FF4560F2"));
                        break;
                    case "theft":
                        theftMarkers.add(marker);
                        marker.setIcon(getMarkerIcon("#ff669900"));
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

    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
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
        String url = getDirectionsUrl(origin, dest);
        DownloadTask downloadTask = new DownloadTask();
        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
        exitNavigation.setVisibility(View.VISIBLE);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
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

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

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
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.parseColor("#4a80f5"));
            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions.getPoints().size() >0 )
                navigationRoute = mMap.addPolyline(lineOptions);
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
}



