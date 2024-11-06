package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.card.MaterialCardView;

public class journal extends AppCompatActivity {

    private RecyclerView journalRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_journal);

        // Apply window insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize RecyclerView and other UI elements
        journalRecyclerView = findViewById(R.id.journalRecyclerView);
        setupRecyclerView();

        // Initialize navigation and button listeners
        setupBottomNav();
        setupAddButton();
    }

    private void setupRecyclerView() {
        // Set up RecyclerView for displaying journal entries
        // This should ideally have an adapter setup for your journal entries
        // Example: journalRecyclerView.setAdapter(new JournalAdapter(journalEntries));
        Toast.makeText(this, "RecyclerView Setup", Toast.LENGTH_SHORT).show();
    }

    private void setupBottomNav() {
        // Access the navigation items and set click listeners
        findViewById(R.id.assessment).setOnClickListener(v -> openAssessment());
        findViewById(R.id.bookIcon).setOnClickListener(v -> openJournal());
        findViewById(R.id.taskIcon).setOnClickListener(v -> openTasks());
        findViewById(R.id.account).setOnClickListener(v -> openAccount());
    }

    private void setupAddButton() {
        // Floating action button for adding new journal entries
        FloatingActionButton addIcon = findViewById(R.id.addIcon);
        addIcon.setOnClickListener(v -> openAddJournalEntry());
    }

    private void openAssessment() {
        // Navigate to Assessment screen
        Toast.makeText(this, "Opening Assessment", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, AssessmentActivity.class));
    }

    private void openJournal() {
        // Refresh or open the journal screen (or reload the journal list)
        Toast.makeText(this, "Refreshing Journal", Toast.LENGTH_SHORT).show();
        // Code to refresh journal entries can be added here
    }

    private void openTasks() {
        // Navigate to Tasks screen
        Toast.makeText(this, "Opening Tasks", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, TasksActivity.class));
    }

    private void openAccount() {
        // Navigate to Account screen
        Toast.makeText(this, "Opening Account", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, AccountActivity.class));
    }

    private void openAddJournalEntry() {
        // Navigate to Add Journal Entry screen
        Toast.makeText(this, "Add new Journal Entry", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, AddJournalEntryActivity.class));
    }
}
