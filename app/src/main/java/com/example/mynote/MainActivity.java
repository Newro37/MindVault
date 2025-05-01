package com.example.mynote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private EditText mLoginEmail, mLoginPassword;
    private CheckBox mShowPasswordCheckbox;
    private TextView mShowPasswordText, mLoginRegister, mLoginForgotPassword;
    private Button mLoginButton;
    private ProgressBar mProgressBar;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            initializeViews();
            setupClickListeners();

            mFirebaseAuth = FirebaseAuth.getInstance();
            checkCurrentUser();

            Log.d(TAG, "MainActivity initialized");
        } catch (Exception e) {
            handleError(e, "Failed to initialize activity");
            finish(); // Close activity if initialization fails
        }
    }

    private void initializeViews() {
        mLoginEmail = findViewById(R.id.loginemailid);
        mLoginPassword = findViewById(R.id.loginpasswordid);
        mShowPasswordCheckbox = findViewById(R.id.showPasswordCheckbox);
        mShowPasswordText = findViewById(R.id.showPasswordText);
        mLoginButton = findViewById(R.id.loginloginid);
        mLoginRegister = findViewById(R.id.loginregisterid);
        mLoginForgotPassword = findViewById(R.id.loginforgotpasswordid);
        mProgressBar = findViewById(R.id.loginprogressid);

        if (mLoginEmail == null || mLoginPassword == null || mLoginButton == null) {
            throw new IllegalStateException("Critical views not found in layout");
        }
    }

    private void setupClickListeners() {
        mLoginButton.setOnClickListener(this);
        mLoginRegister.setOnClickListener(this);
        mLoginForgotPassword.setOnClickListener(this);
        mShowPasswordCheckbox.setOnClickListener(this);
        mShowPasswordText.setOnClickListener(this);
    }

    private void checkCurrentUser() {
        try {
            FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
            if (currentUser != null && currentUser.isEmailVerified()) {
                Log.i(TAG, "User already logged in: " + currentUser.getEmail());
                navigateToNotesPage();
            }
        } catch (Exception e) {
            handleError(e, "Error checking current user");
        }
    }

    @Override
    public void onClick(View v) {
        try {
            int id = v.getId();
            if (id == R.id.loginforgotpasswordid) {
                startActivity(new Intent(this, forgot_password.class));
            } else if (id == R.id.loginregisterid) {
                startActivity(new Intent(this, sign_up.class));
            } else if (id == R.id.loginloginid) {
                handleLogin();
            } else if (id == R.id.showPasswordCheckbox || id == R.id.showPasswordText) {
                togglePasswordVisibility();
            }
        } catch (Exception e) {
            handleError(e, "Error handling click event");
        }
    }

    private void handleLogin() {
        try {
            String email = mLoginEmail.getText().toString().trim();
            String password = mLoginPassword.getText().toString().trim();

            if (!validateLoginInputs(email, password)) {
                return;
            }

            mProgressBar.setVisibility(View.VISIBLE);
            mLoginButton.setEnabled(false);
            Log.i(TAG, "Attempting login for: " + email);

            mFirebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        mProgressBar.setVisibility(View.GONE);
                        mLoginButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            checkEmailVerification();
                        } else {
                            String errorMsg = task.getException() != null ?
                                    task.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Login failed: " + errorMsg);
                            showToast("Login failed: " + (errorMsg != null ?
                                    errorMsg.substring(errorMsg.lastIndexOf(" ") + 1) : "Unknown error"));
                        }
                    });
        } catch (Exception e) {
            handleError(e, "Error during login");
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            if (mLoginButton != null) mLoginButton.setEnabled(true);
        }
    }

    private boolean validateLoginInputs(String email, String password) {
        if (email.isEmpty()) {
            mLoginEmail.setError("Email is required");
            mLoginEmail.requestFocus();
            Log.w(TAG, "Empty email entered");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mLoginEmail.setError("Enter a valid email address");
            mLoginEmail.requestFocus();
            Log.w(TAG, "Invalid email format: " + email);
            return false;
        }

        if (password.isEmpty()) {
            mLoginPassword.setError("Password is required");
            mLoginPassword.requestFocus();
            Log.w(TAG, "Empty password entered");
            return false;
        }

        if (password.length() < 8) {
            mLoginPassword.setError("Password must be at least 8 characters");
            mLoginPassword.requestFocus();
            Log.w(TAG, "Password too short");
            return false;
        }

        return true;
    }

    private void checkEmailVerification() {
        try {
            FirebaseUser user = mFirebaseAuth.getCurrentUser();
            if (user == null) {
                showToast("User not found");
                return;
            }

            if (user.isEmailVerified()) {
                Log.i(TAG, "Email verified for: " + user.getEmail());
                navigateToNotesPage();
            } else {
                Log.w(TAG, "Email not verified for: " + user.getEmail());
                showToast("Please verify your email address");
                mFirebaseAuth.signOut();
            }
        } catch (Exception e) {
            handleError(e, "Error checking email verification");
        }
    }

    private void togglePasswordVisibility() {
        try {
            if (mShowPasswordCheckbox == null || mLoginPassword == null) return;

            if (mShowPasswordCheckbox.isChecked()) {
                mLoginPassword.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                mLoginPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            mLoginPassword.setSelection(mLoginPassword.getText().length());
            Log.d(TAG, "Password visibility toggled");
        } catch (Exception e) {
            handleError(e, "Error toggling password visibility");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            View view = getCurrentFocus();
            if (view != null && (ev.getAction() == MotionEvent.ACTION_UP ||
                    ev.getAction() == MotionEvent.ACTION_MOVE)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
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

    private void showToast(String message) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast: " + e.getMessage());
        }
    }

    private void handleError(Exception e, String userMessage) {
        Log.e(TAG, userMessage + ": " + e.getMessage(), e);
        showToast(userMessage);
    }
}