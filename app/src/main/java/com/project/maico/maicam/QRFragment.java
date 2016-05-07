package com.project.maico.maicam;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.project.maico.maicam.QRCode.Contents;
import com.project.maico.maicam.QRCode.QRCodeEncoder;

public class QRFragment extends Fragment {
    private static final String LOG_TAG = QRFragment.class.getSimpleName();
    public static Bitmap bitmapQRCode;

    public QRFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_qr, container, false);

        //add listener
        Button button1 = (Button) view.findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                EditText qrInputLat = (EditText) view.findViewById(R.id.qrInputLat);
                EditText qrInputLong = (EditText) view.findViewById(R.id.qrInputLong);
                Float latitudeFloat = Float.valueOf(qrInputLat.getText().toString());
                Float longitudeFloat = Float.valueOf(qrInputLong.getText().toString());

                //Find screen size
                WindowManager manager = ((WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE));
                Display display = manager.getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int width = point.x;
                int height = point.y;
                int smallerDimension = width < height ? width : height;
                smallerDimension = smallerDimension * 3/4;

                Bundle bundle = new Bundle();
                bundle.putFloat("LAT", latitudeFloat);
                bundle.putFloat("LONG", longitudeFloat);


                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder("Anoto",
                        bundle,
                        Contents.Type.LOCATION,
                        BarcodeFormat.QR_CODE.toString(),
                        smallerDimension);

                // A geographic location. Use as follows:
                // Bundle bundle = new Bundle();
                // bundle.putFloat("LAT", latitude);
                // bundle.putFloat("LONG", longitude);
                // intent.putExtra(Intents.Encode.DATA, bundle);

                //public static final String LOCATION = "LOCATION_TYPE";



                try {
                    bitmapQRCode = qrCodeEncoder.encodeAsBitmap();
                    //bitmapQRCode = BitmapFactory.decodeResource(getResources(), R.drawable.ic_qr);
                    ImageView myImage = (ImageView) view.findViewById(R.id.imageView1);
                    myImage.setImageBitmap(bitmapQRCode);

                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ImageButton imageButton = (ImageButton) getActivity().findViewById(R.id.qrButton);
        imageButton.setImageResource(R.drawable.ic_qr_active);
    }

    @Override
    public void onDetach() {
        ImageButton imageButton = (ImageButton) getActivity().findViewById(R.id.qrButton);
        imageButton.setImageResource(R.drawable.ic_qr);
        super.onDetach();
    }

}
