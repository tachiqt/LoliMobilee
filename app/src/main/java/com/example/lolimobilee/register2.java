package com.example.lolimobilee;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class register2 extends AppCompatActivity {

    private EditText etHobbies, etInterests, etTalents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        etHobbies = findViewById(R.id.etHobbies);
        etInterests = findViewById(R.id.etInterests);
        etTalents = findViewById(R.id.etTalents);
        CheckBox acceptTerms = findViewById(R.id.acceptTerms);
        CheckBox acceptPrivacyPolicy = findViewById(R.id.acceptPrivacyPolicy);

        acceptTerms.setOnClickListener(v -> showTermsDialog(acceptTerms));

        acceptPrivacyPolicy.setOnClickListener(v -> showPrivacyPolicyDialog(acceptPrivacyPolicy));

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            String hobbies = etHobbies.getText().toString().trim();
            String interests = etInterests.getText().toString().trim();
            String talents = etTalents.getText().toString().trim();

            if (!isValidInput(hobbies) || !isValidInput(interests) || !isValidInput(talents)) {
                Toast.makeText(this, "Only alphabetical characters and commas are allowed", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!acceptTerms.isChecked() || !acceptPrivacyPolicy.isChecked()) {
                Toast.makeText(this, "Please accept the terms and conditions and privacy policy", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent previousIntent = getIntent();
            String fullName = previousIntent.getStringExtra("fullName");
            String gender = previousIntent.getStringExtra("gender");
            String birthday = previousIntent.getStringExtra("birthday");
            String email = previousIntent.getStringExtra("email");
            String password = previousIntent.getStringExtra("password");

            showConfirmationDialog(fullName, gender, birthday, email, password, hobbies, interests, talents);
        });

        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private boolean isValidInput(String input) {
        return input.matches("[a-zA-Z,\\s]+");
    }

    private void showTermsDialog(CheckBox termsCheckbox) {
        new AlertDialog.Builder(this)
                .setTitle("Terms and Conditions")
                .setMessage(getString(R.string.terms_and_conditions))
                .setPositiveButton("Agree", (dialog, which) -> {
                    termsCheckbox.setChecked(true);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    termsCheckbox.setChecked(false);
                })
                .show();
    }

    private void showPrivacyPolicyDialog(CheckBox privacyPolicyCheckbox) {
        new AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage(getString(R.string.privacy_policy))
                .setPositiveButton("Agree", (dialog, which) -> {
                    privacyPolicyCheckbox.setChecked(true);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    privacyPolicyCheckbox.setChecked(false);
                })
                .show();
    }

    private void showConfirmationDialog(String fullName, String gender, String birthday, String email, String password, String hobbies, String interests, String talents) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to submit?")
                .setPositiveButton("OK", (dialog, which) -> {
                    saveDataToFirestore(fullName, gender, birthday, email, password, hobbies, interests, talents);

                    etHobbies.setText("");
                    etInterests.setText("");
                    etTalents.setText("");

                    Intent intent = new Intent(register2.this, login.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveDataToFirestore(String fullName, String gender, String birthday,String email, String password, String hobbies, String interests, String talents) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = email.replace(".", "_"); // Using email as userId (replace . with _)

        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("gender", gender);
        user.put("birthday", birthday);
        user.put("email", email);
        user.put("password", password);
        user.put("hobbies", hobbies);
        user.put("interests", interests);
        user.put("talents", talents);

        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(register2.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "User data saved in Firestore");

                    // Create the user-specific collection after confirmation
                    db.collection("users").document(userId).collection("repository") // Collection name: "repository"
                            .add(new HashMap<>()) // Add an empty document to create the collection
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(register2.this, "User collection created", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "User collection created: " + documentReference.getId());

                                // Navigate to login after creating the collection
                                navigateToLogin();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(register2.this, "Error creating collection", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error creating user collection", e);});
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(register2.this, "Error saving data", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving user data", e);
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(register2.this, login.class);
        startActivity(intent);
        finish();
    }
}
