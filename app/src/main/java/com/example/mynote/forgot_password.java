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
import com.google.firebase.auth.FirebaseAuth;

public class forgot_password extends AppCompatActivity implements View.OnClickListener {

    private EditText mforgotemail;
    private Button mrecoverbutton;
    private TextView mforgotlogin;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mforgotemail = findViewById(R.id.forgotemail);
        mrecoverbutton = findViewById(R.id.recoverbutton);
        mforgotlogin = findViewById(R.id.forgotlogin);

        mforgotemail.setOnClickListener(this);
        mrecoverbutton.setOnClickListener(this);
        mforgotlogin.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.forgotlogin) {
            startActivity(new Intent(forgot_password.this, MainActivity.class));
        } else if (v.getId() == R.id.recoverbutton) {
            recoverPassword();
        }
    }

    private void recoverPassword() {
        String mail = mforgotemail.getText().toString().trim();


        if (mail.isEmpty()) {
            mforgotemail.setError("Enter an email address");
            mforgotemail.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            mforgotemail.setError("Enter a valid email address");
            mforgotemail.requestFocus();
        } else {
            firebaseAuth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        showToast("Recovery email sent. Please check your inbox.");
                        finish();
                        startActivity(new Intent(forgot_password.this, MainActivity.class));
                    } else {
                        showToast("Account doesn't exist");
                    }
                }
            });
        }
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.show();
    }
}
