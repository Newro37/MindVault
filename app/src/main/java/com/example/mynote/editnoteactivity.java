package com.example.mynote;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class editnoteactivity extends AppCompatActivity implements View.OnClickListener {
    Intent data;
    EditText medittitleofnote,meditcontentofnote;
    FloatingActionButton msaveeditnote;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editnoteactivity);

        meditcontentofnote=findViewById(R.id.editcontentofnote);
        medittitleofnote=findViewById(R.id.edittitleofnote);
        msaveeditnote=findViewById(R.id.saveeditnote);

        data=getIntent();

        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

        Toolbar toolbar=findViewById(R.id.toolbarofeditnote);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        msaveeditnote.setOnClickListener(this);

        String notetitle=data.getStringExtra("title");
        String notecontent=data.getStringExtra("content");
        medittitleofnote.setText(notetitle);
        meditcontentofnote.setText(notecontent);

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.saveeditnote){
            String newtitle = medittitleofnote.getText().toString().trim();
            String newcontent = meditcontentofnote.getText().toString().trim();

            if(newtitle.isEmpty() || newcontent.isEmpty()){
                Toast.makeText(getApplicationContext(),"Something is empty",Toast.LENGTH_SHORT).show();
                return;
            }else{
                DocumentReference documentReference=firebaseFirestore.collection("notes")
                        .document(firebaseUser.getUid())
                        .collection("myNotes")
                        .document(data.getStringExtra("noteid"));
                HashMap<String,Object>note=new HashMap<>();
                note.put("title",newtitle);
                note.put("content",newcontent);
                note.put("editTime", FieldValue.serverTimestamp());
                documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(),"Note is updated",Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(editnoteactivity.this, notes_page.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Failed to update",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

}