package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class expandjournal extends AppCompatActivity {

    private TextView journalTitle, journalDate, journalDescription;
    private String entryId, userId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private BottomNavBar bottomNavBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_expandjournal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        journalTitle = findViewById(R.id.journalTitle);
        journalDate = findViewById(R.id.journalDate);
        journalDescription = findViewById(R.id.journalDescription);
        Button editButton = findViewById(R.id.editButton);
        Button deleteButton = findViewById(R.id.deleteButton);
        ImageButton backButton = findViewById(R.id.backButton);

        Intent intent = getIntent();
        entryId = intent.getStringExtra("entryId");
        userId = intent.getStringExtra("userId");
        String title = intent.getStringExtra("title");
        String date = intent.getStringExtra("date");
        String description = intent.getStringExtra("description");
        journalTitle.setText(title);
        journalDate.setText(date);
        journalDescription.setText(description);

        setupBottomNavigation();
        backButton.setOnClickListener(v -> finish());
        editButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(expandjournal.this, editjournal.class);
            editIntent.putExtra("entryId", entryId);
            editIntent.putExtra("userId", userId);
            editIntent.putExtra("title", title);
            editIntent.putExtra("date", date);
            editIntent.putExtra("description", description);
            startActivity(editIntent);
        });
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Journal Entry")
                .setMessage("Are you sure you want to delete this journal entry?")
                .setPositiveButton("Yes", (dialog, which) -> deleteJournalEntry())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteJournalEntry() {
        db.collection("journalEntries").document(entryId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(expandjournal.this, "Journal entry deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(expandjournal.this, "Failed to delete journal entry", Toast.LENGTH_SHORT).show();
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
