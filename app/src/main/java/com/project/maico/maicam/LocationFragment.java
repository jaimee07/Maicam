package com.project.maico.maicam;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class LocationFragment extends Fragment {
    private static final String LOG_TAG = LocationFragment.class.getSimpleName();

    private static TextView mLatitudeValue;
    private static TextView mLongitudeValue;
    private static TextView mLocationName;

    private static FloatingActionButton mFab;

    //Location listener
    private static final long LOCATION_MINTIME = 2000;
    private static final float LOCATION_MINDISTANCE = 10;

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

        //TextViews
        mLatitudeValue = (TextView) view.findViewById(R.id.latitudeValue);
        mLongitudeValue = (TextView) view.findViewById(R.id.longitudeValue);
        mLocationName = (TextView) view.findViewById(R.id.locationName);

        //Setup location manager
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location!=null){
            mLatitudeValue.setText(Utility.convertToDMS(location.getLatitude()));
            mLongitudeValue.setText(Utility.convertToDMS(location.getLongitude()));
            mLocationName.setText("google kung pano kunin location name given the coordinates");
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
    }

    @Override
    public void onDetach() {
        ImageButton imageButton = (ImageButton) getActivity().findViewById(R.id.locationButton);
        imageButton.setImageResource(R.drawable.ic_location);
        super.onDetach();
    }



}
