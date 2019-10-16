package com.example.saheritagesites;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.GoogleMap.OnMarkerClickListener;
import com.androidmapsextensions.OnMapReadyCallback;

import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSONObject;
import com.cocoahero.android.geojson.Geometry;
import com.cocoahero.android.geojson.Point;
import com.cocoahero.android.geojson.Polygon;
import com.cocoahero.android.geojson.Position;
import com.example.saheritagesites.HeritageSite;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;

import android.location.Location;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
//import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonPoint;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;



import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.squareup.picasso.Picasso;

public class MapsActivity extends FragmentActivity implements /*ClusterManager.OnClusterItemInfoWindowClickListener<HeritageSite>,*/ OnMapReadyCallback {

    private GoogleMap mMap;
    GeoJsonLayer polyLayer;
    JSONObject polys;

    private static final String TAG = MapsActivity.class.getSimpleName();

    private FusedLocationProviderClient mFusedLocationProviderClient;

    //private ClusterManager<HeritageSite> mClusterManager;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    MarkerOptions options = new MarkerOptions();
    private BottomSheetBehavior mBottomSheetBehavior;
    View bottomSheet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        bottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Marker m = mMap.getMarkerShowingInfoWindow();
               if (m != null){
                   HeritageSite site = m.getData();
                   Intent intent = new Intent(MapsActivity.this, SiteActivity.class);
                   intent.putExtra("Heritage Site", site);
                   startActivity(intent);
               }
            }
        });

        //Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getExtendedMapAsync(this);
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

        ClusteringSettings clusteringSettings = new ClusteringSettings();
        clusteringSettings.addMarkersDynamically(true);
        clusteringSettings.minMarkersCount(5);
        googleMap.setClustering(clusteringSettings);

        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.isCluster()){
                    for(Marker m: marker.getMarkers()) {
                        float minzoom = mMap.getCameraPosition().zoom;
                        float zoom = mMap.getMinZoomLevelNotClustered(m);
                        if (zoom < minzoom){
                            minzoom = zoom;
                        }
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(minzoom));
                        if(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        }
                    }
                    return false;
                }
                final ImageView imageView = findViewById(R.id.sheetImage);
                HeritageSite site = marker.getData();
                RequestQueue queue = Volley.newRequestQueue(MapsActivity.this);
                String url = "http://ultra.australiasoutheast.cloudapp.azure.com/pullImageJson.php?id=" + site.getSiteID();
                JsonObjectRequest jsonReq = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray imageUrls = response.getJSONArray("images");
                            String url = imageUrls.getString(0);
                            Picasso.with(MapsActivity.this)
                                    .load("http://ultra.australiasoutheast.cloudapp.azure.com/" + url)
                                    .error(R.drawable.noimage)
                                    .into(imageView);
                        } catch (JSONException e) {
                            Log.e("Exception: ", "Failed reading JSON array.");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Exception", error.getMessage());
                    }
                });
                queue.add(jsonReq);

                TextView address = findViewById(R.id.address);

                String boldText = site.getSnippet();
                String normalText = site.getTitle();
                SpannableString str = new SpannableString(boldText + "\n" + normalText);
                str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                address.setText(str);
                if(mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }

                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng position) {
                if(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });

        //mClusterManager = new ClusterManager<>(this, mMap);
        //mMap.setOnCameraIdleListener(mClusterManager);
        //mMap.setOnInfoWindowClickListener(mClusterManager);

        //mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        // Set a listener for when an info window is clicked (will show a description panel)
        //mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        //Prompt for permission to get location data.
        getLocationPermission();

        //Turn on My Location layer and button on map if permission is given.
        updateLocationUI();

        //Get the current location of the device and set the position of the map.
        getDeviceLocation();

        //Add GeoJSON data layer
        addGeoLayer();
    }

    public void addGeoLayer() {
            //GeoJsonLayer geoLayer = new GeoJsonLayer(mMap, R.raw.heritagepoints, getApplicationContext());
            //geolayer.addLayerToMap();

            InputStream stream = getResources().openRawResource(R.raw.heritagepoints);
            String title, snip, extent=null, significance, classdesc, accuracy,
                    devplan, sec23, sec16, planparcels, landcode, lga,
                    councilref, authdate, shrcode, shrdate;

        try {
            GeoJSONObject geoJSON = GeoJSON.parse(stream);
            FeatureCollection collection = (FeatureCollection)geoJSON;
            for (Feature feature: collection.getFeatures()) {
                JSONObject properties = feature.getProperties();
                int siteID = properties.getInt("HERITAGENR");
                title = properties.optString("DETAILS");
                snip = properties.optString("PARLOCATION");
                if (!properties.isNull("EXTENTOFLISTING")) { extent = properties.optString("EXTENTOFLISTING"); } else { extent = null; }
                if (!properties.isNull("SIGNIFICANCE")) { significance = properties.optString("SIGNIFICANCE"); } else { significance = null; }
                if (!properties.isNull("HERITAGECLASS1DESC")) { classdesc = properties.optString("HERITAGECLASS1DESC"); } else { classdesc = null; }
                if (!properties.isNull("ACCURACYDESC")) { accuracy = properties.optString("ACCURACYDESC"); } else { accuracy = null; }
                if (!properties.isNull("DEVPLANDESC")) { devplan = properties.optString("DEVPLANDESC"); } else { devplan = null; }
                if (!properties.isNull("SECTION23S")) { sec23 = properties.optString("SECTION23S"); } else { sec23 = null; }
                if (!properties.isNull("SECTION16S")) { sec16 = properties.optString("SECTION16S"); } else { sec16 = null; }
                if (!properties.isNull("PLANPARCELS")) { planparcels = properties.optString("PLANPARCELS"); } else { planparcels = null; }
                if (!properties.isNull("AS2482DESC")) { landcode = properties.optString("AS2482DESC"); } else { landcode = null; }
                if (!properties.isNull("LGADESC")) { lga = properties.optString("LGADESC"); } else { lga = null; }
                if (!properties.isNull("COUNCILREF")) { councilref = properties.optString("COUNCILREF"); } else { councilref = null; }
                if (!properties.isNull("AUTHORISATIONDATE")) { authdate = properties.optString("AUTHORISATIONDATE"); } else { authdate = null; }
                if (!properties.isNull("SHRCODE")) { shrcode = properties.optString("SHRCODE"); } else { shrcode = null; }
                if (!properties.isNull("SHRSTATUSDATE")) { shrdate = properties.optString("SHRSTATUSDATE"); } else { shrdate = null; }
                Point point = (Point)feature.getGeometry();
                Position pos = point.getPosition();
                LatLng coord = new LatLng(pos.getLatitude(),pos.getLongitude());
                HeritageSite site = new HeritageSite(siteID, coord, title, snip, extent, significance, classdesc, accuracy, devplan, sec23, sec16, planparcels, landcode, lga, councilref, authdate, shrcode, shrdate);
                Marker m = mMap.addMarker(options.title(site.getClassdesc() + " Heritage Site").position(coord).icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                m.setData(site);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

            /*try {
            for (GeoJsonFeature feature : geoLayer.getFeatures()) {
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
                if (feature.hasProperty("HERITAGECLASS1DESC")) {
                    classdesc = feature.getProperty("HERITAGECLASS1DESC");
                }
                if (feature.hasProperty("ACCURACYDESC")) {
                    accuracy = feature.getProperty("ACCURACYDESC");
                }
                if (feature.hasProperty("DEVPLANDESC")) {
                    devplan = feature.getProperty("DEVPLANDESC");
                }
                if (feature.hasProperty("SECTION23S")) {
                    sec23 = feature.getProperty("SECTION23S");
                }
                if (feature.hasProperty("SECTION16S")) {
                    sec16 = feature.getProperty("SECTION16S");
                }
                if (feature.hasProperty("PLANPARCELS")) {
                    planparcels = feature.getProperty("PLANPARCELS");
                }
                if (feature.hasProperty("AS2482DESC")) {
                    landcode = feature.getProperty("AS2482DESC");
                }
                if (feature.hasProperty("LGADESC")) {
                    lga = feature.getProperty("LGADESC");
                }
                if (feature.hasProperty("COUNCILREF")) {
                    councilref = feature.getProperty("COUNCILREF");
                }
                if (feature.hasProperty("AUTHORISATIONDATE")) {
                    authdate = feature.getProperty("AUTHORISATIONDATE");
                }
                if (feature.hasProperty("SHRSTATUSCODE")) {
                    shrcode = feature.getProperty("SHRSTATUSCODE");
                }
                if (feature.hasProperty("SHRSTATUSDATE")) {
                    shrdate = feature.getProperty("SHRSTATUSDATE");
                }

                if (feature.hasGeometry()) {
                    Geometry geo = feature.getGeometry();
                    coord = ((GeoJsonPoint) geo).getCoordinates();
                    HeritageSite site = new HeritageSite(siteID, coord, title, snip, extent, significance, classdesc, accuracy, devplan, sec23, sec16, planparcels, landcode, lga, councilref, authdate, shrcode, shrdate);
                    //mClusterManager.addItem(site);
                }
            }
        } catch(IOException e){
            Log.e("Exception: %s", "GeoJSON file could not be read");
        } catch(JSONException e){
            Log.e("Exception: %s", "GeoJSON file could not be converted to a JSONObject");
        }*/
    }

    /*@Override
    public void onInfoWindowClick(Marker marker) {
        Log.i("Info:", "Clicked this info window: " + marker.getTitle());
        Intent intent = new Intent(MapsActivity.this, SiteActivity.class);
        startActivity(intent);
    }*/

    /*@Override
    public void onClusterItemInfoWindowClick(HeritageSite heritageSite) {
        Log.i("Info:", "Clicked this info window: " + heritageSite.getTitle());
        Intent intent = new Intent(MapsActivity.this, SiteActivity.class);
        intent.putExtra("Heritage Site", heritageSite);
        startActivity(intent);
    }*/

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
