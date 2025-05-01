package com.example.mynote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class notes_page extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "NotesPageActivity";
    private FloatingActionButton mcreatesnotefab;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView mrecyclerview;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder> noteAdapter;

    // Predefined color resources array
    private final int[] colorCodes = {
            R.color.color1,
            R.color.color2,
            R.color.color3,
            R.color.color4,
            R.color.color5,
            R.color.color6,
            R.color.color7,
            R.color.color8,
            R.color.color9,
            R.color.color10
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_notes_page);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("All Notes");
            }

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseUser = firebaseAuth.getCurrentUser();
            firebaseFirestore = FirebaseFirestore.getInstance();

            if (firebaseUser == null) {
                Log.w(TAG, "User not authenticated");
                showToast("User not authenticated. Please log in.");
                navigateToMainActivity();
                return;
            }

            mcreatesnotefab = findViewById(R.id.createnotefab);
            mcreatesnotefab.setOnClickListener(this);

            SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
            boolean newestFirst = sharedPreferences.getBoolean("NewestFirst", true);

            Query query = firebaseFirestore.collection("notes")
                    .document(firebaseUser.getUid())
                    .collection("myNotes")
                    .orderBy("editTime", newestFirst ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);

            FirestoreRecyclerOptions<firebasemodel> allUserNotes =
                    new FirestoreRecyclerOptions.Builder<firebasemodel>()
                            .setQuery(query, firebasemodel.class)
                            .build();

            noteAdapter = new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(allUserNotes) {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull firebasemodel model) {
                    try {
                        ImageView popupbutton = holder.itemView.findViewById(R.id.menupopbutton);

                        // Get stored color index and set background
                        int colorIndex = model.getColorIndex();
                        int colorCode = getFixedColor(colorIndex);
                        holder.mnote.setBackgroundColor(holder.itemView.getResources().getColor(colorCode, null));

                        holder.notetitle.setText(model.getTitle());
                        holder.notecontent.setText(model.getContent());

                        String docId = noteAdapter.getSnapshots().getSnapshot(position).getId();

                        popupbutton.setOnClickListener(v -> {
                            try {
                                PopupMenu popupmenu = new PopupMenu(v.getContext(), v);
                                popupmenu.setGravity(Gravity.END);
                                popupmenu.getMenu().add("Edit").setOnMenuItemClickListener(item -> {
                                    Intent intent = new Intent(v.getContext(), editnoteactivity.class);
                                    intent.putExtra("title", model.getTitle());
                                    intent.putExtra("content", model.getContent());
                                    intent.putExtra("noteid", docId);
                                    intent.putExtra("colorIndex", colorIndex); // Pass color index
                                    v.getContext().startActivity(intent);
                                    return true;
                                });
                                popupmenu.getMenu().add("Share").setOnMenuItemClickListener(item -> {
                                    shareNote(model.getTitle(), model.getContent());
                                    return true;
                                });
                                popupmenu.getMenu().add("Delete").setOnMenuItemClickListener(item -> {
                                    new AlertDialog.Builder(notes_page.this)
                                            .setTitle("Delete")
                                            .setMessage("Do you really want to delete?")
                                            .setPositiveButton("Yes", (dialog, which) -> {
                                                DocumentReference documentReference = firebaseFirestore
                                                        .collection("notes")
                                                        .document(firebaseUser.getUid())
                                                        .collection("myNotes")
                                                        .document(docId);
                                                Log.i(TAG, "Attempting to delete note: " + docId);
                                                documentReference.delete()
                                                        .addOnSuccessListener(unused -> {
                                                            Log.i(TAG, "Note deleted successfully");
                                                            showToast("Note deleted successfully");
                                                        })
                                                        .addOnFailureListener(e -> handleError(e, "Failed to delete note"));
                                            })
                                            .setNegativeButton("No", null)
                                            .show();
                                    return true;
                                });
                                popupmenu.show();
                            } catch (Exception e) {
                                handleError(e, "Error showing popup menu");
                            }
                        });

                        holder.itemView.setOnClickListener(v -> {
                            try {
                                Intent intent = new Intent(notes_page.this, notedetails.class);
                                intent.putExtra("title", model.getTitle());
                                intent.putExtra("content", model.getContent());
                                intent.putExtra("noteid", docId);
                                intent.putExtra("colorIndex", colorIndex); // Pass color to details
                                startActivity(intent);
                            } catch (Exception e) {
                                handleError(e, "Error opening note details");
                            }
                        });
                    } catch (Exception e) {
                        handleError(e, "Error binding note view holder");
                    }
                }

                @NonNull
                @Override
                public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    try {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout, parent, false);
                        return new NoteViewHolder(view);
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating view holder: " + e.getMessage(), e);
                        throw new RuntimeException("Failed to create view holder", e);
                    }
                }
            };

            mrecyclerview = findViewById(R.id.recyclerview);
            mrecyclerview.setHasFixedSize(true);
            staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            mrecyclerview.setLayoutManager(staggeredGridLayoutManager);
            mrecyclerview.setAdapter(noteAdapter);

            Log.d(TAG, "NotesPageActivity initialized");
        } catch (Exception e) {
            handleError(e, "Failed to initialize activity");
        }
    }

    private int getFixedColor(int colorIndex) {
        // Validate color index and return corresponding color
        if (colorIndex < 0 || colorIndex >= colorCodes.length) {
            return colorCodes[0]; // Default to first color if invalid
        }
        return colorCodes[colorIndex];
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.createnotefab) {
            try {
                Intent intent = new Intent(this, create_note.class);
                startActivity(intent);
            } catch (Exception e) {
                handleError(e, "Error opening create note activity");
            }
        }
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView notetitle;
        private final TextView notecontent;
        private final LinearLayout mnote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            notetitle = itemView.findViewById(R.id.notetitle);
            notecontent = itemView.findViewById(R.id.notecontent);
            mnote = itemView.findViewById(R.id.note);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu, menu);
            return true;
        } catch (Exception e) {
            handleError(e, "Error creating options menu");
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            try {
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Do you really want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Log.i(TAG, "User logged out");
                            firebaseAuth.signOut();
                            navigateToMainActivity();
                        })
                        .setNegativeButton("No", null)
                        .show();
            } catch (Exception e) {
                handleError(e, "Error during logout");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (noteAdapter != null) {
                noteAdapter.startListening();
                Log.d(TAG, "Firestore adapter started listening");
            }
        } catch (Exception e) {
            handleError(e, "Error starting Firestore adapter");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (noteAdapter != null) {
                noteAdapter.stopListening();
                Log.d(TAG, "Firestore adapter stopped listening");
            }
        } catch (Exception e) {
            handleError(e, "Error stopping Firestore adapter");
        }
    }

    private void shareNote(String title, String content) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Note: " + title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, content);
            startActivity(Intent.createChooser(shareIntent, "Share Note via"));
            Log.i(TAG, "Note shared: " + title);
        } catch (Exception e) {
            handleError(e, "Error sharing note");
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