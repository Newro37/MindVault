package com.example.mynote;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    private EditText mLoginEmail, mLoginPassword;
    private CheckBox mShowPasswordCheckbox;
    private TextView mShowPasswordText, mLoginRegister, mLoginForgotPassword;
    private Button mLoginButton;
    private ProgressBar mProgressBar;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        mLoginEmail = findViewById(R.id.loginemailid);
        mLoginPassword = findViewById(R.id.loginpasswordid);
        mShowPasswordCheckbox = findViewById(R.id.showPasswordCheckbox);
        mShowPasswordText = findViewById(R.id.showPasswordText);
        mLoginButton = findViewById(R.id.loginloginid);
        mLoginRegister = findViewById(R.id.loginregisterid);
        mLoginForgotPassword = findViewById(R.id.loginforgotpasswordid);
        mProgressBar = findViewById(R.id.loginprogressid);

        // Set click listeners
        mLoginButton.setOnClickListener(this);
        mLoginRegister.setOnClickListener(this);
        mLoginForgotPassword.setOnClickListener(this);
        mShowPasswordCheckbox.setOnClickListener(this);
        mShowPasswordText.setOnClickListener(this);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            startActivity(new Intent(this, notes_page.class));
            finish();
        }
    }

    @Override
    public void onClick(View v) {
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
    }

    private void handleLogin() {
        String email = mLoginEmail.getText().toString().trim();
        String password = mLoginPassword.getText().toString().trim();

        if (email.isEmpty()) {
            mLoginEmail.setError("Email is required");
            mLoginEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            mLoginPassword.setError("Password is required");
            mLoginPassword.requestFocus();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        mLoginButton.setEnabled(false);

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    mProgressBar.setVisibility(View.GONE);
                    mLoginButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        checkEmailVerification();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkEmailVerification() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null && user.isEmailVerified()) {
            startActivity(new Intent(this, notes_page.class));
            finish();
        } else {
            Toast.makeText(this, "Please verify your email address", Toast.LENGTH_SHORT).show();
            mFirebaseAuth.signOut();
        }
    }

    private void togglePasswordVisibility() {
        if (mShowPasswordCheckbox.isChecked()) {
            mLoginPassword.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            mLoginPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        mLoginPassword.setSelection(mLoginPassword.getText().length()); // Move cursor to end
    }
}