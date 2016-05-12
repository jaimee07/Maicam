package com.project.maico.maicam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CameraFragment extends Fragment {

    private static final String LOG_TAG = CameraFragment.class.getSimpleName();
    private static Camera mCamera;
    private static SurfaceHolder mHolder;
    private static CameraPreview mPreview;
    private static Canvas mCanvas;

    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;

    //Saving images
    private static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;

    //Orientation
    private static final int PORTRAIT_90 = 90;
    private static OrientationEventListener mOrientationEventListener;
    private static int mImageOrientation = 90;
    private static String orient = "portrait";

    private static RelativeLayout sensorLayout;

    //Date-time change listener
    private final DateChangeReceiver mDateChangeReceiver = new DateChangeReceiver();

    //Camera Overlay
    protected TextView mLatitudeLongitudeText;
    protected TextView mAltitudeText;
    protected TextView mDateText;
    protected TextView mTimeText;
    protected ImageView mQRCode;

    //flash setting
    private Boolean hasFlash;
    private Boolean flashToggle = false;

    //Location listener
    private static final long LOCATION_MINTIME = 2000;
    private static final float LOCATION_MINDISTANCE = 10;

    protected Location currentLocation;
    private static double longitude;
    private static double latitude;
    private static double altitude;

    private final LocationListener locationListener = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(getActivity(), "Location changed", Toast.LENGTH_SHORT).show();
            //update current location
            currentLocation = location;
            mLatitudeLongitudeText.setText(String.format("%f, %f", location.getLatitude(), location.getLongitude()));
            mAltitudeText.setText(String.format("Altitude: %f m", location.getAltitude()));

            //mLatitudeLongitudeText.setText(Utility.convertToDMS(location.getLatitude(), location.getLongitude()));

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

    public CameraFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        //Create camera
        mCamera = getCameraInstance();

        //Create preview view
        mPreview = new CameraPreview(getActivity(), mCamera);
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
        preview.addView(mPreview);


        //flash setting
        Button flashButton = (Button) view.findViewById(R.id.flashButton);
        hasFlash = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if(!hasFlash){
            flashButton.setClickable(false);
        }else{
            flashButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!flashToggle){
                        //turn on flash
                        if(mCamera == null) {
                            return;
                        }
                        Camera.Parameters param = mCamera.getParameters();
                        param.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        mCamera.setParameters(param);
                        mCamera.startPreview();
                        flashToggle = true;
                    }else{
                        //turn off flash
                        Camera.Parameters param = mCamera.getParameters();
                        param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(param);
                        mCamera.startPreview();
                        flashToggle = false;
                    }
                }
            });
        }

        //Camera Overlay TextViews
        mLatitudeLongitudeText = (TextView) view.findViewById((R.id.latitudeLongitudeText));
        mAltitudeText = (TextView) view.findViewById(R.id.altitudeText);
        mDateText= (TextView) view.findViewById(R.id.dateText);
        mTimeText = (TextView) view.findViewById(R.id.timeText);
        //QRCode image
        mQRCode = (ImageView) view.findViewById(R.id.qrcodeImage);

        //initialize Camera Overlay
        mQRCode.setImageBitmap(QRFragment.bitmapQRCode);
        mDateText.setText(String.format(new SimpleDateFormat("MM/dd/yyyy").format(new Date())));
        mTimeText.setText(String.format(new SimpleDateFormat("h:mm aa").format(new Date())));



        //Setup location manager
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location!=null){
            currentLocation = location;
            mLatitudeLongitudeText.setText(String.format("%f, %f", location.getLatitude(), location.getLongitude()));
            //mLatitudeLongitudeText.setText(Utility.convertToDMS(location.getLatitude(), location.getLongitude()));
            mAltitudeText.setText(String.format("Altitude: %.2f", location.getAltitude()));
        }else{
            Toast.makeText(getActivity(),"No location detected. Make sure location is enabled on the device.",Toast.LENGTH_LONG).show();
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_MINTIME, LOCATION_MINDISTANCE, locationListener);

        sensorLayout = (RelativeLayout) view.findViewById(R.id.sensor_data_layout);
        if(sensorLayout==null){
            Log.d("MaicamDebug", "Sensorlayout is null");
        }else{
            Log.d("MaicamDebug", "Sensorlayout: Not null");
        }


        sensorLayout.bringToFront();
        sensorLayout.setDrawingCacheEnabled(true);

        //add listener
        Button captureButton = (Button) view.findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //set image orientation
                Camera.Parameters param = mCamera.getParameters();
                param.setRotation(mImageOrientation);
                mCamera.setParameters(param);
                Log.d(LOG_TAG, "parameters are set");

                //build drawing cache
                sensorLayout.buildDrawingCache();

                //get image from camera

//                mCamera.autoFocus(new Camera.AutoFocusCallback(){
//
//                    @Override
//                    public void onAutoFocus(boolean success, Camera camera) {
//                        if(success){
//                            mCamera.takePicture(mShutterCallback, null, mPictureCallback);
//                        }
//                    }
//                });

                mCamera.takePicture(mShutterCallback, null, mPictureCallback);
            }
        });



        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final View cameraView = view;
        super.onViewCreated(view, savedInstanceState);

        //Detect orientation via Sensor
        mOrientationEventListener = new OrientationEventListener(getActivity(), SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                String lastOrient = orient;
                /*
                if(orientation >= 45 && orientation<135){
                    degrees = 180;
                    orient = "landscape";
                }else if(orientation >= 135 && orientation<225){
                    degrees = 270;
                    orient = "portrait";
                }else if(orientation >= 225 && orientation<315){
                    degrees = 0;
                    orient = "landscape";
                }*/
                //TODO: Adjust values for faster detection, +-range will do
                if(Math.round(orientation)==90){
                    mImageOrientation = 180;
                    orient = "landscape";
                }else if(Math.round(orientation)==180){
                    mImageOrientation = 270;
                    orient = "portrait";
                }else if(Math.round(orientation)==270){
                    mImageOrientation = 0;
                    orient = "landscape";
                }else if(Math.round(orientation)==0){
                    mImageOrientation = 90;
                    orient = "portrait";
                }

                if(lastOrient!=orient){
                    RelativeLayout relLayout = (RelativeLayout) cameraView.findViewById(R.id.sensor_data_layout);
                    if(relLayout==null) {
                        int w = relLayout.getWidth();
                        int h = relLayout.getHeight();


                        if (orient == "landscape") {
                            relLayout.setRotation(90.0f);
                            relLayout.setTranslationX((w - h) / 2);
                            relLayout.setTranslationY((h - w) / 2);
                        } else {
                            relLayout.setRotation(0f);
                            relLayout.setTranslationX(0f);
                            relLayout.setTranslationY(0f);
                        }


                        ViewGroup.LayoutParams lp = relLayout.getLayoutParams();
                        lp.height = w;
                        lp.width = h;
                        relLayout.requestLayout();
                    }

                }

            }};

        if(mOrientationEventListener.canDetectOrientation()){
            Log.d(LOG_TAG, "can detect orientation");
            mOrientationEventListener.enable();
        }else{
            Log.d(LOG_TAG,"cannot detect orientation");
            mOrientationEventListener.disable();
        }
    }


    /**
     * Access primary camera
     */
    public static Camera getCameraInstance(){
        Camera c = null;
        try{
            c = Camera.open();
            Log.d(LOG_TAG,"Camera is open.");
        }catch(Exception e){
            //camera is not available
            Log.d(LOG_TAG, "Camera is not available. " + e.getMessage());
        }
        return c;
    }

    /**
     * Create a preview class: SurfaceView - displays the live image data
     */
    public class CameraPreview extends SurfaceView
            implements SurfaceHolder.Callback{

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            //autofocus
            setFocusable(true);
            setFocusableInTouchMode(true);

            //install a SurfaceHolder.Callback so we get notified
            //when the underlying surface is created and destroyed
            mHolder = getHolder();
            mHolder.addCallback(this);
            //deprecated but required on Android versions prioir to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            //surface created, now tell the camera where to draw preview
            try {
                if(mCamera == null){
                    mCamera = getCameraInstance();
                }
                mCamera.setDisplayOrientation(PORTRAIT_90);
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }catch(IOException e){
                Log.d(LOG_TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //if preview can change or rotate, take care these events here
            if(mHolder.getSurface() == null){
                return;
            }
            //stop preview before making changes
            try{
                mCamera.stopPreview();
            }catch (Exception e){
                //tried to stop a non-existent preview
            }

            //set preview size and make any resize, rotate or
            //reformatting changes here



            //start preview with new settings
            try{
                //setCameraDisplayOrientation(getActivity(),0,mCamera);
                if (mCamera == null){
                    mCamera = getCameraInstance();
                }

                mCamera.setDisplayOrientation(PORTRAIT_90);
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

//                mCamera.autoFocus(null);
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error starting camera preview: "
                        + e.getMessage());
            }


        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            //empty, release camera preview in your activity
            releaseCamera();
        }



        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if(flashToggle) {
                Camera.Parameters param = mCamera.getParameters();
                param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(param);
                mCamera.startPreview();
                mCamera.autoFocus(null);

                Camera.Parameters param1 = mCamera.getParameters();
                param1.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                mCamera.setParameters(param1);
                mCamera.startPreview();

            }else{
                mCamera.autoFocus(null);
            }
            return false;
//            return super.onTouchEvent(event);
        }
    }



    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            //default sound
        }
    };


    /**
     * Setup listeners for Capture
     */

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            Bitmap bitmapRaw = BitmapFactory.decodeByteArray(data, 0, data.length, options);

            //overlay bitmap
            Bitmap bitmapLayout = Bitmap.createScaledBitmap(sensorLayout.getDrawingCache(), bitmapRaw.getWidth(), bitmapRaw.getHeight(), true);

            mCanvas = new Canvas(bitmapRaw);
            mCanvas.drawBitmap(bitmapLayout, 0, 0, null);

            bitmapLayout.recycle();

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(LOG_TAG, "Error creating media file");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                //drawing of image into file
                bitmapRaw.compress(DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, fos);
                fos.close();

                bitmapRaw.recycle();

                if(currentLocation !=null) {
                    //exif data: location not set
                    ExifInterface exif = new ExifInterface(pictureFile.getAbsolutePath());
                    String latLetter = (currentLocation.getLatitude() > 0) ? "N" : "S";
                    String lonLetter = (currentLocation.getLongitude() > 0) ? "E" : "W";
                    //set latitude
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
                            Utility.gpsExif(currentLocation.getLatitude()));
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latLetter);
                    //set longitude
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
                            Utility.gpsExif(currentLocation.getLongitude()));
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, lonLetter);
                    //set altitude
                    // TODO: 5/8/2016  change to altitude
                    exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, String.format("%f", currentLocation.getAltitude()));
                    //save changes in exif
                    exif.saveAttributes();
                }

            } catch (FileNotFoundException e) {
                Log.d(LOG_TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
            }
            Toast.makeText(getActivity(), "Picture Saved : " + pictureFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

            refreshCamera();

        }
    };

    @Override
    public void onPause(){
        super.onPause();

        //releaseMediaRecorder
        releaseCamera();

        //unregister receiver
        try {
            getActivity().unregisterReceiver(mDateChangeReceiver);
        }catch(IllegalArgumentException e){
            if(e.getMessage().contains("Receiver not registered")){
                //ignore this exception. This is a known bug and is exactly what is desired.
            }else{
                throw e;
            }
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        getActivity().registerReceiver(mDateChangeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

    }

    /**
     * release camera, set camera to null
     */
    private void releaseCamera(){
        if(mCamera!=null){
            mCamera.release();
            mCamera=null;
        }
    }

    /**
     * refresh camera after a shot
     */
    private void refreshCamera() {
        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
        }catch (Exception e) {}

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {}
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile(int type){
        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Maicam");
        //not working in samsung android 4.3
        //File mediaStorageDir = new File(getActivity().getExternalFilesDir(null).getPath());
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(),"MaicamImages");
        //images persist after app is uninstalled

        //create storage directory
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Log.d(LOG_TAG," failed to create directory");
                return null;
            }
        }

        Log.d(LOG_TAG, "External Storage Directory: " +  mediaStorageDir.getPath());

        //create file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if(type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        }else if(type == MEDIA_TYPE_VIDEO){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        }else{
            return null;
        }
        return mediaFile;
    }

    public class DateChangeReceiver extends BroadcastReceiver {
        private final String LOG_TAG = DateChangeReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            //To do when time changes by a minute
            String time = String.format(new SimpleDateFormat("h:mm aa").format(new Date()));

            mTimeText.setText(time);
            if(time.equals("12:00 AM")){
                mDateText.setText(String.format(new SimpleDateFormat("MM/dd/yyyy").format(new Date())));
            }

            //how to upload in my repo
            //2nd try
        }
    }


}
