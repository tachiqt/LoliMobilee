package com.example.lolimobilee;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText usernameEditText = findViewById(R.id.editUsername);
        EditText passwordEditText = findViewById(R.id.editPassword);
        Button signInButton = findViewById(R.id.btnSignIn);
        TextView registernav = findViewById(R.id.registernav);

        signInButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }
            retrieveDataFromFirestore(username, password);
        });

        registernav.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, Lolint.class);
            startActivity(intent);
        });
    }

    private void retrieveDataFromFirestore(String email, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String storedPassword = document.getString("password");

                            if (storedPassword != null && storedPassword.equals(password)) {
                                String fullname = document.getString("fullname");
                                Intent intent = new Intent(login.this, homepage.class);
                                intent.putExtra("fullname", fullname);
                                intent.putExtra("email", email);
                                startActivity(intent);

                                Toast.makeText(this, "Welcome, " + fullname, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "No account found with that email", Toast.LENGTH_SHORT).show();
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
