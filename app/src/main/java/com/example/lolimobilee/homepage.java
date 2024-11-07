package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

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

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");


        TextView helloText = findViewById(R.id.helloText);

        if (userId != null) {
            Log.d(TAG, "Received userId: " + userId);

            // Fetch user details from Firestore
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

        // Set up the RecyclerView for journal entries
        RecyclerView journalRecyclerView = findViewById(R.id.journalRecyclerView);
        journalRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample data for journal entries
        List<JournalEntry> journalEntries = new ArrayList<>();
        journalEntries.add(new JournalEntry("Reflecting on My Day", "2023-12-18", "Today was a productive day. I accomplished..."));
        journalEntries.add(new JournalEntry("Setting New Goals", "2023-12-17", "I decided to focus on improving my..."));

        // Set up the adapter with the journal entries
        JournalEntryAdapter adapter = new JournalEntryAdapter(journalEntries);
        journalRecyclerView.setAdapter(adapter);
    }
}
