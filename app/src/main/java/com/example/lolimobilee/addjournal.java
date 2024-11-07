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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class addjournal extends AppCompatActivity {

    private EditText journalTitleInput, journalDescriptionInput;
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore instance
    private String userId; // ID of the user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_addjournal);

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve the userId from the Intent
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        // Initialize input fields and buttons
        journalTitleInput = findViewById(R.id.journalTitleInput);
        journalDescriptionInput = findViewById(R.id.journalDescriptionInput);
        Button saveButton = findViewById(R.id.saveButton);
        ImageButton backButton = findViewById(R.id.backButton);

        // Set up the save button listener
        saveButton.setOnClickListener(v -> saveJournalEntry());

        // Set up the back button listener
        backButton.setOnClickListener(v -> finish());
    }

    private void saveJournalEntry() {
        String title = journalTitleInput.getText().toString().trim();
        String description = journalDescriptionInput.getText().toString().trim();


        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Title and description cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }


        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Prepare journal entry data
        Map<String, Object> journalEntry = new HashMap<>();
        journalEntry.put("userId", userId);
        journalEntry.put("title", title);
        journalEntry.put("date", currentDate);
        journalEntry.put("description", description);


        db.collection("journalEntries")
                .add(journalEntry)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Journal entry saved", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity and go back to the previous screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save journal entry", Toast.LENGTH_SHORT).show();
                });
    }
}
