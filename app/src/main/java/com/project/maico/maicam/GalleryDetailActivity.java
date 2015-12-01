package com.project.maico.maicam;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.project.maico.maicam.GalleryUtil.ImageLoader;
import com.project.maico.maicam.GalleryUtil.ImageUtil;

import java.io.File;

public class GalleryDetailActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String LOG_TAG = GalleryDetailActivity.class.getSimpleName();

    private ImagePagerAdapter mAdapter;
    private ViewPager mPager;
    private ImageLoader mImageLoader;
    private static File[] files;

    //disk caching
    private static final String DISK_CACHE_SUBDIR = "images";

    //memory caching
    private static final float CACHE_PERCENTAGE = 0.25f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_detail_pager);

        //get files from maicam
        files = ImageUtil.getFilesFromMaicam(this);
        //Fetch screen height and width to use as our max size when loading images
        //as this activity runs full screen

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        //Use half of the longest dimension to resize our images
        final int longest = (height > width ? height : width)/2;

        //Set ImageLoader to load and cache our images
        mImageLoader = new ImageLoader(this, longest, longest, CACHE_PERCENTAGE, DISK_CACHE_SUBDIR, null);


        //Set up ViewPager and backing adapter
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), files.length);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        //mPager.setPageMargin((int) getResources().getDimension(R.dimen.margin));
        mPager.setOffscreenPageLimit(2);


        //Set up activity to go full screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            //actionBar.setDisplayShowTitleEnabled(true);
            //actionBar.setDisplayShowHomeEnabled(true);

            /*mPager.setOnSystemUiVisibilityChangeListener(
                    new View.OnSystemUiVisibilityChangeListener(){

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if((visibility & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0){
                                actionBar.hide();
                                Log.d(LOG_TAG, "hide");
                            }else{
                                actionBar.show();
                                Log.d(LOG_TAG, "show");
                            }
                        }
                    }
            );*/

            //Start low profile mode and hide actionbar
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            actionBar.hide();
        }

        //Set the current item based on the extra passed in to this activity
        final int extraCurrentItem = getIntent().getIntExtra(GalleryDetailActivityFragment.IMAGE_DATA_EXTRA, -1);

        if(extraCurrentItem != -1){
            mPager.setCurrentItem(extraCurrentItem);
        }

    }

    @Override
    public void onClick(View v) {
        final int vis = mPager.getSystemUiVisibility();
        if((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0){
            Toast.makeText(this, "visible actionbar", Toast.LENGTH_SHORT).show();
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            getSupportActionBar().show();
            Log.d(LOG_TAG, "flag visible");
        }else{
            Toast.makeText(this, "hide actionbar", Toast.LENGTH_SHORT).show();
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            getSupportActionBar().hide();
            Log.d(LOG_TAG, "low profile");
        }
    }

    /**
     * Called by the ViewPager child fragments to load images via the one ImageLoader
     */
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter{
        private final int mSize;

        public ImagePagerAdapter(FragmentManager fm, int size) {
            super(fm);
            mSize = size;
        }

        @Override
        public Fragment getItem(int position) {
            return GalleryDetailActivityFragment.newInstance(files[position].getAbsolutePath());
        }

        @Override
        public int getCount() {
            return mSize;
        }
    }



}
