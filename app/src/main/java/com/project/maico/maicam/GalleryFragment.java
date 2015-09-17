package com.project.maico.maicam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;


public class GalleryFragment extends Fragment {

    private final static String LOG_TAG = GalleryFragment.class.getSimpleName();

    private GridView gridView;
    private GridViewAdapter gridAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    //data for gridview
    private ArrayList<ImageItem> getData() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator + "Pictures/" + "Beautiful Designs UI";
        Log.v(LOG_TAG, filepath);
        File currentDir =  new File(filepath);
        if(currentDir.isDirectory()) {
            File[] files = currentDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    boolean allow = pathname.toString().toLowerCase().endsWith(".jpg") || pathname.toString().toLowerCase().endsWith(".png");
                    return allow;
                }
            });
            int i = 1;
            if(files!=null) {
                for (File file : files) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                    Log.v(LOG_TAG, bitmap.toString());
                    imageItems.add(new ImageItem(bitmap, "Image #" + i++));
                }
            }else{
                Log.e(LOG_TAG, "empty directory");
            }
//        TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
//        for (int i = 0; i < imgs.length(); i++) {
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs.getResourceId(i, -1));
//            imageItems.add(new ImageItem(bitmap, "Image#" + i));
//        }
        }else{
            Log.e(LOG_TAG, "file not directory");
        }

        if(imageItems==null){
            Log.e(LOG_TAG,"null  imageItems");
        }

        return imageItems;



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        gridView = (GridView) root.findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(getActivity(),
                                            R.layout.grid_item_layout,
                                            getData());


        gridView.setAdapter(gridAdapter);

        return root;
    }


}
