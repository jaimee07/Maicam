package com.project.maico.maicam;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * A placeholder fragment containing a simple view.
 */
public class GalleryDetailActivityFragment extends Fragment {

    private static String mFilename;
    private static ImageView mImageView;

    private static final String IMAGE_DATA_EXTRA = "extra_image_data";

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
        mFilename = getArguments()!=null ? (String) getArguments().get(IMAGE_DATA_EXTRA) : null;
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

        mImageView.setImageResource(R.drawable.image_placeholder);

        return view;
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
