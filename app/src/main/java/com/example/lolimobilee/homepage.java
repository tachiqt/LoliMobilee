package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class homepage extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "homepage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve the user ID from the Intent
        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");

        // Reference to the TextView
        TextView helloText = findViewById(R.id.helloText);

        // Fetch the user's fullname from Firestore
        if (userId != null) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullname = documentSnapshot.getString("fullname");
                            helloText.setText("Hello, " + fullname + "!");
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    })
                    .addOnFailureListener(e -> Log.d(TAG, "Error fetching document", e));
        }

        // Set up the RecyclerView for journal entries
        RecyclerView journalRecyclerView = findViewById(R.id.journalRecyclerView);
        journalRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample journal entries with title, date, and preview
        List<JournalEntry> journalEntries = new ArrayList<>();
        journalEntries.add(new JournalEntry("Reflecting on My Day", "2023-12-18", "Today was a productive day. I accomplished..."));
        journalEntries.add(new JournalEntry("Setting New Goals", "2023-12-17", "I decided to focus on improving my..."));

        // Set up the adapter with the journal entries
        JournalEntryAdapter adapter = new JournalEntryAdapter(journalEntries);
        journalRecyclerView.setAdapter(adapter);
    }
}
