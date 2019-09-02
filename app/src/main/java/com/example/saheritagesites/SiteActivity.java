package com.example.saheritagesites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.content.Intent;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;



public class SiteActivity extends AppCompatActivity {

    private SiteImagePager sitePager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sitedescription);

        Intent intent = getIntent();
        HeritageSite site = (HeritageSite)intent.getSerializableExtra("Heritage Site");

        sitePager = new SiteImagePager(this);
        final ViewPager siteImages = findViewById(R.id.siteimages);
        siteImages.setAdapter(sitePager);

        TextView siteDes = findViewById(R.id.siteDes);
        TextView txtDet = findViewById(R.id.txtDet);
        TextView txtAdd = findViewById(R.id.txtAdd);
        TextView siteExt = findViewById(R.id.siteExt);
        TextView txtExt = findViewById(R.id.txtExt);
        TextView siteSig = findViewById(R.id.siteSig);
        TextView txtSig = findViewById(R.id.txtSig);

        ImageButton btnBack = findViewById(R.id.btnBack);
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

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
