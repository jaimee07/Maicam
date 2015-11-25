package com.project.maico.maicam;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.project.maico.maicam.GalleryUtil.ImageFetcher;

/**
 * A placeholder fragment containing a simple view.
 */
public class GalleryDetailActivityFragment extends Fragment {

    private static final String LOG_TAG = GalleryDetailActivityFragment.class.getSimpleName();

    private static String mFilename;
    private static ImageView mImageView;
    private static int reqWidth;
    private static int reqHeight;

    public static final String IMAGE_DATA_EXTRA = "extra_image_data";

    /**
     * Factory method to generate a new instance of the fragment given an image number
     *
     * @param filename the filename of image  to load
     * @return A new instance of GalleryDetailActivityFragment with image number extras(?)
     */
    public static GalleryDetailActivityFragment newInstance(String filename){
        final GalleryDetailActivityFragment f = new GalleryDetailActivityFragment();

        final Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, filename);

        f.setArguments(args);
        Log.d(LOG_TAG,"new instance");
        return f;
    }

    /**
     * Empty constructor as per the Fragment documentation
     */
    public GalleryDetailActivityFragment() {

    }

    /**
     * Populate image using the filename from extras
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Set mImageVIew
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_gallery_detail, container, false);

        mImageView = (ImageView) view.findViewById(R.id.imageView);
        reqWidth = mImageView.getWidth();
        reqHeight = mImageView.getHeight();

        mFilename = getArguments()!=null ? (String) getArguments().get(IMAGE_DATA_EXTRA) : null;

        Log.d(LOG_TAG, "OnCreateView in detail fragment");
        mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        BitmapWorkerTask task = new BitmapWorkerTask();
        Log.d(LOG_TAG, mFilename);
        task.execute(mFilename);

        return view;
    }

    public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        //private final WeakReference<ImageView> imageViewReference;
        private String data;

        public BitmapWorkerTask() {
            //use a weak reference to ensure the ImageView can be garbage collected
            //imageViewReference = new WeakReference<>(imageView);
        }

        /**
         * Decode image in background
         * @param params
         * @return
         */
        @Override
        protected Bitmap doInBackground(String ... params) {
            data = params[0];
            Bitmap bitmap = ImageFetcher.decodeSampledBitmapFromFile(data, 300, 300);

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mImageView.setImageBitmap(bitmap);
            Log.d(LOG_TAG, "Image bitmap is set");

        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Use the parent activity to load the image asynchronously into the ImageView
        //so a single cache can be used over all pages in the ViewPager

        if(GalleryDetailActivity.class.isInstance(getActivity())){

        }















    }
}
