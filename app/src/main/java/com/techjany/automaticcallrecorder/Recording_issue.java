package com.techjany.automaticcallrecorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jaredrummler.android.device.DeviceName;

public class Recording_issue extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_issue);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String deviceName = DeviceName.getDeviceName();
        TextView tv=(TextView)findViewById(R.id.textView);
        tv.setText(deviceName);
    }
}
