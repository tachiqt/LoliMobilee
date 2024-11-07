package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class editjournal extends AppCompatActivity {

    private EditText journalTitleInput, journalDescriptionInput;
    private Button saveButton, cancelButton;
    private String entryId; // Firestore document ID for the journal entry
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        // Initialize views
        journalTitleInput = findViewById(R.id.journalTitleInput);
        journalDescriptionInput = findViewById(R.id.journalDescriptionInput);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        ImageButton backButton = findViewById(R.id.backButton);

        // Retrieve data from Intent
        Intent intent = getIntent();
        entryId = intent.getStringExtra("entryId");
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");

        // Set initial data in EditText fields
        journalTitleInput.setText(title);
        journalDescriptionInput.setText(description);

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

        // Update Firestore entry
        db.collection("journalEntries").document(entryId)
                .update("title", updatedTitle, "description", updatedDescription)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(editjournal.this, "Journal entry updated", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity after saving
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(editjournal.this, "Failed to update entry", Toast.LENGTH_SHORT).show();
                });
    }
}
