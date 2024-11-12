package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class editjournal extends AppCompatActivity {

    private EditText journalTitleInput, journalDescriptionInput;
    private Button saveButton, cancelButton;
    private String entryId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;
    private BottomNavBar bottomNavBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editjournal);

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        journalTitleInput = findViewById(R.id.journalTitleInput);
        journalDescriptionInput = findViewById(R.id.journalDescriptionInput);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        ImageButton backButton = findViewById(R.id.backButton);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        entryId = intent.getStringExtra("entryId");
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");

        // Set initial data in EditText fields
        journalTitleInput.setText(title);
        journalDescriptionInput.setText(description);

        setupBottomNavigation();

        // Set up Save button click listener
        saveButton.setOnClickListener(v -> updateJournalEntry());

        // Set up Cancel button click listener
        cancelButton.setOnClickListener(v -> finish());

        // Set up Back button click listener
        backButton.setOnClickListener(v -> finish());
    }

    private void updateJournalEntry() {
        String updatedTitle = journalTitleInput.getText().toString().trim();
        String updatedDescription = journalDescriptionInput.getText().toString().trim();

        // Validate input
        if (updatedTitle.isEmpty() || updatedDescription.isEmpty()) {
            Toast.makeText(this, "Title and description cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare data map for updating Firestore entry
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("title", updatedTitle);
        updatedData.put("description", updatedDescription);

        // Update Firestore entry
        db.collection("journalEntries").document(entryId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(editjournal.this, "Journal entry updated", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity after saving
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(editjournal.this, "Failed to update entry", Toast.LENGTH_SHORT).show();
                });
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