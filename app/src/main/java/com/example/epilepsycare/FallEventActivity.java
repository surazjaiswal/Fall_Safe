package com.example.epilepsycare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class FallEventActivity extends AppCompatActivity {


    private final String TAG = "FallEventActivity";

//    public ArrayList<FallEvents> fallEvents = new ArrayList<>();
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;

    RecyclerView recyclerView;
    FallAdapter fallAdapter;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_event);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();

        loadData();

        recyclerView = findViewById(R.id.fall_recycler_view);
        fallAdapter = new FallAdapter(MainActivity.fallEvents);
        recyclerView.setAdapter(fallAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fallAdapter.setOnItemClickListener(new FallAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.d(TAG, "onItemClick: ");
            }

            @Override
            public void ItemDelete(int position) {
                fallAdapter.fallEvents.remove(position);
                fallAdapter.notifyItemRemoved(position);
                saveData();
            }

            @Override
            public void ItemShare(int position) {
                String s = preferences.getString(getString(R.string.emgMessage),"Hello, can get a help at ") +"\n" + MainActivity.fallEvents.get(position).fall_location;
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_TEXT,s)
                        .setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent,null);
                startActivity(shareIntent);
            }

            @Override
            public void OnLinkClick(int position) {
                String s = MainActivity.fallEvents.get(position).fall_location;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                startActivity(intent);
            }
        });
    }

    public void saveData(){
        Gson gson = new Gson();
        String json = gson.toJson(MainActivity.fallEvents);
        editor.putString("com.example.epilepsycare.fallevent",json);
        editor.apply();
        fallAdapter.notifyDataSetChanged();
    }

    public void loadData(){
        if(MainActivity.fallEvents==null){
            MainActivity.fallEvents = new ArrayList<>();
        }
        Gson gson = new Gson();
        String json = preferences.getString("com.example.epilepsycare.fallevent",null);
        Type type = new TypeToken<ArrayList<FallEvents>>(){}.getType();
        MainActivity.fallEvents = gson.fromJson(json,type);
    }

}