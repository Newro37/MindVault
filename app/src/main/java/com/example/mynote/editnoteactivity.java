package com.example.mynote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
    private static final String TAG = "EditNoteActivity";
    private Intent data;
    private EditText medittitleofnote, meditcontentofnote;
    private FloatingActionButton msaveeditnote;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser firebaseUser;
    private int originalColorIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_editnoteactivity);

            meditcontentofnote = findViewById(R.id.editcontentofnote);
            medittitleofnote = findViewById(R.id.edittitleofnote);
            msaveeditnote = findViewById(R.id.saveeditnote);

            data = getIntent();
            originalColorIndex = data.getIntExtra("colorIndex", 0);

            if (!data.hasExtra("title") || !data.hasExtra("content") || !data.hasExtra("noteid")) {
                Log.w(TAG, "Missing intent extras");
                showToast("Invalid note data");
                navigateToNotesPage();
                return;
            }

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseUser = firebaseAuth.getCurrentUser();

            if (firebaseUser == null) {
                Log.w(TAG, "User not authenticated");
                showToast("User not authenticated. Please log in.");
                navigateToMainActivity();
                return;
            }

            Toolbar toolbar = findViewById(R.id.toolbarofeditnote);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            msaveeditnote.setOnClickListener(this);

            String notetitle = data.getStringExtra("title");
            String notecontent = data.getStringExtra("content");
            medittitleofnote.setText(notetitle);
            meditcontentofnote.setText(notecontent);

            Log.d(TAG, "EditNoteActivity initialized for note ID: " + data.getStringExtra("noteid"));
        } catch (Exception e) {
            handleError(e, "Failed to initialize activity");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateToNotesPage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveeditnote) {
            try {
                String newtitle = medittitleofnote.getText().toString().trim();
                String newcontent = meditcontentofnote.getText().toString().trim();

                if (newtitle.isEmpty()) {
                    medittitleofnote.setError("Title is required");
                    medittitleofnote.requestFocus();
                    Log.w(TAG, "Empty title entered");
                    return;
                }
                if (newcontent.isEmpty()) {
                    meditcontentofnote.setError("Content cannot be empty");
                    meditcontentofnote.requestFocus();
                    Log.w(TAG, "Empty content entered");
                    return;
                }

                String noteId = data.getStringExtra("noteid");
                DocumentReference documentReference = firebaseFirestore
                        .collection("notes")
                        .document(firebaseUser.getUid())
                        .collection("myNotes")
                        .document(noteId);

                HashMap<String, Object> note = new HashMap<>();
                note.put("title", newtitle);
                note.put("content", newcontent);
                note.put("editTime", FieldValue.serverTimestamp());
                note.put("colorIndex", originalColorIndex); // Preserve original color

                Log.i(TAG, "Attempting to update note: " + noteId);
                documentReference.set(note)
                        .addOnSuccessListener(unused -> {
                            Log.i(TAG, "Note updated successfully");
                            showToast("Note updated successfully");
                            navigateToNotesPage();
                        })
                        .addOnFailureListener(e -> handleError(e, "Failed to update note"));
            } catch (Exception e) {
                handleError(e, "Error updating note");
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            View view = getCurrentFocus();
            if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            Log.e(TAG, "Error in dispatchTouchEvent: " + e.getMessage(), e);
            return false;
        }
    }

    private void navigateToNotesPage() {
        try {
            Intent intent = new Intent(this, notes_page.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            handleError(e, "Failed to navigate to notes page");
        }
    }

    private void navigateToMainActivity() {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            handleError(e, "Failed to navigate to main activity");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void handleError(Exception e, String userMessage) {
        Log.e(TAG, userMessage + ": " + e.getMessage(), e);
        showToast(userMessage);
    }
}