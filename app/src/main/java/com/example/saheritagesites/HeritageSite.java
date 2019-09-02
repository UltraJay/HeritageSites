package com.example.saheritagesites;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class HeritageSite implements ClusterItem, Serializable{

    private final transient LatLng mPosition;

    private String mSiteID;
    private String mTitle;
    private String mSnippet;

    private String mExtent;
    private String mSignificance;

    public HeritageSite (LatLng coord){
        mPosition = coord;
        mTitle = null;
        mSnippet = null;
    }

    public HeritageSite(String siteID, LatLng coord, String title, String snippet, String extent, String significance) {
        mSiteID = siteID;
        mPosition = coord;
        mTitle = title;
        mSnippet = snippet;
        mExtent = extent;
        mSignificance = significance;
    }

    @Override
    public LatLng getPosition() { return mPosition; }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public String getExtent() {
        return mExtent;
    }

    public String getSignificance() {
        return mSignificance;
    }

    public String getSiteID() {
        return mSiteID;
    }
}
