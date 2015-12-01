package com.project.maico.maicam.GalleryUtil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileFilter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Clover on 11/14/2015.
 */
public class ImageUtil {
    public static Context mContext;

    public ImageUtil(Context context){
        mContext = context;
    }

    /**
     * Loading subsampled version of the full image based on required width and height
     * @param options dimensions and type of image data
     * @param reqWidth required width of image
     * @param reqHeight required height of image
     * @return inSampleSize
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight){
        //raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth){
            final int halfHeight = height/2;
            final int halfWidth = width/2;
            //calculate largest inSampleSize, (height and width) > requested
            while(halfHeight/inSampleSize > reqHeight &&
                    halfWidth/inSampleSize > reqWidth){
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Decode bitmap from a resource
     * @param res Resources
     * @param resId Resource id
     * @param reqWidth Required width
     * @param reqHeight Required height
     * @return Bitmap Sampled bitmap
     */
    public static Bitmap decodeSampledBitmapFromResource(
            Resources res, int resId, int reqWidth, int reqHeight){
        //Decode w/ inJustDecodeBounds = true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        //calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        //Decode bitmap w/ in sampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromFile(
            String filename, int reqWidth, int reqHeight){
        //Decode w/ inJustDecodeBounds = true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        //calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        //Decode bitmap w/ in sampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Decode and sample down a bitmap from a file input stream to the requested width and height.
     *
     * @param fileDescriptor The file descriptor to read from
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromDescriptor(
            FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }


    public static File[] getFilesFromMaicam(Context context){
        //String filepath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator + "Pictures/" + "Beautiful Designs UI";
        File[] files = null;

        String filepath = context.getExternalFilesDir(null).getAbsolutePath();
        File currentDir =  new File(filepath);
        if(currentDir.isDirectory()) {
            files = currentDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    boolean allow = pathname.toString().toLowerCase().endsWith(".jpg") || pathname.toString().toLowerCase().endsWith(".png");
                    return allow;
                }
            });
        }

        for(File file:files){
            Log.d("gallery fragment","image key: " + file.getAbsolutePath());
        }

        return files;
    }

    /**
     *
     * @param context
     * @param uniqueName folder name
     * @return File directory for disk cache
     */

    public static File getDiskCacheDir(Context context, String uniqueName){
        final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable() ?
                        context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();

        Log.d("gallery fragment", cachePath + cachePath + File.separator + uniqueName);
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * Code from DisplayingBitmap
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}
