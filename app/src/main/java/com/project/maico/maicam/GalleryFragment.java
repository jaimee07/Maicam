package com.project.maico.maicam;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.project.maico.maicam.GalleryUtil.AsyncDrawable;
import com.project.maico.maicam.GalleryUtil.ImageFetcher;

import java.io.File;
import java.lang.ref.WeakReference;

public class GalleryFragment extends Fragment implements AdapterView.OnItemClickListener{
    private static String LOG_TAG = GalleryFragment.class.getSimpleName();

    private ImageAdapter mAdapter;
    private int mImageThumbSize = 0;
    private int mImageThumbSpacing = 0;

    public static int mReqHeight = 0;
    public static int mReqWidth = 0;

    private static File[] files;
    private static Bitmap mPlaceholderImage;

    private LruCache<String, Bitmap> mMemoryCache;


    //required empty constructor
    public GalleryFragment(){}

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get maximum available VM memory
        //Stored in kilobytes as LruCache takes an integer in its constructor
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);
        //use 1/8th of the available memory for our cache
        final int cacheSize = maxMemory/8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                //return super.sizeOf(key, value);
                //cache size will be measured in kbytes
                return bitmap.getByteCount()/1024;
            }
        };

        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
        mPlaceholderImage = BitmapFactory.decodeResource(getResources(), R.drawable.image_placeholder);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_gallery, container, false);
        final GridView gridView = (GridView) v.findViewById(R.id.gridView);

        files = ImageFetcher.getFilesFromMaicam(getActivity());

        mAdapter = new ImageAdapter(getActivity());
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
                                final int columnWidth = (gridView.getWidth() / numColumns) - mImageThumbSpacing;

                                mAdapter.setNumColumns(numColumns);
                                mAdapter.setItemHeight(columnWidth); //square thumbnails
                                mReqHeight = columnWidth;
                                mReqWidth = columnWidth;

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

        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*final Intent intent = new Intent(getActivity(), GalleryDetail.class);
        intent.putExtra(GalleryDetail.EXTRA_IMAGE, position);
        startActivity(intent);
        */
        Log.d(LOG_TAG, "OnItemClick" + position);
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
            Log.d(LOG_TAG,"get view for postion: " + position);
            loadBitmap(files[position], imageView, position);
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

    /**
     * Load bitmap in the background
     * @param file
     * @param imageView
     */
    public void loadBitmap(File file, ImageView imageView, int position){
        final String imageKey = file.getAbsolutePath();
        final Bitmap bitmap = getBitmapFromMemCache(imageKey);

        if(bitmap!=null){
            imageView.setImageBitmap(bitmap);
            Log.d(LOG_TAG, "setbitmap from cache done");
            Log.d(LOG_TAG, "-----------------------------");
        }else {
             if(cancelPotentialWork(imageKey, imageView)){
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                //create AsyncDrawable and bind it to the ImageView
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(getResources(), mPlaceholderImage, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(file.getAbsolutePath());

            }
        }
    }

    /**
     * Checks associated task with current image view
     * @param data
     * @param imageView
     * @return
     */
    public static boolean cancelPotentialWork(String data, ImageView imageView){
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if(bitmapWorkerTask != null){
            final String bitmapData = bitmapWorkerTask.data;
            //if bitmap is not yet set, or it differs from new data
            if(bitmapData == null|| bitmapData != data){
                bitmapWorkerTask.cancel(true); //cancel previous task
            }else{
                return false; //same work is already in progress
            }
        }
        return true; //No task is associated or existing task is cancelled
    }


    /**
     * Helper method for cancelPotentialWork and onPostExecute of BitmapWorkerTask.
     *
     * Returns the associated BitmapWorkerTask associated with the ImageView
     * @param imageView
     * @return
     */
    public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView){
        if(imageView != null){
            final Drawable drawable = imageView.getDrawable();
            if(drawable instanceof AsyncDrawable){
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * Process bitmaps in a background thread using AsyncTask
     */
    public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String data;

        public BitmapWorkerTask(ImageView imageView) {
            //use a weak reference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
        }

        /**
         * Decode image in background
         * @param params
         * @return
         */
        @Override
        protected Bitmap doInBackground(String ... params) {
            data = params[0];
            final Bitmap bitmap = ImageFetcher.decodeSampledBitmapFromFile(data, mReqWidth, mReqHeight);

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(isCancelled()){
                bitmap = null;
            }
            if(imageViewReference!=null && bitmap!=null){
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if(this==bitmapWorkerTask && imageView!=null){
                    imageView.setImageBitmap(bitmap);
                    addBitmapToMemoryCache(data, bitmap);
                    Log.d(LOG_TAG,"setbitmap done");
                    Log.d(LOG_TAG,"-------------------------");
                }
            }
        }
    }


    // memory cache methods

    /**
     * Adds bitmap to memory cache
     * @param key
     * @param bitmap
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap){
        if(getBitmapFromMemCache(key) == null){
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * Gets bitmap in memory cache using key value
     * @param key
     * @return
     */
    public Bitmap getBitmapFromMemCache(String key){
        return mMemoryCache.get(key);
    }



}