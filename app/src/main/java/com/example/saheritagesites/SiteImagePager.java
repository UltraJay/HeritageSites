package com.example.saheritagesites;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import androidx.viewpager.widget.PagerAdapter;

import org.json.JSONArray;
import org.json.JSONException;

public class SiteImagePager extends PagerAdapter {

    //This class handles the image slideshow on the site description page

    private Context context;

    //A JSON array containing any available image URLs
    private JSONArray imageUrls;

    public SiteImagePager (Context context, JSONArray urls) {
        this.context = context;
        this.imageUrls = urls;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position){
        //This creates each page of the ViewPager
        //A Viewpager can contain an entire layout, but this one only includes a single ImageView
        View view = LayoutInflater.from(context).inflate(R.layout.imagefragment, container, false);
        ImageView mImageView = view.findViewById(R.id.imagedisplay);
        String url;
        try {
            if (imageUrls.length() > 0) {
                //Grab the image url for the current page and load it into the ImageView
                url = imageUrls.getString(position);
                Picasso.with(context)
                        .load("http://ultra.australiasoutheast.cloudapp.azure.com/" + url)
                        .error(R.drawable.noimage)
                        .into(mImageView);
            }
            else {
                // If there are no images available, set the image to Heritage SA logo
                mImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.heritagelogo));
            }
        } catch (JSONException e) {
            Log.e("Exception: ", "Error reading JSON.");
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View) view);
    }

    @Override
    public int getCount() {
        //This function is needed to create every page of the ViewPager
        //The number of pages is either the number of image URLs or just one - the logo
        if (imageUrls.length() > 0) {
            return imageUrls.length();
        }
        else {
            return 1;
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object == view;
    }

    public String getImage(int position){
        //Returns the url for the image the ViewPager is currently on
        //Called when zooming in on an image
        String url = null;
        try {
            url = imageUrls.getString(position);
        } catch (JSONException e) {
            Log.e("Exception: ", "Error reading JSON.");
        }
        return url;
    }
}
