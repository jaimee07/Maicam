package com.project.maico.maicam;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.project.maico.maicam.GalleryUtil.ImageLoader;
import com.project.maico.maicam.GalleryUtil.ImageUtil;

import java.io.File;

public class GalleryFragment extends Fragment implements AdapterView.OnItemClickListener{
    private static String LOG_TAG = GalleryFragment.class.getSimpleName();
    private static Activity myContext;

    private ImageAdapter mAdapter;
    private ImageLoader mImageLoader;
    private int mImageThumbSize;
    private int mImageThumbSpacing;

    private static File[] files;
    private static Bitmap mPlaceholderImage;

    //disk caching
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    //memory caching
    private static final float CACHE_PERCENTAGE = 0.125f;

    //required empty constructor
    public GalleryFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize values
        mImageThumbSize =  getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        mPlaceholderImage = BitmapFactory.decodeResource(getResources(), R.drawable.image_placeholder);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_gallery, container, false);
        final GridView gridView = (GridView) v.findViewById(R.id.gridView);

        files = ImageUtil.getFilesFromMaicam(myContext);

        mAdapter = new ImageAdapter(myContext);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);

        // This listener is used to get the final width of the GridView and then calculate the
        // number of columns and the width of each column. The width of each column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
        // of each view so we get nice square thumbnails.
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        if (mAdapter.getNumColumns() == 0) {

                            final int numColumns = (int) Math.floor(gridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                            if (numColumns > 0) {
                                final int imageSize = (gridView.getWidth() / numColumns) - mImageThumbSpacing;

                                mAdapter.setNumColumns(numColumns);
                                mAdapter.setItemHeight(imageSize); //square thumbnails

                                //Update image thumbnail size
                                mImageThumbSize = imageSize;

                                if (BuildConfig.DEBUG) {
                                    Log.d(LOG_TAG, "onCreateView - numColumns set to " + numColumns);
                                }
                                //not sure about this
                                gridView.getViewTreeObserver()
                                        .removeOnGlobalLayoutListener(this);

                            }
                        }
                    }
                });

        mImageLoader= new ImageLoader(myContext, mImageThumbSize, mImageThumbSize, CACHE_PERCENTAGE, DISK_CACHE_SUBDIR, mPlaceholderImage );

        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Intent intent = new Intent(myContext, DetailActivity.class);
        intent.putExtra (com.project.maico.maicam.DetailFragment.IMAGE_DATA_EXTRA, (int) id);

        //TODO: make scale up animation
        startActivity(intent);
    }

    /**
     * Backing adapter for GridView implementation of Gallery Fragment
     */
    private class ImageAdapter extends BaseAdapter{
        private final Context mContext;
        private int mItemHeight = 0;
        private int mNumColumns = 0;
        private GridView.LayoutParams mImageViewLayoutParams;

        public ImageAdapter(Context context){
            super();
            mContext = context;
            mImageViewLayoutParams = new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        @Override
        public int getCount() {
            return files.length;
        }

        @Override
        public File getItem(int position) {
            return files[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            //if convertView is not recycled, initialize
            if(convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setLayoutParams(mImageViewLayoutParams);
            }else{
                imageView = (ImageView) convertView;
            }

            //Check height matches our calculated column width
            if(imageView.getLayoutParams().height != mItemHeight){
                imageView.setLayoutParams(mImageViewLayoutParams);
            }
            Log.d(LOG_TAG,"get view for position: " + position);
            mImageLoader.loadBitmap(files[position].getAbsolutePath(), imageView);
            return imageView;
        }

        public void setNumColumns(int numColumns) {
            mNumColumns = numColumns;
        }

        public int getNumColumns(){
            return mNumColumns;
        }

        public void setItemHeight(int height) {
            if(height == mItemHeight){
                return;
            }
            mItemHeight = height;
            mImageViewLayoutParams = new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight);
            notifyDataSetChanged();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myContext = activity;
        ImageButton imageButton = (ImageButton) myContext.findViewById(R.id.galleryButton);
        imageButton.setImageResource(R.drawable.ic_gallery_active);
    }

    @Override
    public void onDetach() {
        ImageButton imageButton = (ImageButton) myContext.findViewById(R.id.galleryButton);
        imageButton.setImageResource(R.drawable.ic_gallery);
        super.onDetach();
    }



}