package com.example.vaishali.gosafe;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.SparseArray;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng coordinates = new LatLng(-34, 151);

        mMap.addMarker(new MarkerOptions().position(coordinates).title("Markers"));
        try {
            putCustomMarkers();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates));
    }

    private void putCustomMarkers() throws IOException {
        Map<String, List<String>> issuesAndAddresses = getIssuesAndAddresses();
        for (String issue : issuesAndAddresses.keySet()) {
            for (String address : issuesAndAddresses.get(issue)) {

                mMap.addMarker(new MarkerOptions().position(getCoordinatesFromAddress(address)).title(issue + " markers")
                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(issue, 100, 100))));
            }
        }
    }

    private Map<String, List<String>> getIssuesAndAddresses() throws IOException {
        Map<String, List<String>> result = new HashMap<>();
        Map<Integer, String> resourceIdToIssue = new HashMap<>();
        resourceIdToIssue.put(R.raw.accident, "accident");
        resourceIdToIssue.put(R.raw.police, "police");
        resourceIdToIssue.put(R.raw.harrasment, "harrasment");
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
            System.out.println("lat lang" + latitude + " " + longitude);
            return new LatLng(latitude, longitude);
        }
        return new LatLng(0, 0);  // set it to some default value accordingly.
    }
}
