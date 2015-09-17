package com.project.maico.maicam;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by Clover on 9/16/2015.
 */
public class GridViewAdapter extends ArrayAdapter {
    private Context context;
    private int resource;
    private ArrayList<Bitmap> data = new ArrayList();
    private ImageView imageView;

    public GridViewAdapter(Context context, int resource, ArrayList<Bitmap> data) {
        super(context, resource, data);
        this.context = context;
        this.resource = resource;
        this.data = data;
    }



    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View grid = view;

        if (grid == null) {
            //inflate xml
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            grid = inflater.inflate(resource, parent, false);
        }
        imageView = (ImageView) grid.findViewById(R.id.imageView);
        imageView.setImageBitmap(data.get(position));

        return grid;
    }






}
