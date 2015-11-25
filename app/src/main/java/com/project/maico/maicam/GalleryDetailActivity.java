package com.project.maico.maicam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class GalleryDetailActivity extends FragmentActivity {
    private static final String TAG = GalleryDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String filename = intent.getStringExtra(GalleryDetailActivityFragment.IMAGE_DATA_EXTRA);
        Log.d(TAG, "filename: " + filename);


        if(getSupportFragmentManager().findFragmentByTag(TAG)==null){
            final android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, GalleryDetailActivityFragment.newInstance(filename), TAG);
            ft.commit();
        }
    }


}
