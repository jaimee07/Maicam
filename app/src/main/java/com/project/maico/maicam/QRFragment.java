package com.project.maico.maicam;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class QRFragment extends Fragment {
    private static final String LOG_TAG = QRFragment.class.getSimpleName();

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_qr, container, false);
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
