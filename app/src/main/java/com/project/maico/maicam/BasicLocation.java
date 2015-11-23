package com.project.maico.maicam;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Clover on 11/22/2015.
 * Revised BasicLocationSample from Android Developer Page
 *
 * Use Location API to retrieve the last known location for a device.
 *
 */
public class BasicLocation implements ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = BasicLocation.class.getSimpleName();

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location
     */
    protected Location mLastLocation;

    private static Context mContext;

    public BasicLocation(Context context){
        mContext = context;

        buildGoogleApiClient();
        onStart();
    }

    /**
     * Builds a GoogleApiClient.
     * Uses the addApi() to request the LocationServices API
     */
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Runs when a GoogleApiClient object sucessfully connects
     * @param connectionHint
     */

    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d(LOG_TAG,"mLastLocation has been initialized." );

    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(LOG_TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }


    public void onStart(){
        mGoogleApiClient.connect();
    }

    public void onStop(){
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

}
