package com.project.maico.maicam;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class GalleryDetailActivity extends FragmentActivity {
    private static final String TAG = GalleryDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportFragmentManager().findFragmentByTag(TAG)==null){
            final android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, new GalleryDetailActivityFragment(), TAG);
            ft.commit();
        }
    }


}
