package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class homepage extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "homepage";
    private List<JournalEntry> journalEntries;
    private JournalEntryAdapter adapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        TextView helloText = findViewById(R.id.helloText);

        if (userId != null) {
            Log.d(TAG, "Received userId: " + userId);
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            if (fullName != null && !fullName.isEmpty()) {
                                String firstName = fullName.split(" ")[0];
                                helloText.setText("Hi, " + firstName + "!");
                            } else {
                                helloText.setText("Hi, User!");
                            }
                        } else {
                            Log.d(TAG, "No such document");
                            helloText.setText("Hi, User!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "Error fetching document", e);
                        Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                        helloText.setText("Hi, User!");
                    });
        } else {
            Log.e(TAG, "userId is null - make sure it is passed correctly from the previous activity");
            helloText.setText("Hi, User!");
        }

        // Initialize the RecyclerView and adapter
        RecyclerView journalRecyclerView = findViewById(R.id.journalRecyclerView);
        journalRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        journalEntries = new ArrayList<>();
        adapter = new JournalEntryAdapter(this, journalEntries, userId);
        journalRecyclerView.setAdapter(adapter);

        // Load journal entries from Firestore
        loadJournalEntries();

        // Handle "Add Journal" button click
        findViewById(R.id.addJournalButton).setOnClickListener(v -> {
            Intent addIntent = new Intent(homepage.this, addjournal.class);
            addIntent.putExtra("userId", userId);
            startActivity(addIntent);
        });
    }

    private void loadJournalEntries() {
        db.collection("journalEntries")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (querySnapshot != null) {
                        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    JournalEntry newEntry = dc.getDocument().toObject(JournalEntry.class);
                                    newEntry.setId(dc.getDocument().getId()); // Set the document ID
                                    journalEntries.add(newEntry);
                                    adapter.notifyItemInserted(journalEntries.size() - 1);
                                    break;

                                case MODIFIED:
                                    JournalEntry modifiedEntry = dc.getDocument().toObject(JournalEntry.class);
                                    modifiedEntry.setId(dc.getDocument().getId()); // Set the document ID
                                    int modifiedIndex = getIndexById(dc.getDocument().getId());
                                    if (modifiedIndex != -1) {
                                        journalEntries.set(modifiedIndex, modifiedEntry);
                                        adapter.notifyItemChanged(modifiedIndex);
                                    }
                                    break;

                                case REMOVED:
                                    int removedIndex = getIndexById(dc.getDocument().getId());
                                    if (removedIndex != -1) {
                                        journalEntries.remove(removedIndex);
                                        adapter.notifyItemRemoved(removedIndex);
                                    }
                                    break;
                            }
                        }
                    }
                });
    }

    private int getIndexById(String id) {
        for (int i = 0; i < journalEntries.size(); i++) {
            if (journalEntries.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
