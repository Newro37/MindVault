package com.example.mynote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class sign_up extends AppCompatActivity implements View.OnClickListener {

    private EditText msignupemail, msignuppassword, msignupname;
    private Button msignupbutton;
    private TextView msignuplogin;
    private ProgressBar msignupprogressbar;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        msignuplogin = findViewById(R.id.signuplogin);
        msignuplogin.setOnClickListener(this);

        msignupbutton = findViewById(R.id.signupbutton);
        msignupbutton.setOnClickListener(this);

        msignupemail = findViewById(R.id.signupemail);
        msignuppassword = findViewById(R.id.signuppassword);
        msignupprogressbar = findViewById(R.id.signupprogressbar);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.signuplogin) {
            Intent intent = new Intent(sign_up.this, MainActivity.class);
            startActivity(intent);
        }

        if (v.getId() == R.id.signupbutton) {
            String mail = msignupemail.getText().toString().trim();
            String pass = msignuppassword.getText().toString().trim();

            if (mail.isEmpty()) {
                msignupemail.setError("Enter an email address");
                msignupemail.requestFocus();
                return;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                msignupemail.setError("Enter a valid email address");
                msignupemail.requestFocus();
                return;
            } else if (pass.isEmpty()) {
                msignuppassword.setError("Enter password");
                msignuppassword.requestFocus();
                return;
            } else if (pass.length() < 8) {
                msignuppassword.setError("Password should contain at least 8 characters");
                msignuppassword.requestFocus();
                return;
            } else {
                msignupprogressbar.setVisibility(View.VISIBLE);

                firebaseAuth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        msignupprogressbar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.BOTTOM, 0, 100);
                            toast.show();

                            sendEmailVerification();
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Failed to register", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.BOTTOM, 0, 100);
                            toast.show();
                        }
                    }
                });
            }
        }
    }

    private void sendEmailVerification() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Verification email sent", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM, 0, 100);
                        toast.show();
                        firebaseAuth.signOut();
                        finish();
                        startActivity(new Intent(sign_up.this, MainActivity.class));
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Failed to send verification email", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM, 0, 100);
                        toast.show();
                    }
                }
            });
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Failed to send verification email", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0, 100);
            toast.show();
        }
    }
}
