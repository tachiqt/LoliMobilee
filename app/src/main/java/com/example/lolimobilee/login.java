package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class login extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText emailEditText = findViewById(R.id.editUsername);
        EditText passwordEditText = findViewById(R.id.editPassword);
        Button signInButton = findViewById(R.id.btnSignIn);
        TextView registernav = findViewById(R.id.registernav);
        TextView forgotPassword = findViewById(R.id.forgotPassword);

        signInButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            signInWithEmailAndPassword(email, password);
        });

        registernav.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, Lolint.class);
            startActivity(intent);
        });

        forgotPassword.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email to reset password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void signInWithEmailAndPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            retrieveUserDataFromFirestore(user.getUid());
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void retrieveUserDataFromFirestore(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            String fullname = task.getResult().getString("fullName");
                            String email = task.getResult().getString("email");

                            Intent intent = new Intent(login.this, homepage.class);
                            intent.putExtra("userId", userId);
                            intent.putExtra("fullname", fullname);
                            intent.putExtra("email", email);
                            startActivity(intent);

                            Toast.makeText(this, "Welcome, " + fullname, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error retrieving data", Toast.LENGTH_SHORT).show();
                        if (task.getException() != null) {
                            task.getException().printStackTrace();
                        }
                    }
                });
    }
}
