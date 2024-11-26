package com.example.lolimobilee;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.HashMap;
import java.util.Map;

public class assessment extends AppCompatActivity {
    private BottomNavBar bottomNavBar;
    private String userId;

    private FirebaseFirestore db;
    private Map<String, Button> topicButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_assessment);

        // Retrieve the userId from the Intent
        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            userId = "";
            Log.e(TAG, "User ID not passed; defaulting to empty string.");
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up system insets for immersive UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        setupBottomNavigation();
        setupButtons();

        TextView helloText = findViewById(R.id.userGreeting);
        if (!userId.isEmpty()) {
            loadUserData(helloText);
        } else {
            helloText.setText("Hi, User!");
        }

        // Check completion status for topics
        checkAssessmentCompletionStatus();
    }

    private void setupBottomNavigation() {
        ImageView assessmentIcon = findViewById(R.id.assessment);
        ImageView bookIcon = findViewById(R.id.bookIcon);
        ImageView taskIcon = findViewById(R.id.taskIcon);
        ImageView accountIcon = findViewById(R.id.account);
        FloatingActionButton dashboardIcon = findViewById(R.id.dashboardIcon);

        bottomNavBar = new BottomNavBar(this, userId, assessmentIcon, bookIcon, taskIcon, accountIcon, dashboardIcon);
    }

    private void setupButtons() {
        topicButtons = new HashMap<>();

        // Map each button to its respective topic
        topicButtons.put("Conversational Skills", findViewById(R.id.startConversationalSkills));
        topicButtons.put("Listening and Empathy", findViewById(R.id.startListeningAndEmpathy));

        // Set up listeners for each button
        for (Map.Entry<String, Button> entry : topicButtons.entrySet()) {
            String topicTitle = entry.getKey();
            Button button = entry.getValue();
            button.setOnClickListener(view -> navigateToAssessmentDetails("Pre-Assessment", topicTitle));
        }
    }

    private void navigateToAssessmentDetails(String assessmentType, String topicTitle) {
        Intent intent = new Intent(this, conversationalskillspre.class);
        intent.putExtra("userId", userId);
        intent.putExtra("topicTitle", topicTitle);
        intent.putExtra("assessmentType", assessmentType);
        startActivity(intent);
    }

    private void checkAssessmentCompletionStatus() {
        db.collection("pre_assessment")
                .whereEqualTo("UserId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String topic = document.getString("Topic");
                        String status = document.getString("Status");

                        if (topic != null && "Completed".equalsIgnoreCase(status)) {
                            Button button = topicButtons.get(topic);
                            if (button != null) {
                                updateButtonToCompleted(button);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch assessment status: " + e.getMessage());
                    Toast.makeText(this, "Error fetching assessment status.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateButtonToCompleted(Button button) {
        button.setText("Completed");
        button.setEnabled(false); //
    }

    private void loadUserData(TextView helloText) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String firstName = (fullName != null && !fullName.isEmpty()) ? fullName.split(" ")[0] : "User";
                        helloText.setText("Hi, " + firstName + "!");
                    } else {
                        helloText.setText("Hi, User!");
                        Log.d(TAG, "No such document for user.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user data", e);
                    helloText.setText("Hi, User!");
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                });
    }
}
