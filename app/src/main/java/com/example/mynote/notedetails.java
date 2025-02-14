package com.example.mynote;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class notedetails extends AppCompatActivity implements View.OnClickListener {
    private TextView mtitleofnotedetail, mcontentofnotedetail;
    FloatingActionButton mgotoeditnote;
    private Intent data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notedetails);

        Toolbar toolbar = findViewById(R.id.toolbarofnotedetail);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mtitleofnotedetail = findViewById(R.id.titleofnotedetail);
        mcontentofnotedetail = findViewById(R.id.contentofnotedetail);
        mgotoeditnote = findViewById(R.id.gotoeditnote);

        data = getIntent();

        mgotoeditnote.setOnClickListener(this);

        mcontentofnotedetail.setText(data.getStringExtra("content"));
        mtitleofnotedetail.setText(data.getStringExtra("title"));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.gotoeditnote) {
            Intent intent = new Intent(notedetails.this, editnoteactivity.class);
            intent.putExtra("title", data.getStringExtra("title"));
            intent.putExtra("content", data.getStringExtra("content"));
            intent.putExtra("noteid", data.getStringExtra("noteid"));
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent intent=new Intent(notedetails.this, notes_page.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
