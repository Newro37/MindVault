package com.example.mynote;

import android.content.Intent;
import android.os.Bundle;
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

public class sign_up extends AppCompatActivity implements View.OnClickListener {

    private EditText mSignUpEmail, mSignUpPassword, mConfirmPassword;
    private CheckBox mShowPasswordCheckbox;
    private TextView mShowPasswordText, mLoginPrompt, mPasswordError;
    private Button mSignUpButton;
    private ProgressBar mProgressBar;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mSignUpEmail = findViewById(R.id.signupemail);
        mSignUpPassword = findViewById(R.id.signuppassword);
        mConfirmPassword = findViewById(R.id.confirmpassword);
        mShowPasswordCheckbox = findViewById(R.id.showPasswordCheckbox);
        mShowPasswordText = findViewById(R.id.showPasswordText);
        mSignUpButton = findViewById(R.id.signupbutton);
        mLoginPrompt = findViewById(R.id.signuplogin);
        mProgressBar = findViewById(R.id.signupprogressbar);
        mPasswordError = findViewById(R.id.passwordError);

        mSignUpButton.setOnClickListener(this);
        mLoginPrompt.setOnClickListener(this);
        mShowPasswordCheckbox.setOnClickListener(this);
        mShowPasswordText.setOnClickListener(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.signupbutton) {
            handleSignUp();
        } else if (id == R.id.signuplogin) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (id == R.id.showPasswordCheckbox || id == R.id.showPasswordText) {
            togglePasswordVisibility();
        }
    }

    private void handleSignUp() {
        String email = mSignUpEmail.getText().toString().trim();
        String password = mSignUpPassword.getText().toString().trim();
        String confirmPassword = mConfirmPassword.getText().toString().trim();

        if (email.isEmpty()) {
            mSignUpEmail.setError("Email is required");
            mSignUpEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            mSignUpPassword.setError("Password is required");
            mSignUpPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            mConfirmPassword.setError("Confirm password is required");
            mConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            mPasswordError.setVisibility(View.VISIBLE);
            mPasswordError.setText("Passwords do not match");
            return;
        } else {
            mPasswordError.setVisibility(View.GONE);
        }

        if (password.length() < 8) {
            mSignUpPassword.setError("Password must be at least 8 characters");
            mSignUpPassword.requestFocus();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        mSignUpButton.setEnabled(false);

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    mProgressBar.setVisibility(View.GONE);
                    mSignUpButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        sendEmailVerification();
                    } else {
                        Toast.makeText(this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendEmailVerification() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void togglePasswordVisibility() {
        if (mShowPasswordCheckbox.isChecked()) {
            mSignUpPassword.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            mConfirmPassword.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            mSignUpPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mConfirmPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        mSignUpPassword.setSelection(mSignUpPassword.getText().length());
        mConfirmPassword.setSelection(mConfirmPassword.getText().length());
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}