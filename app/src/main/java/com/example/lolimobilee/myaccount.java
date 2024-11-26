package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class myaccount extends AppCompatActivity {

    private static final String TAG = "myaccount";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private String userId;
    private List<Task> tasks = new ArrayList<>(); // Initialize the tasks list

    private ProgressBar incompleteTaskProgressBar;
    private ProgressBar completeTaskProgressBar;

    private TextView usernameTextView, birthdateTextView, hobbiesTextView, interestsTextView, talentsTextView;
    private EditText updateBirthdate, updateHobbies, updateInterests, updateTalents;
    private MaterialButton saveButton, logoutButton, userLogButton;
    private BottomNavBar bottomNavBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myaccount);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Get user ID from intent
        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID not provided. Cannot retrieve data.");
            Toast.makeText(this, "Error: User ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind UI elements
        usernameTextView = findViewById(R.id.username);
        birthdateTextView = findViewById(R.id.birthdate);
        hobbiesTextView = findViewById(R.id.hobbies);
        interestsTextView = findViewById(R.id.interests);
        talentsTextView = findViewById(R.id.talents);

        incompleteTaskProgressBar = findViewById(R.id.incompleteTaskProgressBar);
        completeTaskProgressBar = findViewById(R.id.completeTaskProgressBar);

        updateBirthdate = findViewById(R.id.updateBirthdate);
        updateHobbies = findViewById(R.id.updateHobbies);
        updateInterests = findViewById(R.id.updateInterests);
        updateTalents = findViewById(R.id.updateTalents);

        saveButton = findViewById(R.id.saveHobbiesInterestsTalentsButton);
        logoutButton = findViewById(R.id.logoutButton);
        userLogButton = findViewById(R.id.userLogButton);

        // Load user info and task progress
        loadUserInfo();
        loadTaskProgress();
        setupBottomNavigation();

        // Set up button listeners
        saveButton.setOnClickListener(v -> updateUserInfo());
        logoutButton.setOnClickListener(v -> logoutUser());
        userLogButton.setOnClickListener(v -> navigateToUserLog());
    }

    private void loadUserInfo() {
        db.collection("personal_information").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String birthday = documentSnapshot.getString("birthday");
                        String hobbies = documentSnapshot.getString("hobbies");
                        String interests = documentSnapshot.getString("interests");
                        String talents = documentSnapshot.getString("talents");

                        usernameTextView.setText(fullName != null ? fullName : "N/A");
                        birthdateTextView.setText(birthday != null ? birthday : "N/A");
                        hobbiesTextView.setText(hobbies != null ? hobbies : "N/A");
                        interestsTextView.setText(interests != null ? interests : "N/A");
                        talentsTextView.setText(talents != null ? talents : "N/A");
                    } else {
                        Log.e(TAG, "No user data found for the provided user ID.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading user info", e));
    }

    private void loadTaskProgress() {
        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    if (querySnapshot != null) {
                        tasks.clear(); // Clear the list before adding new tasks
                        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                            Task task = dc.getDocument().toObject(Task.class);
                            task.setId(dc.getDocument().getId());
                            tasks.add(task);
                        }
                        updateProgressBars();
                    }
                });
    }

    private void updateProgressBars() {
        int totalTasks = tasks.size();
        int completedTasks = 0;
        int incompleteTasks = 0;

        for (Task task : tasks) {
            if (task.isComplete()) {
                completedTasks++;
            } else {
                incompleteTasks++;
            }
        }

        int incompleteProgress = totalTasks > 0 ? (incompleteTasks * 100) / totalTasks : 0;
        int completeProgress = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;

        incompleteTaskProgressBar.setProgress(incompleteProgress);
        completeTaskProgressBar.setProgress(completeProgress);
    }

    private void updateUserInfo() {
        // Get the updated information from EditText fields
        String newBirthdate = updateBirthdate.getText().toString().trim();
        String newHobbies = updateHobbies.getText().toString().trim();
        String newInterests = updateInterests.getText().toString().trim();
        String newTalents = updateTalents.getText().toString().trim();

        // Prepare data to update Firestore
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("birthday", newBirthdate);
        updatedData.put("hobbies", newHobbies);
        updatedData.put("interests", newInterests);
        updatedData.put("talents", newTalents);

        db.collection("personal_information").document(userId).update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(myaccount.this, "Information updated successfully", Toast.LENGTH_SHORT).show();
                    loadUserInfo(); // Refresh the displayed data
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user info", e);
                    Toast.makeText(myaccount.this, "Failed to update information", Toast.LENGTH_SHORT).show();
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(myaccount.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    private void navigateToUserLog() {
        Intent intent = new Intent(myaccount.this, userlog.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(myaccount.this, login.class);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {

        ImageView assessmentIcon = findViewById(R.id.assessment);
        ImageView bookIcon = findViewById(R.id.bookIcon);
        ImageView taskIcon = findViewById(R.id.taskIcon);
        ImageView accountIcon = findViewById(R.id.account);
        FloatingActionButton dashboardIcon = findViewById(R.id.dashboardIcon);


        bottomNavBar = new BottomNavBar(this, userId, assessmentIcon, bookIcon, taskIcon, accountIcon, dashboardIcon);
    }
}
