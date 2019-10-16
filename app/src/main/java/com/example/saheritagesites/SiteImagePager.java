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

    private Context context;

    private JSONArray imageUrls;

    public SiteImagePager (Context context, JSONArray urls) {
        this.context = context;
        this.imageUrls = urls;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position){
        View view = LayoutInflater.from(context).inflate(R.layout.imagefragment, container, false);
        ImageView mImageView = view.findViewById(R.id.imagedisplay);
        //mImageView.setImageDrawable(context.getResources().getDrawable(getImage(position)));
        String url = null;
        try {
            url = imageUrls.getString(position);
        } catch (JSONException e) {
            Log.e("Exception: ", "Error reading JSON");
        }
        if (url != null){
            Picasso.with(context)
                    .load("http://ultra.australiasoutheast.cloudapp.azure.com/" + url)
                    .error(R.drawable.noimage)
                    .into(mImageView);
        }
        else {
            mImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.noimage));
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
        if (imageUrls != null) {
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

    public int getImage(int position){
        switch (position){
            case 0:
                return R.drawable.sample0;
            case 1:
                return R.drawable.sample1;
            case 2:
                return R.drawable.sample2;
            case 3:
                return R.drawable.sample3;
            default:
                return R.drawable.sample0;
        }
    }
}
