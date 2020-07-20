package com.example.epilepsycare;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    public static boolean isServiceStopped = false;

    TextView tv_safetySTS_safe,tv_safetySTS_unsafe;
    CheckBox chk_bx_Notify,chk_bx_sendSMS,chk_bx_voiceAlert,chk_bx_sendLocation;
    Switch btn_startService;

    Intent serviceIntent;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Asking for permission
        ActivityCompat.requestPermissions(MainActivity.this,new String[]
                {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        initialize();
        savedPreferences();



        // For foreground service
        if(isMyServiceRunning()){
            btn_startService.setChecked(true);
            Toast.makeText(this, "Service IS Running", Toast.LENGTH_SHORT).show();
        }else {
            btn_startService.setChecked(false);
            serviceIntent = new Intent(MainActivity.this,MyService.class);
        }
        btn_startService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(isMyServiceRunning()){
                        Toast.makeText(MainActivity.this, "Service IS Running", Toast.LENGTH_SHORT).show();
                    }else {
                        isServiceStopped=false;
                        Toast.makeText(MainActivity.this, "Service Started", Toast.LENGTH_SHORT).show();
                        startService(serviceIntent);
                    }
                }else{
//                    stopService(serviceIntent);
                    isServiceStopped = true;
                    Toast.makeText(MainActivity.this, "Service Stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // onChecked Change Listener
        chk_bx_Notify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyService.notificationAlert = chk_bx_Notify.isChecked();
                editor.putBoolean("notifyAlert",chk_bx_Notify.isChecked());
                editor.apply();
            }
        });
        chk_bx_sendSMS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyService.sendSMS = chk_bx_sendSMS.isChecked();
                editor.putBoolean("sendSMS",chk_bx_sendSMS.isChecked());
                editor.apply();
            }
        });
        chk_bx_sendLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyService.sendLocation = chk_bx_sendLocation.isChecked();
                editor.putBoolean("sendLocation",chk_bx_sendLocation.isChecked());
                editor.apply();
            }
        });
        chk_bx_voiceAlert.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyService.voiceAlert = chk_bx_voiceAlert.isChecked();
                editor.putBoolean("voiceAlert",chk_bx_voiceAlert.isChecked());
                editor.apply();
            }
        });

    }


    public boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MyService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void initialize(){

        // initializing text_views
        tv_safetySTS_safe = findViewById(R.id.safetySTS_safe);
        tv_safetySTS_unsafe = findViewById(R.id.safetySTS_unsafe);

        // initializing checkboxes
        chk_bx_Notify = findViewById(R.id.chk_bx_notify);
        chk_bx_sendSMS = findViewById(R.id.chk_bx_sendSMS);
        chk_bx_voiceAlert = findViewById(R.id.chk_bx_voice);
        chk_bx_sendLocation = findViewById(R.id.chk_bx_location);

        // initializing switch
        btn_startService = findViewById(R.id.btn_startService);

    }

    public void savedPreferences(){
        chk_bx_Notify.setChecked(sharedPreferences.getBoolean("notifyAlert",true));
        chk_bx_sendSMS.setChecked(sharedPreferences.getBoolean("sendSMS",false));
        chk_bx_sendLocation.setChecked(sharedPreferences.getBoolean("sendLocation",false));
        chk_bx_voiceAlert.setChecked(sharedPreferences.getBoolean("voiceAlert",false));

        MyService.notificationAlert = sharedPreferences.getBoolean("notifyAlert",true);
        MyService.sendSMS = sharedPreferences.getBoolean("sendSMS",false);
        MyService.sendLocation = sharedPreferences.getBoolean("sendLocation",false);
        MyService.voiceAlert = sharedPreferences.getBoolean("voiceAlert",false);
    }

    public void getLocation(){}

    public void voicePrompt(){}

    public void getNotification(){}

    public void helpWindow(){}

    public void fallEvents(){}



}
