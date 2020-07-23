package com.example.epilepsycare;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    public static boolean isServiceStopped = false;

    TextView tv_safetySTS_safe,tv_safetySTS_unsafe;
    Button btn_emgContacts,btn_history,btn_help;
    CheckBox chk_bx_Notify,chk_bx_sendSMS,chk_bx_voiceAlert,chk_bx_sendLocation;
    Switch btn_startService;

    Intent serviceIntent;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    AlertDialog.Builder contactDialog;
    View contactView;

    // Emergency Contact
    public static String emgName, emgNumber, emgMessage;
    TextView tv_emg_name, tv_emg_number, tv_emg_message;

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

        contactDialog = new AlertDialog.Builder(MainActivity.this);
        contactView = getLayoutInflater().inflate(R.layout.emg_contacts,null);
        tv_emg_name = contactView.findViewById(R.id.emg_Name);
        tv_emg_number = contactView.findViewById(R.id.emg_Number);
        tv_emg_message = contactView.findViewById(R.id.emg_Message);

        initialize();
        savedPreferences();
        emgSavedContact();



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

        btn_emgContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_emg_name.setText(emgName);
                tv_emg_number.setText(emgNumber);
                tv_emg_message.setText(emgMessage);
                showEmgContact();
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

        // initializing buttons
        btn_emgContacts = findViewById(R.id.btn_emgContacts);
        btn_history = findViewById(R.id.btn_history);
        btn_help = findViewById(R.id.btn_help);

    }

    public void savedPreferences(){
        chk_bx_Notify.setChecked(sharedPreferences.getBoolean("notifyAlert",true));
        chk_bx_sendSMS.setChecked(sharedPreferences.getBoolean("sendSMS",false));
        chk_bx_sendLocation.setChecked(sharedPreferences.getBoolean("sendLocation",false));
        chk_bx_voiceAlert.setChecked(sharedPreferences.getBoolean("voiceAlert",false));

        /*tv_emg_name.setText(sharedPreferences.getString("emgName","Name"));
        tv_emg_number.setText(sharedPreferences.getString("emgNumber","1234567890"));
        tv_emg_message.setText(sharedPreferences.getString("emgMessage","Hello, I need help."));*/

        MyService.notificationAlert = sharedPreferences.getBoolean("notifyAlert",true);
        MyService.sendSMS = sharedPreferences.getBoolean("sendSMS",false);
        MyService.sendLocation = sharedPreferences.getBoolean("sendLocation",false);
        MyService.voiceAlert = sharedPreferences.getBoolean("voiceAlert",false);

    }

    public void emgSavedContact(){
        emgName = sharedPreferences.getString(getString(R.string.emgName),"Name");
        emgNumber = sharedPreferences.getString(getString(R.string.emgNumber),"1234567890");
        emgMessage = sharedPreferences.getString(getString(R.string.emgMessage),"Hello, I need help");
    }

    public void showEmgContact(){
        contactDialog = new AlertDialog.Builder(MainActivity.this);
        contactView = getLayoutInflater().inflate(R.layout.emg_contacts,null);
        tv_emg_name = contactView.findViewById(R.id.emg_Name);
        tv_emg_number = contactView.findViewById(R.id.emg_Number);
        tv_emg_message = contactView.findViewById(R.id.emg_Message);
        Button btn_edit_emgContact = contactView.findViewById(R.id.btn_edit_emgContact);

        tv_emg_name.setText(emgName);
        tv_emg_number.setText(emgNumber);
        tv_emg_message.setText(emgMessage);

        contactDialog.setView(contactView);
        AlertDialog alertDialog1 = contactDialog.create();
        alertDialog1.setCanceledOnTouchOutside(true);
        alertDialog1.show();

        btn_edit_emgContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editEmgContact();
            }
        });
    }
    public void editEmgContact(){
        AlertDialog.Builder editContactDialog = new AlertDialog.Builder(MainActivity.this);
        View editEmgContactView = getLayoutInflater().inflate(R.layout.edit_emg_contacts,null);
        final EditText edit_emg_name = editEmgContactView.findViewById(R.id.edit_Name);
        final EditText edit_emg_number = editEmgContactView.findViewById(R.id.edit_Number);
        final EditText edit_emg_message = editEmgContactView.findViewById(R.id.edit_Message);
        Button btn_cancel = editEmgContactView.findViewById(R.id.btn_cancel);
        Button btn_save = editEmgContactView.findViewById(R.id.btn_save);

        editContactDialog.setView(editEmgContactView);
        final AlertDialog alertDialog2 = editContactDialog.create();
        alertDialog2.setCanceledOnTouchOutside(false);
        alertDialog2.show();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog2.dismiss();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                emgName = edit_emg_name.getText().toString().trim();
                emgNumber = edit_emg_number.getText().toString().trim();
                emgMessage = edit_emg_message.getText().toString().trim();

                tv_emg_name.setText(emgName);
                tv_emg_number.setText(emgNumber);
                tv_emg_message.setText(emgMessage);

                if(emgName.length() == 0 || emgNumber.length() == 0 || emgMessage.length() == 0){
                    Toast.makeText(MainActivity.this, "Please Enter all details", Toast.LENGTH_SHORT).show();
                }else {
//                    MyService.emgName = emgName;
//                    MyService.emgNumber = emgNumber;
//                    MyService.emgMessage = emgMessage;
                    editor.putString(getString(R.string.emgName),emgName);
                    editor.putString(getString(R.string.emgNumber),emgNumber);
                    editor.putString(getString(R.string.emgMessage),emgMessage);
                    editor.apply();

                    Toast.makeText(MainActivity.this, "Contact Saved", Toast.LENGTH_SHORT).show();
                    alertDialog2.dismiss();
                }
            }
        });

    }

    public void getLocation(){}

    public void voicePrompt(){}

    public void getNotification(){}

    public void helpWindow(){}

    public void fallEvents(){}



}
