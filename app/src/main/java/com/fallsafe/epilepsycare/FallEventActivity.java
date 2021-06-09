package com.fallsafe.epilepsycare;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class FallEventActivity extends AppCompatActivity {


    private final String TAG = "FallEventActivity";
    public static final int ADD_NOTE_REQUEST = 77;
    public static final String EXTRA_POSITION = "com.fallsafe.epilepsycare.EXTRA_POSITION";

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

        setTitle("History");

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
                String s = preferences.getString(getString(R.string.emgMessage), "Hello, can get a help at ") + "\n" + MainActivity.fallEvents.get(position).fall_locationLink;
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_TEXT,s)
                        .setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent,null);
                startActivity(shareIntent);
            }

            @Override
            public void AddNote(int position) {
                Intent noteIntent = new Intent(FallEventActivity.this,AddNoteActivity.class);
                noteIntent.putExtra(EXTRA_POSITION,position);
                startActivityForResult(noteIntent,ADD_NOTE_REQUEST);
            }

            @Override
            public void LocateOnMap(int position) {
                String s = MainActivity.fallEvents.get(position).fall_locationLink;
                if (s != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                    startActivity(intent);
                } else {
                    Toast.makeText(FallEventActivity.this, "No Location Record", Toast.LENGTH_SHORT).show();
                }

            }

//            @Override
//            public void OnLinkClick(int position) {
//                String s = MainActivity.fallEvents.get(position).fall_location;
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
//                startActivity(intent);
//            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        saveData();
        /*if(requestCode == ADD_NOTE_REQUEST && resultCode == RESULT_OK){
            String note = data.getStringExtra(AddNoteActivity.EXTRA_NOTE);
            Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Note not Saved", Toast.LENGTH_SHORT).show();
        }*/
    }
    public void saveData(){
        Gson gson = new Gson();
        String json = gson.toJson(MainActivity.fallEvents);
        editor.putString("com.fallsafe.epilepsycare.fallevent", json);
        editor.apply();
        fallAdapter.notifyDataSetChanged();
    }

    public void loadData(){
        if(MainActivity.fallEvents==null){
            MainActivity.fallEvents = new ArrayList<>();
        }
        Gson gson = new Gson();
        String json = preferences.getString("com.fallsafe.epilepsycare.fallevent", null);
        Type type = new TypeToken<ArrayList<FallEvents>>() {
        }.getType();
        MainActivity.fallEvents = gson.fromJson(json,type);
    }

}