package com.example.epilepsycare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class AddNoteActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE = "com.example.epilepsycare.EXTRA_NOTE";
    private EditText editTextNote;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        editTextNote = findViewById(R.id.editNote);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Add Note");

        Intent intent = getIntent();
        position = intent.getIntExtra(FallEventActivity.EXTRA_POSITION,0);

        loadData(position);

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

    public void loadData(int pos){
        editTextNote.setText(MainActivity.fallEvents.get(pos).getFall_note());
    }

    public void saveData(int pos){
        MainActivity.fallEvents.get(pos).setFall_note(editTextNote.getText().toString().trim());
    }
}