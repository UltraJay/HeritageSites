package com.example.saheritagesites;

import com.example.saheritagesites.HeritageSite;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.location.Location;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.data.geojson.GeoJsonGeometryCollection;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.Geometry;

import com.google.maps.android.data.geojson.GeoJsonPoint;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONException;

import java.io.IOException;

public class MapsActivity extends FragmentActivity implements ClusterManager.OnClusterItemInfoWindowClickListener<HeritageSite>, OnMapReadyCallback {

    private GoogleMap mMap;

    private static final String TAG = MapsActivity.class.getSimpleName();

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private ClusterManager<HeritageSite> mClusterManager;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

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
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mClusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        //mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        // Set a listener for when an info window is clicked (will show a description panel)
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        //Prompt for permission to get location data.
        getLocationPermission();

        //Turn on My Location layer and button on map if permission is given.
        updateLocationUI();

        //Get the current location of the device and set the position of the map.
        getDeviceLocation();

        //Add GeoJSON data layer
        addGeoLayer();
    }

    public void addGeoLayer(){
        String siteID = null;
        String title = null;
        String snip = null;
        String extent = null;
        String significance = null;
        LatLng coord;
        try {
            GeoJsonLayer geolayer = new GeoJsonLayer(mMap, R.raw.heritagepoints, getApplicationContext());
            //geolayer.addLayerToMap();

            for (GeoJsonFeature feature : geolayer.getFeatures()) {
                if (feature.hasProperty("HERITAGENR")) {
                    siteID = feature.getProperty("HERITAGENR");
                }
                if (feature.hasProperty("DETAILS")) {
                    title = feature.getProperty("DETAILS");
                }
                if (feature.hasProperty("PARLOCATION")) {
                    snip = feature.getProperty("PARLOCATION");
                }
                if (feature.hasProperty("EXTENTOFLISTING")) {
                    extent = feature.getProperty("EXTENTOFLISTING");
                }
                if (feature.hasProperty("SIGNIFICANCE")) {
                    significance = feature.getProperty("SIGNIFICANCE");
                }
                if (feature.hasGeometry()) {
                    Geometry geo = feature.getGeometry();
                    coord = ((GeoJsonPoint) geo).getCoordinates();
                    HeritageSite site = new HeritageSite(siteID, coord, title, snip, extent, significance);
                    mClusterManager.addItem(site);
                }
            }
        } catch (IOException e) {
            Log.e("Exception: %s", "GeoJSON file could not be read");
        } catch (JSONException e) {
            Log.e("Exception: %s", "GeoJSON file could not be converted to a JSONObject");
        }

    }

    /*@Override
    public void onInfoWindowClick(Marker marker) {
        Log.i("Info:", "Clicked this info window: " + marker.getTitle());
        Intent intent = new Intent(MapsActivity.this, SiteActivity.class);
        startActivity(intent);
    }
    */
    @Override
    public void onClusterItemInfoWindowClick(HeritageSite heritageSite) {
        Log.i("Info:", "Clicked this info window: " + heritageSite.getTitle());
        Intent intent = new Intent(MapsActivity.this, SiteActivity.class);
        intent.putExtra("Heritage Site", heritageSite);
        startActivity(intent);
    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

}
