package com.project.maico.maicam.GalleryUtil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by Clover on 11/14/2015.
 */
public class ImageFetcher {
    public static Context mContext;

    public ImageFetcher(Context context){
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

}
