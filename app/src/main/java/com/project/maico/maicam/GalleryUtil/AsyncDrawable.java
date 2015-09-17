package com.project.maico.maicam.GalleryUtil;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.project.maico.maicam.GalleryFragment;

import java.lang.ref.WeakReference;

/**
 * Created by Clover on 11/14/2015.
 */
public class AsyncDrawable extends BitmapDrawable {
    private final WeakReference<GalleryFragment.BitmapWorkerTask> bitmapWorkerTaskReference;

    public AsyncDrawable(Resources res, Bitmap bitmap, GalleryFragment.BitmapWorkerTask task) {
        super(res, bitmap);
        bitmapWorkerTaskReference =
                new WeakReference<>(task);
    }

    public GalleryFragment.BitmapWorkerTask getBitmapWorkerTask(){
        return bitmapWorkerTaskReference.get();
    }
}
