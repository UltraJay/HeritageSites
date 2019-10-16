package com.example.saheritagesites;

import java.io.Serializable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.json.JSONArray;

public class HeritageSite implements ClusterItem, Serializable{

    private final transient LatLng mPosition;

    private JSONArray imageUrls;

    private int mSiteID;
    private String mTitle;
    private String mSnippet;

    private String mExtent;
    private String mSignificance;
    private String mClassdesc;
    private String mAccuracy;
    private String mDevplan;
    private String mSec23;
    private String mSec16;
    private String mPlanparcels;
    private String mLandcode;
    private String mLga;
    private String mCouncilref;
    private String mAuthdate;
    private String mShrcode;
    private String mShrdate;

    public HeritageSite (LatLng coord){
        mPosition = coord;
        mTitle = null;
        mSnippet = null;
    }

    public HeritageSite(int siteID, LatLng coord, String title, String snippet, String extent, String significance, String classdesc, String accuracy, String devplan,
                        String sec23, String sec16, String planparcels, String landcode, String lga, String councilref, String authdate, String shrcode, String shrdate) {
        mSiteID = siteID;
        mPosition = coord;
        mTitle = title;
        mSnippet = snippet;
        mExtent = extent;
        mSignificance = significance;
        mClassdesc = classdesc;
        mAccuracy = accuracy;
        mDevplan = devplan;
        mSec23 = sec23;
        mSec16 = sec16;
        mPlanparcels = planparcels;
        mLandcode = landcode;
        mLga = lga;
        mCouncilref = councilref;
        mAuthdate = authdate;
        mShrcode = shrcode;
        mShrdate = shrdate;
    }

    public void setImageUrls (JSONArray urls){
        imageUrls = urls;
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

    public JSONArray getImageUrls() {
        return imageUrls;
    }

    public String getExtent() {
        return mExtent;
    }

    public String getSignificance() { return mSignificance; }

    public int getSiteID() { return mSiteID; }

    public String getClassdesc() { return mClassdesc; }

    public String getAccuracy() { return mAccuracy; }

    public String getDevplan() {
        return mDevplan;
    }

    public String getSec23() {
        return mSec23;
    }

    public String getSec16() {
        return mSec16;
    }

    public String getPlanparcels() {
        return mPlanparcels;
    }

    public String getLandcode() {
        return mLandcode;
    }

    public String getLga() {
        return mLga;
    }

    public String getCouncilref() {
        return mCouncilref;
    }

    public String getAuthdate() {
        return mAuthdate;
    }

    public String getShrcode() {
        return mShrcode;
    }

    public String getShrdate() { return mShrdate; }

}
