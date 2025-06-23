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

import androidx.activity.OnBackPressedCallback;
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
import java.util.Random;

public class create_note extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CreateNoteActivity";
    private EditText mcreatetitleofnote, mcreatecontentofnote;
    private FloatingActionButton msavenote;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_create_note);

            msavenote = findViewById(R.id.savenote);
            mcreatecontentofnote = findViewById(R.id.createcontentofnote);
            mcreatetitleofnote = findViewById(R.id.createtitleofnote);

            Toolbar toolbar = findViewById(R.id.toolbarofcreatenote);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> navigateToNotesPage());

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseUser = firebaseAuth.getCurrentUser();

            if (firebaseUser == null) {
                Log.w(TAG, "User not authenticated");
                showToast("User not authenticated. Please log in.");
                navigateToMainActivity();
                return;
            }

            msavenote.setOnClickListener(this);

            // ✅ Modern back press handling
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    navigateToNotesPage();
                }
            });

            Log.d(TAG, "CreateNoteActivity initialized");

        } catch (Exception e) {
            handleError(e, "Failed to initialize activity");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.savenote) {
            try {
                String title = mcreatetitleofnote.getText().toString().trim();
                String content = mcreatecontentofnote.getText().toString().trim();

                if (title.isEmpty()) {
                    mcreatetitleofnote.setError("Title is required");
                    mcreatetitleofnote.requestFocus();
                    Log.w(TAG, "Empty title entered");
                    return;
                }
                if (content.isEmpty()) {
                    mcreatecontentofnote.setError("Content cannot be empty");
                    mcreatecontentofnote.requestFocus();
                    Log.w(TAG, "Empty content entered");
                    return;
                }

                DocumentReference documentReference = firebaseFirestore
                        .collection("notes")
                        .document(firebaseUser.getUid())
                        .collection("myNotes")
                        .document();

                int colorIndex = random.nextInt(10);

                HashMap<String, Object> note = new HashMap<>();
                note.put("title", title);
                note.put("content", content);
                note.put("editTime", FieldValue.serverTimestamp());
                note.put("creationTime", FieldValue.serverTimestamp()); // Added creationTime
                note.put("colorIndex", colorIndex);

                Log.i(TAG, "Attempting to save note: " + title);
                documentReference.set(note)
                        .addOnSuccessListener(unused -> {
                            Log.i(TAG, "Note created successfully");
                            showToast("Note created successfully");
                            navigateToNotesPage();
                        })
                        .addOnFailureListener(e -> handleError(e, "Failed to create note"));
            } catch (Exception e) {
                handleError(e, "Error saving note");
            }
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