package com.example.mynote;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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

            MenuItem emailHeader = menu.findItem(R.id.menu_email_header);
            if (emailHeader != null && firebaseUser != null && firebaseUser.getEmail() != null) {
                emailHeader.setTitle(firebaseUser.getEmail());
                emailHeader.setEnabled(false);
                emailHeader.setCheckable(false);
            } else if (emailHeader != null) {
                emailHeader.setTitle("User Not Logged In");
                emailHeader.setEnabled(false);
                emailHeader.setCheckable(false);
            }
            return true;
        } catch (Exception e) {
            handleError(e, "Error creating options menu");
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_email_header) {
                return true;
            } else if (itemId == R.id.logout) {
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
                return true;
            } else if (itemId == R.id.change_password) {
                handleChangePassword();
                return true;
            } else if (itemId == R.id.delete_account) {
                handleDeleteAccount();
                return true;
            }
        } catch (Exception e) {
            handleError(e, "Error handling options menu item selected");
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleChangePassword() {
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            showToast("User not logged in or email not available.");
            navigateToMainActivity();
            return;
        }

        // Step 1: Prompt for old password
        AlertDialog.Builder oldPasswordBuilder = new AlertDialog.Builder(this);
        oldPasswordBuilder.setTitle("Confirm Current Password");

        final EditText oldPasswordInput = new EditText(this);
        oldPasswordInput.setHint("Enter Current Password");
        oldPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        oldPasswordBuilder.setView(oldPasswordInput);

        oldPasswordBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String oldPassword = oldPasswordInput.getText().toString().trim();
                if (oldPassword.isEmpty()) {
                    showToast("Current password cannot be empty.");
                    return;
                }

                // Reauthenticate user
                AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), oldPassword);
                firebaseUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User re-authenticated successfully.");
                                    // Step 2: Prompt for new password
                                    promptForNewPassword();
                                } else {
                                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                    Log.e(TAG, "Reauthentication failed: " + errorMessage);
                                    showToast("Incorrect old password or reauthentication failed: " + errorMessage);
                                    if (errorMessage != null && errorMessage.contains("REQUIRES_RECENT_LOGIN")) {
                                        showToast("Please log in again to change password.");
                                        firebaseAuth.signOut();
                                        navigateToMainActivity();
                                    }
                                }
                            }
                        });
            }
        });
        oldPasswordBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        oldPasswordBuilder.show();
    }

    private void promptForNewPassword() {
        AlertDialog.Builder newPasswordBuilder = new AlertDialog.Builder(this);
        newPasswordBuilder.setTitle("Enter New Password");

        final EditText newPasswordInput = new EditText(this);
        newPasswordInput.setHint("Enter New Password");
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        final EditText confirmPasswordInput = new EditText(this);
        confirmPasswordInput.setHint("Confirm New Password");
        confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(newPasswordInput);
        layout.addView(confirmPasswordInput);
        newPasswordBuilder.setView(layout);

        newPasswordBuilder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPassword = newPasswordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    showToast("Password fields cannot be empty.");
                    return;
                }
                if (!newPassword.equals(confirmPassword)) {
                    showToast("Passwords do not match.");
                    return;
                }
                if (newPassword.length() < 6) {
                    showToast("Password must be at least 6 characters long.");
                    return;
                }

                firebaseUser.updatePassword(newPassword)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User password updated.");
                                    showToast("Password changed successfully.");
                                } else {
                                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                    Log.e(TAG, "Error changing password: " + errorMessage);
                                    showToast("Failed to change password: " + errorMessage);
                                    if (errorMessage != null && errorMessage.contains("REQUIRES_RECENT_LOGIN")) {
                                        showToast("Please log in again. Session expired.");
                                        firebaseAuth.signOut();
                                        navigateToMainActivity();
                                    }
                                }
                            }
                        });
            }
        });
        newPasswordBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        newPasswordBuilder.show();
    }


    private void handleDeleteAccount() {
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            showToast("User not logged in or email not available.");
            navigateToMainActivity();
            return;
        }

        // First Confirmation: "Do you really want to delete your account?"
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Do you really want to delete your account?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If confirmed, then proceed to password confirmation
                        promptForPasswordAndExecuteDeletion();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void promptForPasswordAndExecuteDeletion() {
        AlertDialog.Builder passwordConfirmBuilder = new AlertDialog.Builder(this);
        passwordConfirmBuilder.setTitle("Confirm Current Password");
        passwordConfirmBuilder.setMessage("Please enter your current password to confirm account deletion.");

        final EditText currentPasswordInput = new EditText(this);
        currentPasswordInput.setHint("Current Password");
        currentPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordConfirmBuilder.setView(currentPasswordInput);

        passwordConfirmBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String currentPassword = currentPasswordInput.getText().toString().trim();
                if (currentPassword.isEmpty()) {
                    showToast("Password cannot be empty.");
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);
                firebaseUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User re-authenticated for deletion successfully.");
                                    // Final step: Execute deletion after successful re-authentication
                                    executeAccountDeletion();
                                } else {
                                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                    Log.e(TAG, "Reauthentication failed for deletion: " + errorMessage);
                                    showToast("Incorrect password or reauthentication failed: " + errorMessage);
                                    if (errorMessage != null && errorMessage.contains("REQUIRES_RECENT_LOGIN")) {
                                        showToast("Please log in again to delete your account. Session expired.");
                                        firebaseAuth.signOut();
                                        navigateToMainActivity();
                                    }
                                }
                            }
                        });
            }
        });
        passwordConfirmBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        passwordConfirmBuilder.show();
    }

    private void executeAccountDeletion() {
        new AlertDialog.Builder(this)
                .setTitle("Final Confirmation")
                .setMessage("Are you absolutely sure? This action cannot be undone and all your notes will be lost permanently.")
                .setPositiveButton("Yes, Delete Permanently", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseUser.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User account and associated data deleted.");
                                            // Optional: Also delete user-specific data from Firestore here if stored under their UID
                                            showToast("Account and all notes deleted successfully.");
                                            navigateToMainActivity();
                                        } else {
                                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                            Log.e(TAG, "Failed to delete account: " + errorMessage);
                                            showToast("Failed to delete account: " + errorMessage);
                                            // Fallback for REQUIRES_RECENT_LOGIN if it somehow occurs after reauthentication
                                            if (errorMessage != null && errorMessage.contains("REQUIRES_RECENT_LOGIN")) {
                                                showToast("Please log in again. Session expired.");
                                                firebaseAuth.signOut();
                                                navigateToMainActivity();
                                            }
                                        }
                                    }
                                });
                    }
                })
                .setNegativeButton("No, Cancel", null)
                .show();
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
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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