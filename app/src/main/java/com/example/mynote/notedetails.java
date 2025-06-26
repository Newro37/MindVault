package com.example.mynote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class notedetails extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "NoteDetailsActivity";
    private TextView mtitleofnotedetail, mcontentofnotedetail, mlastModifiedDate;
    private FloatingActionButton mgotoeditnote;
    private Intent data;
    private RelativeLayout noteDetailContainer;

    private final int[] COLOR_RESOURCES = {
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
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_notedetails);

            Toolbar toolbar = findViewById(R.id.toolbarofnotedetail);
            if (toolbar == null) throw new RuntimeException("Toolbar not found");
            setSupportActionBar(toolbar);

            noteDetailContainer = findViewById(R.id.noteDetailContainer);
            if (noteDetailContainer == null) throw new RuntimeException("Note container not found");

            mtitleofnotedetail = findViewById(R.id.titleofnotedetail);
            mcontentofnotedetail = findViewById(R.id.contentofnotedetail);
            mgotoeditnote = findViewById(R.id.gotoeditnote);
            mlastModifiedDate = findViewById(R.id.lastModifiedDate);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }

            data = getIntent();
            if (data == null) {
                throw new IllegalStateException("No intent data available");
            }

            String title = data.getStringExtra("title");
            String content = data.getStringExtra("content");
            long lastEditTime = data.getLongExtra("lastEditTime", 0);

            if (mtitleofnotedetail != null) {
                mtitleofnotedetail.setText(title != null ? title : "");
            }
            if (mcontentofnotedetail != null) {
                mcontentofnotedetail.setText(content != null ? content : "");
            }

            // Set last modified date and time
            if (mlastModifiedDate != null && lastEditTime > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()); // Reverted format
                String formattedDate = sdf.format(new Date(lastEditTime));
                mlastModifiedDate.setText("Last Modified: " + formattedDate);
            } else if (mlastModifiedDate != null) {
                mlastModifiedDate.setText("Last Modified: N/A");
            }

            int colorIndex = data.getIntExtra("colorIndex", 0);
            setNoteBackgroundColor(colorIndex);

            if (mgotoeditnote != null) {
                mgotoeditnote.setOnClickListener(this);
            }

        } catch (Exception e) {
            handleError(e, "Failed to initialize activity");
            finish();
        }
    }

    private void setNoteBackgroundColor(int colorIndex) {
        try {
            if (COLOR_RESOURCES.length == 0) {
                throw new IllegalStateException("No color resources available");
            }

            if (colorIndex < 0 || colorIndex >= COLOR_RESOURCES.length) {
                colorIndex = 0;
                Log.w(TAG, "Invalid color index, using default");
            }

            if (noteDetailContainer != null) {
                noteDetailContainer.setBackgroundColor(
                        ContextCompat.getColor(this, COLOR_RESOURCES[colorIndex])
                );
            }
        } catch (Exception e) {
            handleError(e, "Failed to set background color");
        }
    }

    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.gotoeditnote) {
                if (data == null) {
                    throw new IllegalStateException("No intent data available");
                }

                Intent intent = new Intent(this, editnoteactivity.class);
                intent.putExtra("title", data.getStringExtra("title"));
                intent.putExtra("content", data.getStringExtra("content"));
                intent.putExtra("noteid", data.getStringExtra("noteid"));
                intent.putExtra("colorIndex", data.getIntExtra("colorIndex", 0));
                intent.putExtra("lastEditTime", data.getLongExtra("lastEditTime", 0));
                startActivity(intent);
            }
        } catch (Exception e) {
            handleError(e, "Failed to handle click");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            if (item.getItemId() == android.R.id.home) {
                navigateToNotesPage();
                return true;
            }
        } catch (Exception e) {
            handleError(e, "Failed to handle options");
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onBackPressed() {
        try {
            navigateToNotesPage();
        } catch (Exception e) {
            handleError(e, "Failed to handle back press");
        }
    }

    private void handleError(Exception e, String userMessage) {
        try {
            Log.e(TAG, userMessage + ": " + e.getMessage(), e);
            showToast(userMessage);
        } catch (Exception logEx) {
            Log.e(TAG, "Error handling error: " + logEx.getMessage());
        }
    }

    private void showToast(String message) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to show toast: " + e.getMessage());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            View view = getCurrentFocus();
            if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        } catch (Exception e) {
            handleError(e, "Failed to handle touch");
        }
        return super.dispatchTouchEvent(ev);
    }
}