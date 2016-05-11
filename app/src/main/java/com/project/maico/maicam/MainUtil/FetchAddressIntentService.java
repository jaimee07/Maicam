package com.project.maico.maicam.MainUtil;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.project.maico.maicam.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Clover on 5/10/2016.
 * Address lookup Service: reverse geocoding
 */
public class FetchAddressIntentService extends IntentService {
    private static final String LOG_TAG = FetchAddressIntentService.class.getSimpleName();
    protected ResultReceiver mReceiver;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchAddressIntentService(String name) {
        super(name);
    }

    public FetchAddressIntentService() {
        super(FetchAddressIntentService.class.getSimpleName());
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        // Check if receiver was properly registered.
        if (mReceiver == null) {
            Log.wtf(LOG_TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }

//        // Get the location passed to this service through an extra.
//        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
//
//        // Make sure that the location data was really sent over through an extra. If it wasn't,
//        // send an error error message and return.
//        if (location == null) {
//            errorMessage = getString(R.string.no_location_data_provided);
//            Log.wtf(TAG, errorMessage);
//            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
//            return;
//        }

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(
                Constants.LOCATION_DATA_EXTRA);

        //localized to user's geographic region
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        //get an address
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException e) {
            //e.printStackTrace();
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e(LOG_TAG, errorMessage, e);
        }catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(LOG_TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(LOG_TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        }else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(LOG_TAG, getString(R.string.address_found));
            deliverResultToReceiver(Constants.SUCCESS_RESULT, TextUtils.join(System.getProperty("line.separator"), addressFragments));
            //TextUtils.join(System.getProperty("line.separator"),addressFragments)

        }


    }
}
