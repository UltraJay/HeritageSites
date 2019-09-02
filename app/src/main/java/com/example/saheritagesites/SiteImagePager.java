package com.example.saheritagesites;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

public class SiteImagePager extends PagerAdapter {

    private Context context;

    public SiteImagePager (Context context) {
        this.context = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position){
        View view = LayoutInflater.from(context).inflate(R.layout.imagefragment, container, false);
        ImageView mImageView = view.findViewById(R.id.imagedisplay);
        mImageView.setImageDrawable(context.getResources().getDrawable(getImage(position)));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View) view);
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object == view;
    }

    private int getImage(int position){
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
