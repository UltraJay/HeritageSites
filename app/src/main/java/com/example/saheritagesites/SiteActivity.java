package com.example.saheritagesites;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.github.chrisbanes.photoview.PhotoView;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class SiteActivity extends AppCompatActivity {

    private SiteImagePager sitePager;
    HeritageSite site;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sitedescription);

        Intent intent = getIntent();
        site = (HeritageSite)intent.getSerializableExtra("Heritage Site");

        RequestQueue queue = Volley.newRequestQueue(this);
        //RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        String url = "http://ultra.australiasoutheast.cloudapp.azure.com/pullImageJson.php?id=" + site.getShrcode();
        /*JsonObjectRequest jsonReq = new JsonObjectRequest(url, new JSONObject(), requestFuture, requestFuture);
        try {
            JSONObject response = requestFuture.get(10, TimeUnit.SECONDS);
            JSONArray imageUrls = response.getJSONArray("images");
            site.setImageUrls(imageUrls);
        } catch (InterruptedException e) {
            Log.e("Exception: ", "Url request interrupted.", e);
            requestFuture.onErrorResponse(new VolleyError(e));
        } catch (ExecutionException e) {
            Log.e("Exception: ","URL request failed.", e);
            requestFuture.onErrorResponse(new VolleyError(e));
        } catch (TimeoutException e) {
            Log.e("Exception: ", "Url request timed out.", e);
            requestFuture.onErrorResponse(new VolleyError(e));
        } catch (JSONException e) {
            Log.e("Exception: ", "Failed reading JSON array", e);
            requestFuture.onErrorResponse(new VolleyError(e));
        }*/

        final ViewPager siteImages = findViewById(R.id.siteimages);


        JsonObjectRequest jsonReq = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray imageUrls = response.getJSONArray("images");
                    site.setImageUrls(imageUrls);
                    sitePager = new SiteImagePager(SiteActivity.this, imageUrls);
                    siteImages.setAdapter(sitePager);
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView siteDes = findViewById(R.id.siteDes);
        TextView txtDet = findViewById(R.id.txtDet);
        TextView txtAdd = findViewById(R.id.txtAdd);
        TextView siteExt = findViewById(R.id.siteExt);
        TextView txtExt = findViewById(R.id.txtExt);
        TextView siteSig = findViewById(R.id.siteSig);
        TextView txtSig = findViewById(R.id.txtSig);
        TextView siteClass = findViewById(R.id.siteClass);
        TextView txtClass = findViewById(R.id.txtClass);
        TextView siteAcc = findViewById(R.id.siteAcc);
        TextView txtAcc = findViewById(R.id.txtAcc);
        TextView siteDev = findViewById(R.id.siteDev);
        TextView txtDev = findViewById(R.id.txtDev);
        TextView siteSec23 = findViewById(R.id.siteSec23);
        TextView txtSec23 = findViewById(R.id.txtSec23);
        TextView siteSec16 = findViewById(R.id.siteSec16);
        TextView txtSec16 = findViewById(R.id.txtSec16);
        TextView sitePlan = findViewById(R.id.sitePlan);
        TextView txtPlan = findViewById(R.id.txtPlan);
        TextView siteLand = findViewById(R.id.siteLand);
        TextView txtLand = findViewById(R.id.txtLand);
        TextView siteLga = findViewById(R.id.siteLga);
        TextView txtLga = findViewById(R.id.txtLga);
        TextView siteCouncil = findViewById(R.id.siteCouncil);
        TextView txtCouncil = findViewById(R.id.txtCouncil);
        TextView siteAuth = findViewById(R.id.siteAuth);
        TextView txtAuth = findViewById(R.id.txtAuth);
        TextView siteShrCode = findViewById(R.id.siteShrCode);
        TextView txtShrCode = findViewById(R.id.txtShrCode);
        TextView siteShrDate = findViewById(R.id.siteShrDate);
        TextView txtShrDate = findViewById(R.id.txtShrDate);

        ImageButton btnImageLeft = findViewById(R.id.imageleft);
        btnImageLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                siteImages.setCurrentItem(siteImages.getCurrentItem() - 1);
            }
        });
        ImageButton btnImageRight = findViewById(R.id.imageright);
        btnImageRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                siteImages.setCurrentItem(siteImages.getCurrentItem() + 1);
            }
        });


        txtDet.setText(site.getTitle());
        txtAdd.setText(site.getSnippet());

        if (site.getExtent() != null) {
            txtExt.setText(site.getExtent());
            siteExt.setText("Extent of Listing");
            txtExt.setVisibility(View.VISIBLE);
            siteExt.setVisibility(View.VISIBLE);
        }
        if (site.getSignificance() != null) {
            txtSig.setText(site.getSignificance());
            siteSig.setText("Significance");
            txtSig.setVisibility(View.VISIBLE);
            siteSig.setVisibility(View.VISIBLE);
        }
        if (site.getClass() != null) {
            txtClass.setText(site.getClassdesc());
            siteClass.setText("Heritage Class");
            txtClass.setVisibility(View.VISIBLE);
            siteClass.setVisibility(View.VISIBLE);
        }
        if (site.getAccuracy() != null) {
            txtAcc.setText(site.getAccuracy());
            siteAcc.setText("Location Accuracy");
            txtAcc.setVisibility(View.VISIBLE);
            siteAcc.setVisibility(View.VISIBLE);
        }
        if (site.getDevplan() != null) {
            txtDev.setText(site.getDevplan());
            siteDev.setText("Development Plan");
            txtDev.setVisibility(View.VISIBLE);
            siteDev.setVisibility(View.VISIBLE);
        }
        if (site.getSec23() != null) {
            String[] s23details = site.getSec23().split(" ");
            //txtSec23.setText(site.getSec23());
            for (int i = 0; i < s23details.length; i++) {
                switch (s23details[i]){
                    case "a":{
                        txtSec23.append("(a) it displays historical, economic or social themes that are of importance to the local area");
                        break;
                    }
                    case "b":{
                        txtSec23.append("(b) it represents customs or ways of life that are characteristic of the local area");
                        break;
                    }
                    case "c":{
                        txtSec23.append("(c) it has played an important part in the lives of local residents");
                        break;
                    }
                    case "d":{
                        txtSec23.append("(d) it displays aesthetic merit, design characteristics or construction techniques of significance to the local area");
                        break;
                    }
                    case "e":{
                        txtSec23.append("(e) it is associated with a notable local personality or event");
                        break;
                    }
                    case "f":{
                        txtSec23.append("(f) it is a notable landmark in the area");
                        break;
                    }
                    case "g":{
                        txtSec23.append("(g) in the case of a tree... it is of special historical or social significance or importance within the local area");
                        break;
                    }
                }
                if(i != s23details.length - 1){
                    txtSec23.append("\n");
                }
            }
            siteSec23.setText("Section 23 Information");
            txtSec23.setVisibility(View.VISIBLE);
            siteSec23.setVisibility(View.VISIBLE);
        }
        if (site.getSec16() != null) {
            String[] s16details = site.getSec16().split(" ");
            //txtSec16.setText(site.getSec16());
            for (int i = 0; i < s16details.length; i++) {
                switch (s16details[i]){
                    case "A":{
                        txtSec16.append("(a) it demonstrates important aspects of the evolution or pattern of the State's history");
                        break;
                    }
                    case "B":{
                        txtSec16.append("(b) it has rare, uncommon or endangered qualities that are of cultural significance");
                        break;
                    }
                    case "C":{
                        txtSec16.append("(c) it may yield information that will contribute to an understanding of the State's history, including its natural history");
                        break;
                    }
                    case "D":{
                        txtSec16.append("(d) it is an outstanding representative of a particular class of places of cultural significance");
                        break;
                    }
                    case "E":{
                        txtSec16.append("(e) it demonstrates a high degree of creative, aesthetic or technical accomplishment or is an outstanding representative of particular construction techniques or design characteristics");
                        break;
                    }
                    case "F":{
                        txtSec16.append("(f) it has strong cultural or spiritual associations for the community or a group within it");
                        break;
                    }
                    case "G":{
                        txtSec16.append("(g) it has a special association with the life or work of a person or organisation or an event of historical importance");
                        break;
                    }
                }
                if(i != s16details.length - 1) {
                    txtSec16.append("\n");
                }
            }
            siteSec16.setText("Section 16 Information");
            txtSec16.setVisibility(View.VISIBLE);
            siteSec16.setVisibility(View.VISIBLE);
        }
        if (site.getPlanparcels() != null) {
            txtPlan.setText(site.getPlanparcels());
            sitePlan.setText("Plan Parcel and Title Information");
            txtPlan.setVisibility(View.VISIBLE);
            sitePlan.setVisibility(View.VISIBLE);
        }
        if (site.getLandcode() != null) {
            txtLand.setText(site.getLandcode());
            siteLand.setText("ASL Classification Code");
            txtLand.setVisibility(View.VISIBLE);
            siteLand.setVisibility(View.VISIBLE);
        }
        if (site.getLga() != null) {
            txtLga.setText(site.getLga());
            siteLga.setText("Local Government Area (LGA)");
            txtLga.setVisibility(View.VISIBLE);
            siteLga.setVisibility(View.VISIBLE);
        }
        if (site.getCouncilref() != null) {
            txtCouncil.setText(site.getCouncilref());
            siteCouncil.setText("Council Reference");
            txtCouncil.setVisibility(View.VISIBLE);
            siteCouncil.setVisibility(View.VISIBLE);
        }
        if (site.getAuthdate() != null) {
            txtAuth.setText(site.getAuthdate());
            siteAuth.setText("Authorization Date");
            txtAuth.setVisibility(View.VISIBLE);
            siteAuth.setVisibility(View.VISIBLE);
        }
        if (site.getShrcode() != null) {
            txtShrCode.setText(site.getShrcode());
            siteShrCode.setText("State Heritage Registry Code");
            txtShrCode.setVisibility(View.VISIBLE);
            siteShrCode.setVisibility(View.VISIBLE);
        }
        if (site.getShrdate() != null) {
            txtShrDate.setText(site.getShrdate());
            siteShrDate.setText("State Heritage Registry Date");
            txtShrDate.setVisibility(View.VISIBLE);
            siteShrDate.setVisibility(View.VISIBLE);
        }

        ImageButton imagefull = findViewById(R.id.imagefull);
        imagefull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(SiteActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.photozoom, null);
                PhotoView photoView = mView.findViewById(R.id.imageZoom);
                photoView.setImageResource(sitePager.getImage(siteImages.getCurrentItem()));
                mBuilder.setView(mView);
                AlertDialog mDialog = mBuilder.create();
                mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.optionmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            case R.id.app_bar_search:
                return true;
            case R.id.save_item:
                return true;
            case R.id.share_item:
                return true;
            case R.id.other_item:
                int siteid = site.getSiteID();
                Intent open_url = new Intent(Intent.ACTION_VIEW);
                open_url.setData(Uri.parse("http://maps.sa.gov.au/heritagesearch/HeritageItem.aspx?p_heritageno=" + siteid));
                startActivity(open_url);
            case R.id.report_item:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
