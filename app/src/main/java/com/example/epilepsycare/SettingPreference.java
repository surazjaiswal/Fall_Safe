package com.example.epilepsycare;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class SettingPreference extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_preference);



    }
}
