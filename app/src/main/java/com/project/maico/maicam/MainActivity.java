package com.project.maico.maicam;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_logo);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        Fragment defaultFragment = new GalleryFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.main_fragment_container, defaultFragment);
        ft.commit();

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fabcam);
        fab.setOnClickListener(this);
        ImageButton locationButton = (ImageButton) findViewById(R.id.locationButton);
        locationButton.setOnClickListener(this);
        ImageButton qrButton = (ImageButton) findViewById(R.id.qrButton);
        qrButton.setOnClickListener(this);
        ImageButton galleryButton = (ImageButton) findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(this);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_map){
            openLocation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openLocation(){
        //Fragment locationFragment = (LocationFragment) getSupportFragmentManager().findFragmentByTag("locationFragmentTag");

        //Setup location manager
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location==null){
            Toast.makeText(this,"No location detected",Toast.LENGTH_LONG).show();
            return;
        }else{
            //use lat and longitude
            String lat = String.valueOf(location.getLatitude()); //"14.655044"; //latitude
            String longi = String.valueOf(location.getLongitude()); //"120.977299"; //longitude
            Uri geoLocation = Uri.parse("geo:" + lat + "," + longi);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(geoLocation);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Log.d(LOG_TAG, "Couldn't call location, no receiving apps installed!");
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.fabcam:
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                if(location==null){
//                    Toast.makeText(this, "Waiting for GPS signal...", Toast.LENGTH_SHORT).show();
                /*}else */if(QRFragment.bitmapQRCode==null){
                    Toast.makeText(this, "Waiting for QR Code image, see the QR Code Tab", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(this, CameraActivity.class);
                    startActivity(intent);
                    Log.d("MaicamDebug", "Camera Activity started");
                }
                break;
            case R.id.locationButton:
                Fragment f = new LocationFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.main_fragment_container, f, "locationFragmentTag");
                ft.commit();
                break;
            case R.id.galleryButton:
                Fragment f1 = new GalleryFragment();
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.main_fragment_container, f1);
                ft1.commit();
                break;
            case R.id.qrButton:
                Fragment f2 = new QRFragment();
                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                ft2.replace(R.id.main_fragment_container, f2);
                ft2.commit();
                break;
            default: break;
        }

    }
}
