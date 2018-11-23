package com.example.andjm.scanny;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CrontabFragment extends DialogFragment {
    private EditText textDPI;
    private EditText textTime;
    private Button changeDPI;
    private Button changeTime;

    public static CrontabFragment newInstance(String data) {
        CrontabFragment dialog = new CrontabFragment();
        Bundle args = new Bundle();
        args.putString("data", data);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.crontab_fragment, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        String data= getArguments().getString("data", "");
        textDPI    = view.findViewById(R.id.textDPI);
        textTime   = view.findViewById(R.id.textTime);
        changeDPI  = view.findViewById(R.id.changeDPI);
        changeTime = view.findViewById(R.id.changeTime);

        changeDPI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"DPI updated!", Toast.LENGTH_SHORT).show();
            }
        });

        changeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"DPI updated!", Toast.LENGTH_SHORT).show();
            }
        });
    }




}
