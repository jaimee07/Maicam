package com.project.maico.maicam;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.project.maico.maicam.QRCode.Contents;
import com.project.maico.maicam.QRCode.QRCodeEncoder;

public class QRFragment extends Fragment {
    private static final String LOG_TAG = QRFragment.class.getSimpleName();
    private static Activity myContext;
    public static Bitmap bitmapQRCode;

    //Location
    public Location mLastLocation;
    private static final long LOCATION_MINTIME = 2000;
    private static final float LOCATION_MINDISTANCE = 10;

    private final LocationListener locationListener = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
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


    public QRFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_qr, container, false);

        //button
        Button button1 = (Button) view.findViewById(R.id.button1);

        //Setup location manager
        LocationManager lm = (LocationManager) myContext.getSystemService(Context.LOCATION_SERVICE);
        mLastLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(mLastLocation==null){
            Toast.makeText(myContext, "No location detected. Make sure location is enabled on the device.", Toast.LENGTH_LONG).show();
//            button1.setClickable(false);
//            button1.setEnabled(false);
        }
        else{
            button1.setClickable(true);
            button1.setEnabled(true);
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_MINTIME, LOCATION_MINDISTANCE, locationListener);

        //add listener
        button1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
//                Float latitudeFloat = (float) mLastLocation.getLatitude();
//                Float longitudeFloat =  (float) mLastLocation.getLongitude();

                Float latitudeFloat = (float) 1.0;
                Float longitudeFloat =  (float) 2.0;

                //Find screen size
                WindowManager manager = ((WindowManager)myContext.getSystemService(Context.WINDOW_SERVICE));
                Display display = manager.getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int width = point.x;
                int height = point.y;
                int smallerDimension = width < height ? width : height;
                smallerDimension = smallerDimension * 3/4;

                Bundle bundle = new Bundle();
                bundle.putFloat("LAT", latitudeFloat);
                bundle.putFloat("LONG", longitudeFloat);


                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder("Maicam",
                        bundle,
                        Contents.Type.LOCATION,
                        BarcodeFormat.QR_CODE.toString(),
                        smallerDimension);

                // A geographic location. Use as follows:
                // Bundle bundle = new Bundle();
                // bundle.putFloat("LAT", latitude);
                // bundle.putFloat("LONG", longitude);
                // intent.putExtra(Intents.Encode.DATA, bundle);

                //public static final String LOCATION = "LOCATION_TYPE";



                try {
                    bitmapQRCode = qrCodeEncoder.encodeAsBitmap();
                    //bitmapQRCode = BitmapFactory.decodeResource(getResources(), R.drawable.ic_qr);
                    ImageView myImage = (ImageView) view.findViewById(R.id.imageView1);
                    myImage.setImageBitmap(bitmapQRCode);

                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myContext = activity;
        ImageButton imageButton = (ImageButton) myContext.findViewById(R.id.qrButton);
        imageButton.setImageResource(R.drawable.ic_qr_active);
    }

    @Override
    public void onDetach() {
        ImageButton imageButton = (ImageButton) myContext.findViewById(R.id.qrButton);
        imageButton.setImageResource(R.drawable.ic_qr);
        super.onDetach();
    }

}
