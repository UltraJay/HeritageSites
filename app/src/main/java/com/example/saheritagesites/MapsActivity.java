package com.example.saheritagesites;

import com.android.volley.DefaultRetryPolicy;
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
import com.cocoahero.android.geojson.Point;
import com.cocoahero.android.geojson.Position;
import com.androidmapsextensions.Polygon;
import com.androidmapsextensions.PolygonOptions;

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
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.squareup.picasso.Picasso;

public class MapsActivity extends FragmentActivity implements /*ClusterManager.OnClusterItemInfoWindowClickListener<HeritageSite>,*/ OnMapReadyCallback {

    private GoogleMap mMap;

    private static final String TAG = MapsActivity.class.getSimpleName();

    private FusedLocationProviderClient mFusedLocationProviderClient;

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

        //Set up the bottom sheet used to display an image along with site information
        bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //Tapping a bottom sheet takes you to a site description page
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

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //get~Extended~MapAsync() needed for "androidmapextensions" library to work
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
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        //Setup clustering engine
        ClusteringSettings clusteringSettings = new ClusteringSettings();
        clusteringSettings.addMarkersDynamically(true);
        clusteringSettings.minMarkersCount(5);
        googleMap.setClustering(clusteringSettings);

        //Disable Google Maps toolbar (bottom sheet covers it)
        mMap.getUiSettings().setMapToolbarEnabled(false);

        //Populate and show the bottom sheet when a marker is clicked
        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //Ignore and exit if marker is a cluster plus close the bottom sheet if it is opened
                //Same behavior as when a blank spot on the map is tapped
                if (marker.isCluster()){
                    if(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    }
                    return false;
                }

                //Clear all polygons from the map
                //If this isn't done then the map can get crowded with polygons
                //plus clicking a marker a second time will redraw a polygon on top of itself
                List<Polygon> polygons = mMap.getPolygons();
                Iterator<Polygon> iter = polygons.iterator();
                while (iter.hasNext()) {
                    Polygon p = iter.next();
                    iter.remove();
                    p.remove();
                }
                polygons.clear();

                final ImageView imageView = findViewById(R.id.sheetImage);
                HeritageSite site = marker.getData();

                //Debug logging to see which site a marker contains
                Log.i("Info", "Clicked marker for site " + site.getSiteID());

                //Setup asynchronous task queue for HTTP requests
                RequestQueue queue = Volley.newRequestQueue(MapsActivity.this);

                //URL for obtaining image URLs stored online pulled via State Heritage Registry code
                String url = "http://ultra.australiasoutheast.cloudapp.azure.com/pullImageJson.php?id=" + site.getShrcode();
                //Grabs a JSON object from the URL
                JsonObjectRequest jsonReq = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray imageUrls = response.getJSONArray("images");
                            //Grabs only the first image from the array of images and puts it into the bottom sheet ImageView
                            if (imageUrls.length() > 0) {
                                String firstimage = imageUrls.getString(0);
                                Picasso.with(MapsActivity.this)
                                        .load("http://ultra.australiasoutheast.cloudapp.azure.com/" + firstimage)
                                        .error(R.drawable.noimage)
                                        .into(imageView);
                            }
                            else {
                                //If no images are available show the app icon
                                imageView.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
                                imageView.setBackgroundColor(getResources().getColor(R.color.black));
                            }
                        } catch (JSONException e) {
                            Log.e("Exception: ", "Failed reading JSON array.");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Exception", "Volley failed to load site.");
                    }
                });
                //Add the JSON request to the request queue
                queue.add(jsonReq);

                //URL for pulling polygon coordinates from GeoJSON hosted online via Heritage site ID
                String polyurl = "http://ultra.australiasoutheast.cloudapp.azure.com/pullPoints.php?id=" + site.getSiteID();
                //Grabs a JSON object from the URL
                JsonObjectRequest polyReq = new JsonObjectRequest(polyurl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray polys = response.getJSONArray("points");
                            List<LatLng> latLngs = new ArrayList<LatLng>();
                            for (int b = 0; b < polys.length(); b++) {
                                //Grabs the array for each building
                                JSONArray building = polys.getJSONArray(b);
                                for (int p = 0; p < building.length(); p++) {
                                    //Grabs the arrays containing each coordinate in a building and then add those LatLngs to a list
                                    JSONArray points = building.getJSONArray(p);
                                    LatLng polypoint = new LatLng(points.getDouble(1), points.getDouble(0));
                                    latLngs.add(polypoint);
                                }
                                if (latLngs != null) {
                                    //If there are polygon points present, add the polygon to the map
                                    mMap.addPolygon(new PolygonOptions().addAll(latLngs).fillColor(getResources().getColor(R.color.colorPrimaryLightTrans)).strokeColor(getResources().getColor(R.color.colorPrimaryDark)));
                                }
                                latLngs.clear();
                            }

                        } catch (JSONException e) {
                            Log.e("Exception", "Failed reading JSON array.");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MapsActivity.this, "Failed to load site footprint. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });

                //Grabbing polygons is more costly so set a higher timeout period
                polyReq.setRetryPolicy(new DefaultRetryPolicy(
                        10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                //Add the polygon request to the queue
                queue.add(polyReq);

                //Prepare the text for the bottom sheet
                TextView address = findViewById(R.id.address);

                //Make the address bold and the name of the site normal all within the same text view by using a spannable string
                //Needed as two text views contain too much padding
                String boldText = site.getSnippet();
                String normalText = site.getTitle();
                SpannableString str = new SpannableString(boldText + "\n" + normalText);
                str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                address.setText(str);

                //Finally, open the fully populated bottom sheet
                if(mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }

                return false;
            }
        });

        //When a user taps an empty part of the map, markers are deselected
        //so close the bottom sheet if it is open
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng position) {
                if(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });

        //Prompt for permission to get location data.
        getLocationPermission();

        //Turn on My Location layer and button on map if permission is given.
        updateLocationUI();

        //Get the current location of the device and set the position of the map.
        getDeviceLocation();

        //Add GeoJSON heritage site markers
        addGeoLayer();
    }

    public void addGeoLayer() {
            //Input stream to read the JSON line by line
            InputStream stream = getResources().openRawResource(R.raw.heritagepoints);
            String title, snip, extent, significance, classdesc, accuracy,
                    devplan, sec23, sec16, planparcels, landcode, lga,
                    councilref, authdate, shrcode, shrdate;

        try {
            //Parse the GeoJSON into a GeoJSON object
            GeoJSONObject geoJSON = GeoJSON.parse(stream);
            //Typecast the overall GeoJSON object into a Feature Collection object
            FeatureCollection collection = (FeatureCollection)geoJSON;
            //Iterate through the Feature Collection for each Feature (Heritage Site)
            for (Feature feature: collection.getFeatures()) {
                //Get the properties object (site details)
                JSONObject properties = feature.getProperties();
                int siteID = properties.getInt("HERITAGENR");
                title = properties.optString("DETAILS");
                snip = properties.optString("PARLOCATION");
                //Optional parameters that are grabbed if they exist, but set their associated string to null if they don't exist
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
                //Grabs the coordinates from the geometry collection of the Feature
                //Requires typecasting and digging deep into the object structure to pull out the LatLng
                Point point = (Point)feature.getGeometry();
                Position pos = point.getPosition();
                LatLng coord = new LatLng(pos.getLatitude(),pos.getLongitude());
                //Creates a HeritageSite object associated with the Feature
                HeritageSite site = new HeritageSite(siteID, coord.latitude, coord.longitude, title, snip, extent, significance, classdesc, accuracy, devplan, sec23, sec16, planparcels, landcode, lga, councilref, authdate, shrcode, shrdate);
                //Adds a violet marker to the map from the coordinates and sets its info window to the HeritageSite's level of class
                Marker m = mMap.addMarker(options.title(site.getClassdesc() + " Heritage Site").position(coord).icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                //Attaches the HeritageSite object to the marker
                m.setData(site);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getDeviceLocation() {
        //Gets the current location of the device if location permission has been granted
        //Only checks permissions, will not request permission
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

    //If location permission has been granted, adds the MyLocation button to the map
    //Tries to ask for permission if it hasn't been granted
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
