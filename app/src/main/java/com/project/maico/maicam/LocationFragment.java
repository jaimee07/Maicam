package com.project.maico.maicam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.project.maico.maicam.MainUtil.Constants;
import com.project.maico.maicam.MainUtil.FetchAddressIntentService;

public class LocationFragment extends Fragment {
    private static final String LOG_TAG = LocationFragment.class.getSimpleName();

    private static TextView mLatitudeValue;
    private static TextView mLongitudeValue;
    private static TextView mLocationName;

    public Location mLastLocation;
    private AddressResultReceiver mResultReceiver;

    //Location listener
    private static final long LOCATION_MINTIME = 2000;
    private static final float LOCATION_MINDISTANCE = 10;

    private LocationListener locationListener;

    /*
    private final LocationListener locationListener = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(getActivity(), "Location changed", Toast.LENGTH_SHORT).show();
            mLatitudeValue.setText(Utility.convertToDMS(location.getLatitude()));
            mLongitudeValue.setText(Utility.convertToDMS(location.getLongitude()));
            mLocationName.setText("google kung pano kunin location name given the coordinates");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    */

    public LocationFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        //fetch location name
        mResultReceiver = new AddressResultReceiver(new Handler());

        //TextViews
        mLatitudeValue = (TextView) view.findViewById(R.id.latitudeValue);
        mLongitudeValue = (TextView) view.findViewById(R.id.longitudeValue);
        mLocationName = (TextView) view.findViewById(R.id.locationName);

        //Setup location manager
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLastLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(mLastLocation!=null){
            mLatitudeValue.setText(Utility.convertToDMS(mLastLocation.getLatitude()));
            mLongitudeValue.setText(Utility.convertToDMS(mLastLocation.getLongitude()));
            if (!Geocoder.isPresent()) {
                mLocationName.setText(R.string.no_geocoder_available);
            }else{
                startIntentService();
            }

        }else{
            Toast.makeText(getActivity(),"No location detected. Make sure location is enabled on the device.",Toast.LENGTH_LONG).show();
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_MINTIME, LOCATION_MINDISTANCE, locationListener);


        return view;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ImageButton imageButton = (ImageButton) getActivity().findViewById(R.id.locationButton);
        imageButton.setImageResource(R.drawable.ic_location_active);

        locationListener = new LocationListener(){

            @Override
            public void onLocationChanged(Location location) {
                mLastLocation = location;

                Toast.makeText(getActivity(), "Location changed", Toast.LENGTH_SHORT).show();
                mLatitudeValue.setText(Utility.convertToDMS(location.getLatitude()));
                mLongitudeValue.setText(Utility.convertToDMS(location.getLongitude()));
                //mLocationName.setText("google kung pano kunin location name given the coordinates");
                if (!Geocoder.isPresent()) {
                    mLocationName.setText(R.string.no_geocoder_available);
                }else{
                    startIntentService();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


    }

    @Override
    public void onDetach() {
        ImageButton imageButton = (ImageButton) getActivity().findViewById(R.id.locationButton);
        imageButton.setImageResource(R.drawable.ic_location);
        super.onDetach();
    }

    protected void startIntentService() {
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        getActivity().startService(intent);
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mLocationName.setText(resultData.getString(Constants.RESULT_DATA_KEY));

        }
    }




}
