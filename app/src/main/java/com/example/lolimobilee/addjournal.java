package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class addjournal extends AppCompatActivity {

    private EditText journalTitleInput, journalDescriptionInput;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;
    private BottomNavBar bottomNavBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addjournal);

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve userId from Intent
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        // Validate userId
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, login.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        journalTitleInput = findViewById(R.id.journalTitleInput);
        journalDescriptionInput = findViewById(R.id.journalDescriptionInput);
        Button saveButton = findViewById(R.id.saveButton);
        ImageButton backButton = findViewById(R.id.backButton);

        setupBottomNavigation();

        // Save Button Click Listener
        saveButton.setOnClickListener(v -> saveJournalEntry());

        // Back Button Click Listener
        backButton.setOnClickListener(v -> finish());
    }

    private void setupBottomNavigation() {
        ImageView assessmentIcon = findViewById(R.id.assessment);
        ImageView bookIcon = findViewById(R.id.bookIcon);
        ImageView taskIcon = findViewById(R.id.taskIcon);
        ImageView accountIcon = findViewById(R.id.account);
        FloatingActionButton dashboardIcon = findViewById(R.id.dashboardIcon);

        bottomNavBar = new BottomNavBar(this, userId, assessmentIcon, bookIcon, taskIcon, accountIcon, dashboardIcon);
    }

    private void saveJournalEntry() {
        String title = journalTitleInput.getText().toString().trim();
        String description = journalDescriptionInput.getText().toString().trim();

        // Validate title and description
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Title and description cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String entryId = UUID.randomUUID().toString();

        // Create a journal entry map
        Map<String, Object> journalEntry = new HashMap<>();
        journalEntry.put("userId", userId);
        journalEntry.put("entryId", entryId);
        journalEntry.put("title", title);
        journalEntry.put("date", currentDate);
        journalEntry.put("description", description);
        journalEntry.put("status", "created");

        // Save the journal entry to Firestore
        db.collection("journalEntries")
                .add(journalEntry)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Journal entry saved", Toast.LENGTH_SHORT).show();

                    // Navigate back to homepage
                    Intent intent = new Intent(addjournal.this, homepage.class);
                    intent.putExtra("userId", userId); // Pass the userId to homepage
                    startActivity(intent);
                    finish(); // Close the addjournal activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save journal entry", Toast.LENGTH_SHORT).show();
                });
    }
}
