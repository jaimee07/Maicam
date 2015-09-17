package com.project.maico.maicam;

import android.app.Activity;
import android.content.Context;
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
    private static int mImageOrientation = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        //Create camera
        mCamera = getCameraInstance();

        //Create preview view
        mPreview = new CameraPreview(getActivity(), mCamera);
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
        preview.addView(mPreview);

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

                int degrees = 90;
                String orient = "portrait";
                if(orientation >= 45 && orientation<135){
                    degrees = 180;
                    orient = "landscape";
                }else if(orientation >= 135 && orientation<225){
                    degrees = 270;
                    orient = "portrait";
                }else if(orientation >= 225 && orientation<315){
                    degrees = 0;
                    orient = "landscape";
                }
                mImageOrientation = degrees;

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
            int orientation = setCameraDisplayOrientation(getActivity(), 0);



            File pictureFile =  getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if(pictureFile == null){
                Log.d(LOG_TAG, "Error creating media file");
                return;
            }
            try{
                FileOutputStream fos = new FileOutputStream(pictureFile);
                //drawing of image into file
                fos.write(data);
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
