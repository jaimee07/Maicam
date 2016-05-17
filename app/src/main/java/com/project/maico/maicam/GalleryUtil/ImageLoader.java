package com.project.maico.maicam.GalleryUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.project.maico.maicam.BuildConfig;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

/**
 * Created by Clover on 12/1/2015.
 * This class handles the loading of images and caching it to disk and memory.
 */
public class ImageLoader {
    private static final String LOG_TAG = ImageLoader.class.getSimpleName();

    private Context mContext;

    //image size
    private int mReqHeight;
    private int mReqWidth;
    private Bitmap mPlaceholderImage;

    //memory caching
    private LruCache<String, Bitmap> mMemoryCache;

    //disk caching
    private static final int DISK_CACHE_SIZE = 1024*1024*10; //10MB

    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;

    //Writing images to disk
    private static final int DISK_CACHE_INDEX = 0;
    private static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;

    public ImageLoader(Context context, int width, int height, float cachePercentage, String diskCacheSubdir, Bitmap placeHolderImage){
        mContext = context;

        mReqHeight = height;
        mReqWidth = width;
        mPlaceholderImage = placeHolderImage;

        //Get maximum available VM memory, stored in kilobytes as LruCache takes an integer in its constructor
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);

        //set cache size
        final int cacheSize = (int) (maxMemory*cachePercentage);
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                //return super.sizeOf(key, value);
                //cache size will be measured in kbytes
                return bitmap.getByteCount()/1024;
            }
        };

        //initialize disk cache n background thread
        File cacheDir = ImageUtil.getDiskCacheDir(mContext, diskCacheSubdir);
        new InitDiskCacheTask().execute(cacheDir);

    }


    /**
     * Load bitmap in the background
     * @param imageKey
     * @param imageView
     */
    public void loadBitmap(String imageKey, ImageView imageView){
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
                        new AsyncDrawable(mContext.getResources(), mPlaceholderImage, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(imageKey);

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
            Bitmap bitmap = getBitmapFromDiskCache(data);

            if(bitmap == null) { //not found in disk cache
                bitmap = ImageUtil.decodeSampledBitmapFromFile(data, mReqWidth, mReqHeight);
            }

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
                    //add bitmap to cache
                    addBitmapToCache(data, bitmap);
                    Log.d(LOG_TAG, "setbitmap done");
                    Log.d(LOG_TAG,"-------------------------");
                }
            }
        }
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
     * Initialize Disk Cache
     */
    class InitDiskCacheTask extends AsyncTask<File, Void, Void>{
        @Override
        protected Void doInBackground(File... params){
            synchronized(mDiskCacheLock){
                File cacheDir = params[0];
                try {
                    mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
                    mDiskCacheStarting = false; //finished initialization
                    mDiskCacheLock.notifyAll(); //wake any waiting thread
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }


    /**
     * Adds bitmap to memory cache
     * @param data
     * @param bitmap
     */
    public void addBitmapToCache(String data, Bitmap bitmap){
        //Add to memory cache
        if(getBitmapFromMemCache(data) == null){
            mMemoryCache.put(data, bitmap);
        }
        //Add to disk cache
        synchronized (mDiskCacheLock) {
            // Add to disk cache
            if (mDiskLruCache != null) {
                final String key = ImageUtil.hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            bitmap.compress(
                                    DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, out);
                            editor.commit();
                            out.close();

                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "addBitmapToCache - " + e);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "addBitmapToCache - " + e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {}
                }
            }
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

    /**
     * Get from disk cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromDiskCache(String data) {
        //BEGIN_INCLUDE(get_bitmap_from_disk_cache)
        final String key = ImageUtil.hashKeyForDisk(data);
        Bitmap bitmap = null;

        synchronized (mDiskCacheLock) {
            //wait while disk cache is started from background
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                        if (BuildConfig.DEBUG) {
                            Log.d(LOG_TAG, "Disk cache hit");
                        }
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            FileDescriptor fd = ((FileInputStream) inputStream).getFD();

                            // Decode bitmap, but we don't want to sample so give
                            // MAX_VALUE as the target dimensions
                            bitmap = ImageUtil.decodeSampledBitmapFromDescriptor(
                                    fd, Integer.MAX_VALUE, Integer.MAX_VALUE);
                        }
                    }
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "getBitmapFromDiskCache - " + e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {}
                }
            }
            return bitmap;
        }
        //END_INCLUDE(get_bitmap_from_disk_cache)
    }




}
