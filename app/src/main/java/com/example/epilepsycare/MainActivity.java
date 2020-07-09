package com.example.epilepsycare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    TextView tv_safetySTS_safe,tv_safetySTS_unsafe;
    CheckBox chk_bx_Notify,chk_bx_sendSMS,chk_bx_voicePrompt,chk_bx_sendLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);






    }

    public void initialize(){

        // initializing text_views
        tv_safetySTS_safe = findViewById(R.id.safetySTS_safe);
        tv_safetySTS_unsafe = findViewById(R.id.safetySTS_unsafe);

        // initializing checkboxes
        chk_bx_Notify = findViewById(R.id.chk_bx_notify);
        chk_bx_sendSMS = findViewById(R.id.chk_bx_sendSMS);
        chk_bx_voicePrompt = findViewById(R.id.chk_bx_voice);
        chk_bx_sendLocation = findViewById(R.id.chk_bx_location);

    }


    public void getLocation(){}

    public void voicePrompt(){}

    public void getNotification(){}

    public void helpWindow(){}

    public void fallEvents(){}



}
