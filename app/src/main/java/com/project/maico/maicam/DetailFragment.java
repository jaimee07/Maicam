package com.project.maico.maicam;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.project.maico.maicam.GalleryUtil.ImageLoader;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static Activity myContext;

    private ImageLoader mImageLoader;
    private ImageView mImageView;
    private String mFileName;

    public static final String IMAGE_DATA_EXTRA = "extra_image_data";

    /**
     * Factory method to generate a new instance of the fragment given an image number
     *
     * @param filename the filename of image  to load
     * @return A new instance of DetailFragment with image number extras(?)
     */
    public static DetailFragment newInstance(String filename){
        final DetailFragment f = new DetailFragment();

        final Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, filename);

        f.setArguments(args);
        Log.d(LOG_TAG,"new instance");
        return f;
    }

    /**
     * Empty constructor as per the Fragment documentation
     */
    public DetailFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myContext = activity;
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
        final View view = inflater.inflate(R.layout.fragment_detail, container, false);

        mImageView = (ImageView) view.findViewById(R.id.imageView);
        //TODO: set height and width of the image
//        reqWidth = mImageView.getWidth();
//        reqHeight = mImageView.getHeight();

        mFileName = getArguments()!=null ? (String) getArguments().get(IMAGE_DATA_EXTRA) : null;

        Log.d(LOG_TAG, "OnCreateView in detail fragment");
        mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//        imageView.setImageResource(R.drawable.image_placeholder);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Use the parent activity to load the image asynchronously into the ImageView
        //so a single cache can be used over all pages in the ViewPager

        if(DetailActivity.class.isInstance(myContext)){
            mImageLoader = ((DetailActivity)myContext).getImageLoader();
            mImageLoader.loadBitmap(mFileName, mImageView);
        }

        // Pass clicks on the ImageView to the parent activity to handle
        if(View.OnClickListener.class.isInstance(myContext)&& Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
            mImageView.setOnClickListener((View.OnClickListener)myContext);
        }


    }
}
