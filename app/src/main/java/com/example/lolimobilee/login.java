package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private LottieAnimationView loadingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        // Set up window insets for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        EditText emailEditText = findViewById(R.id.editUsername);
        EditText passwordEditText = findViewById(R.id.editPassword);
        Button signInButton = findViewById(R.id.btnSignIn);
        TextView registernav = findViewById(R.id.registernav);
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        loadingAnimation = findViewById(R.id.loadingAnimation); // Initialize loading animation

        // Hide animation initially
        loadingAnimation.setVisibility(View.GONE);

        // Sign-in button click listener
        signInButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading animation and start sign-in process
            showLoadingAnimation(true);
            signInWithEmailAndPassword(email, password);
        });

        // Register navigation click listener
        registernav.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, Lolint.class);
            startActivity(intent);
        });

        // Forgot password click listener
        forgotPassword.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email to reset password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send password reset email
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Failed to send reset email: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void showLoadingAnimation(boolean show) {
        if (loadingAnimation == null) return;
        if (show) {
            loadingAnimation.setVisibility(View.VISIBLE);
            loadingAnimation.playAnimation();
        } else {
            loadingAnimation.pauseAnimation();
            loadingAnimation.setVisibility(View.GONE);
        }
    }

    private void signInWithEmailAndPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            retrieveUserDataFromFirestore(user.getUid());
                        } else {
                            showLoadingAnimation(false);
                            Toast.makeText(this, "User not found. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showLoadingAnimation(false);
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Authentication failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void retrieveUserDataFromFirestore(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    showLoadingAnimation(false); // Hide animation after data retrieval
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String fullname = task.getResult().getString("fullName");
                        String email = task.getResult().getString("email");

                        // Navigate to homepage
                        Intent intent = new Intent(login.this, homepage.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("fullname", fullname);
                        intent.putExtra("email", email);
                        startActivity(intent);

                        Toast.makeText(this, "Welcome, " + fullname, Toast.LENGTH_SHORT).show();
                    } else if (task.isSuccessful()) {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Error retrieving data: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
