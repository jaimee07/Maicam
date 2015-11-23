package com.project.maico.maicam;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AccountFragment extends Fragment {

    private static final String LOG_TAG = AccountFragment.class.getSimpleName();
    private static Camera mCamera;
    private static SurfaceHolder mHolder;
    private static CameraPreview mPreview;

    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;

    private static final int PORTRAIT_90 = 90;

    private static OrientationEventListener mOrientationEventListener;
    private static int mImageOrientation = 90;
    private static String orient = "portrait";

    private static BasicLocation basicLocation;

    private static RelativeLayout sensorLayout;
    protected TextView mLatitudeLongitudeText;
    protected TextView mAltitudeText;
    protected TextView mDateText;
    protected TextView mTimeText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        //Create camera
        mCamera = getCameraInstance();

        //Create preview view
        mPreview = new CameraPreview(getActivity(), mCamera);

        //Location overlay
        basicLocation = new BasicLocation(getActivity());

        mLatitudeLongitudeText = (TextView) view.findViewById(R.id.latitudeLongitudeText);
        mAltitudeText = (TextView) view.findViewById(R.id.altitudeText);
        mDateText= (TextView) view.findViewById(R.id.dateText);
        mTimeText= (TextView) view.findViewById(R.id.timeText);

        if(basicLocation.mLastLocation!=null) {
            mLatitudeLongitudeText.setText(String.format("%f, %f",
                    basicLocation.mLastLocation.getLatitude(),basicLocation.mLastLocation.getLongitude()));
            mAltitudeText.setText(String.format("%s, %f", "Altitude:", basicLocation.mLastLocation.getAltitude()));
            mTimeText.setText(String.format("%s", new SimpleDateFormat("hh:mm aa").format(new Date())));
            mDateText.setText(String.format("%s", new SimpleDateFormat("MM/dd/yyyy").format(new Date())));
        }else{
            Toast.makeText(getActivity(),"No location set", Toast.LENGTH_SHORT).show();
        }
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        sensorLayout = (RelativeLayout) view.findViewById(R.id.sensor_data_layout);
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
                mCamera.takePicture(null, null, mPicture);
            }
        });



        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**
         * Detect orientation via Sensor
         */

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
                    RelativeLayout relLayout = (RelativeLayout) getView().findViewById(R.id.sensor_data_layout);
                    int w = relLayout.getWidth();
                    int h = relLayout.getHeight();


                    if(orient=="landscape"){
                        relLayout.setRotation(90.0f);
                        relLayout.setTranslationX((w-h) / 2);
                        relLayout.setTranslationY((h-w) / 2);
                    }else{
                        relLayout.setRotation(0f);
                        relLayout.setTranslationX(0f);
                        relLayout.setTranslationY(0f);
                    }


                    ViewGroup.LayoutParams lp = relLayout.getLayoutParams();
                    lp.height = w;
                    lp.width = h;
                    relLayout.requestLayout();

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

    @Override
    public void onStart() {
        super.onStart();
        basicLocation.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        basicLocation.onStop();
    }

    public static void refreshLocation(){

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
                if(mCamera == null){
                    mCamera = getCameraInstance();
                }

                mCamera.setDisplayOrientation(PORTRAIT_90);
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
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
    }


    /**
     * Build a preview layout in xml
     */

    /**
     * Setup listeners for Capture
     */
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            //Create bitmap from byte[] data
            Bitmap bitmapRaw = BitmapFactory.decodeByteArray(data, 0, data.length);
            //Bitmap bitmapLayout = Bitmap.createBitmap(preview.getDrawingCache(),0,0, bitmapRaw.getWidth(), bitmapRaw.getHeight());
            //preview.destroyDrawingcache

            //bitmapLayout scaled
            Bitmap bitmapLayout = Bitmap.createScaledBitmap(sensorLayout.getDrawingCache(), bitmapRaw.getWidth(), bitmapRaw.getHeight(), true);

            //Bitmap bitmap = addImageInfo(rawBitmap);
            Bitmap bitmap =  Bitmap.createBitmap(bitmapRaw.getWidth(), bitmapRaw.getHeight(),
                    bitmapRaw.getConfig());
            Canvas mCanvas = new Canvas(bitmap);
            mCanvas.drawBitmap(bitmapRaw,0,0,new Paint());
            mCanvas.drawBitmap(bitmapLayout,0,0,new Paint());

            File pictureFile =  getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if(pictureFile == null){
                Log.d(LOG_TAG, "Error creating media file");
                return;
            }
            try{
                FileOutputStream fos = new FileOutputStream(pictureFile);
                //drawing of image into file
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(LOG_TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
            }
            Toast.makeText(getActivity(), "Picture Saved", Toast.LENGTH_LONG).show();
            refreshCamera();
        }

    };

    public Bitmap addImageInfo(Bitmap mBitmap){
        Bitmap result = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(),
                mBitmap.getConfig());
        Canvas mCanvas = new Canvas(result);
        mCanvas.drawBitmap(mBitmap, 0, 0, null);

        //String latitudeLongitude = R.string.latitudeLongitude;
        String latitudeLongitude = "14*32\'6\"N, 121*2\'25\"E";
        String altitude = "Altitude: 262ft";
        String timeOverlay = "3:56pm";
        String dateOverlay = "11/29/2015";

        //copypaste
        Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(50);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setShadowLayer(10f, 10f, 10f, Color.BLACK);
        //set position
        paintText.setTextAlign(Paint.Align.CENTER);
        Rect rectText = new Rect();
        paintText.getTextBounds(latitudeLongitude, 0, latitudeLongitude.length(), rectText);
        //end copypaste

        mCanvas.drawText(latitudeLongitude, mCanvas.getWidth() / 2, rectText.height(), paintText);
        Log.d(LOG_TAG, "add info done");
        return result;
    }








    @Override
    public void onPause(){
        super.onPause();
        //releaseMediaRecorder
        releaseCamera();
        Log.d(LOG_TAG, "Camera is released");
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            //setCameraDisplayOrientation(getActivity(), 0, mCamera);
//            mCamera.setPreviewDisplay(mHolder);
//            mCamera.startPreview();
            Log.d(LOG_TAG, "Onresume" );
        } catch (Exception e) {
            Log.d(LOG_TAG, "On Resume error " + e.getMessage());
        }
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
     * @param activity
     * @param cameraId
     * @return default orientation for the correct preview
     */
    private int setCameraDisplayOrientation(Activity activity, int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;

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
        File mediaStorageDir = new File(getActivity().getExternalFilesDir(null).getPath());
        //images persist after app is uninstalled

        //create storage directory
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Log.d(LOG_TAG," failed to create directory");
                return null;
            }
        }

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








}
