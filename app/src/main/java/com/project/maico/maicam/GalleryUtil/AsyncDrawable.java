package com.project.maico.maicam.GalleryUtil;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

/**
 * Created by Clover on 11/14/2015.
 */
public class AsyncDrawable extends BitmapDrawable {
    private final WeakReference<ImageLoader.BitmapWorkerTask> bitmapWorkerTaskReference;

    public AsyncDrawable(Resources res, Bitmap bitmap, ImageLoader.BitmapWorkerTask task) {

        super(res, bitmap);
        bitmapWorkerTaskReference =
                new WeakReference<>(task);
    }

    public ImageLoader.BitmapWorkerTask getBitmapWorkerTask(){
        return bitmapWorkerTaskReference.get();
    }
}
