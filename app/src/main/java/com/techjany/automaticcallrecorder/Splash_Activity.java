package com.techjany.automaticcallrecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class Splash_Activity extends Activity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences SP1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean b1=SP1.getBoolean("LOCK",false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
        }
        if(b1){
            Intent intent=new Intent(getApplicationContext(),PinLock.class);
            startActivity(intent);
            finish();
        }else{
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
