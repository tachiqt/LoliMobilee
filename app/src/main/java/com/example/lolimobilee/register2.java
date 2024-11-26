package com.example.lolimobilee;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class register2 extends AppCompatActivity {

    private EditText etHobbies, etInterests, etTalents;
    private LottieAnimationView successAnimation, loadingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        etHobbies = findViewById(R.id.etHobbies);
        etInterests = findViewById(R.id.etInterests);
        etTalents = findViewById(R.id.etTalents);
        loadingAnimation = findViewById(R.id.loadingAnimation);

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
            String userId = previousIntent.getStringExtra("userId");
            String fullName = previousIntent.getStringExtra("fullName");
            String gender = previousIntent.getStringExtra("gender");
            String birthday = previousIntent.getStringExtra("birthday");
            String email = previousIntent.getStringExtra("email");
            int age = calculateAge(birthday);

            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "User ID is missing or invalid");
                Toast.makeText(this, "Error: User ID is missing", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show confirmation dialog
            showConfirmationDialog(userId, fullName, gender, birthday, email, hobbies, interests, talents, age);
        });

        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private boolean isValidInput(String input) {
        return input.matches("[a-zA-Z,\\s]+");
    }

    private int calculateAge(String birthday) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date birthDate = dateFormat.parse(birthday);
            Calendar birthDay = Calendar.getInstance();
            birthDay.setTime(birthDate);

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < birthDay.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing birthday", e);
            return 0;
        }
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


    private void showConfirmationDialog(String userId, String fullName, String gender, String birthday, String email, String hobbies, String interests, String talents, int age) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to submit?")
                .setPositiveButton("OK", (dialog, which) -> {
                    showLoadingAnimation(true);
                    saveDataToFirestore(userId, fullName, gender, birthday, email, hobbies, interests, talents, age);
                    Toast.makeText(this, "You're all set!", Toast.LENGTH_SHORT).show();
                    navigateToLogin();

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showTermsDialog(CheckBox termsCheckbox) {
        new AlertDialog.Builder(this)
                .setTitle("Terms and Conditions")
                .setMessage(getString(R.string.terms_and_conditions))
                .setPositiveButton("Agree", (dialog, which) -> termsCheckbox.setChecked(true))
                .setNegativeButton("Cancel", (dialog, which) -> termsCheckbox.setChecked(false))
                .show();
    }

    private void showPrivacyPolicyDialog(CheckBox privacyPolicyCheckbox) {
        new AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage(getString(R.string.privacy_policy))
                .setPositiveButton("Agree", (dialog, which) -> privacyPolicyCheckbox.setChecked(true))
                .setNegativeButton("Cancel", (dialog, which) -> privacyPolicyCheckbox.setChecked(false))
                .show();
    }

    private void saveDataToFirestore(String userId, String fullName, String gender, String birthday, String email, String hobbies, String interests, String talents, int age) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("gender", gender);
        user.put("birthday", birthday);
        user.put("email", email);
        user.put("hobbies", hobbies);
        user.put("interests", interests);
        user.put("talents", talents);
        user.put("age", age);

        db.collection("personal_information").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    showLoadingAnimation(false);
                })
                .addOnFailureListener(e -> {
                    showLoadingAnimation(false);
                    Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving user data", e);
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(register2.this, login.class);
        startActivity(intent);
        finish();
    }
}
