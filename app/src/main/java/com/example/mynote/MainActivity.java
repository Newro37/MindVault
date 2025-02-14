package com.example.mynote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mloginemail, mloginpassword;
    private TextView mloginregister, mloginforgotpassword;
    private Button mloginlogin;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        mloginemail = findViewById(R.id.loginemailid);
        mloginpassword = findViewById(R.id.loginpasswordid);

        mloginregister = findViewById(R.id.loginregisterid);
        mloginregister.setOnClickListener(this);

        mloginforgotpassword = findViewById(R.id.loginforgotpasswordid);
        mloginforgotpassword.setOnClickListener(this);

        mloginlogin = findViewById(R.id.loginloginid);
        mloginlogin.setOnClickListener(this);


        firebaseAuth = FirebaseAuth.getInstance();


        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            finish();
            startActivity(new Intent(MainActivity.this, notes_page.class));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.loginforgotpasswordid) {
            startActivity(new Intent(MainActivity.this, forgot_password.class));
        } else if (v.getId() == R.id.loginregisterid) {
            startActivity(new Intent(MainActivity.this, sign_up.class));
        } else if (v.getId() == R.id.loginloginid) {
            handleLogin();
        }
    }

    private void handleLogin() {
        String mail = mloginemail.getText().toString().trim();
        String pass = mloginpassword.getText().toString().trim();

        if (mail.isEmpty()) {
            mloginemail.setError("Enter an email address");
            mloginemail.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            mloginemail.setError("Enter a valid email address");
            mloginemail.requestFocus();
        } else if (pass.isEmpty()) {
            mloginpassword.setError("Enter password");
            mloginpassword.requestFocus();
        } else if (pass.length() < 8) {
            mloginpassword.setError("Password should contain at least 8 characters");
            mloginpassword.requestFocus();
        } else {
            firebaseAuth.signInWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        checkMailVerification();
                    } else {
                        String errorMessage = task.getException().getMessage();
                        if (errorMessage.contains("password is invalid")) {
                            mloginpassword.setError("Incorrect password");
                            mloginpassword.requestFocus();
                        } else if (errorMessage.contains("no user record")) {
                            showToast("No account found with this email");
                        } else {
                            showToast("Login failed: " + errorMessage);
                        }
                    }
                }
            });
        }
    }

    private void checkMailVerification() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
            showToast("Logged in");
            finish();
            startActivity(new Intent(MainActivity.this, notes_page.class));
        } else {
            showToast("Verify your email first");
            firebaseAuth.signOut();
        }
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.show();
    }
}
