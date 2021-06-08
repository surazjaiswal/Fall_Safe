package com.example.epilepsycare;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AddNoteActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE = "com.example.epilepsycare.EXTRA_NOTE";
    private EditText editTextNote;
    ImageView noteViewFallImage;
    private TextView tvNoteViewLocation, tvNoteViewDateTime, tvNoteViewFallStatus;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        editTextNote = findViewById(R.id.editNote);
        tvNoteViewDateTime = findViewById(R.id.noteView_datetime);
        tvNoteViewFallStatus = findViewById(R.id.noteView_fallStatus);
        tvNoteViewLocation = findViewById(R.id.noteView_location);
        noteViewFallImage = findViewById(R.id.noteView_img);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Add Note");

        Intent intent = getIntent();
        position = intent.getIntExtra(FallEventActivity.EXTRA_POSITION, 0);

        loadData(position);

        tvNoteViewLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = MainActivity.fallEvents.get(position).fall_locationLink;
                if (s != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater addNoteMenuInflater = getMenuInflater();
        addNoteMenuInflater.inflate(R.menu.add_note_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.saveNote:
                saveNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void saveNote(){
        String note = editTextNote.getText().toString().trim();
        if(!note.isEmpty()){

            /*Intent dataNote = new Intent();
            dataNote.putExtra(EXTRA_NOTE,note);
            setResult(RESULT_OK,dataNote);
            finish();*/

            saveData(position);
            finish();
            Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Note is empty", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadData(int pos) {
        editTextNote.setText(MainActivity.fallEvents.get(pos).getFall_note());
        tvNoteViewLocation.setText(MainActivity.fallEvents.get(pos).getFall_locationAddress());
        tvNoteViewDateTime.setText(MainActivity.fallEvents.get(pos).fall_date_time);
        if (MainActivity.fallEvents.get(pos).isFall()) {
            tvNoteViewFallStatus.setText("Fall Detected");
            tvNoteViewFallStatus.setTextColor(Color.RED);
            noteViewFallImage.setImageResource(R.drawable.ic_fall);
        } else {
            tvNoteViewFallStatus.setText("False Fall Detected");
            tvNoteViewFallStatus.setTextColor(Color.GREEN);
            noteViewFallImage.setImageResource(R.drawable.ic_error);
        }
    }

    public void saveData(int pos){
        MainActivity.fallEvents.get(pos).setFall_note(editTextNote.getText().toString().trim());
    }
}