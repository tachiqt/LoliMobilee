package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class journal extends AppCompatActivity {

    private static final String TAG = "journal";
    private RecyclerView journalRecyclerView;
    private JournalEntryAdapter journalAdapter;
    private BottomNavBar bottomNavBar;
    private String userId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<JournalEntry> journalEntries = new ArrayList<>();
    private List<JournalEntry> filteredJournalEntries = new ArrayList<>();
    private FloatingActionButton addJournalFab;
    private TextInputEditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_journal);
        userId = getIntent().getStringExtra("userId");
        searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterJournalEntries(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        journalRecyclerView = findViewById(R.id.journalRecyclerView);
        journalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        journalAdapter = new JournalEntryAdapter(this, filteredJournalEntries, userId);
        journalRecyclerView.setAdapter(journalAdapter);

        loadJournalEntries();

        ImageView assessmentIcon = findViewById(R.id.assessment);
        ImageView bookIcon = findViewById(R.id.bookIcon);
        ImageView taskIcon = findViewById(R.id.taskIcon);
        ImageView accountIcon = findViewById(R.id.account);
        FloatingActionButton dashboardIcon = findViewById(R.id.dashboardIcon);

        bottomNavBar = new BottomNavBar(this, userId, assessmentIcon, bookIcon, taskIcon, accountIcon, dashboardIcon);

        addJournalFab = findViewById(R.id.addIcon);
        addJournalFab.setOnClickListener(v -> {
            Intent intent = new Intent(journal.this, addjournal.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }

    private void loadJournalEntries() {
        db.collection("journalEntries")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "created")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    if (querySnapshot != null) {
                        journalEntries.clear();
                        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                            JournalEntry entry = dc.getDocument().toObject(JournalEntry.class);
                            entry.setId(dc.getDocument().getId());
                            switch (dc.getType()) {
                                case ADDED:
                                    journalEntries.add(entry);
                                    break;
                                case MODIFIED:
                                    int modifiedIndex = getJournalEntryIndexById(entry.getId());
                                    if (modifiedIndex != -1) {
                                        journalEntries.set(modifiedIndex, entry);
                                    }
                                    break;
                                case REMOVED:
                                    int removedIndex = getJournalEntryIndexById(entry.getId());
                                    if (removedIndex != -1) {
                                        journalEntries.remove(removedIndex);
                                    }
                                    break;
                            }
                        }
                        filterJournalEntries(searchBar.getText().toString());
                    }
                });
    }


    private void filterJournalEntries(String query) {
        filteredJournalEntries.clear();
        if (query.isEmpty()) {
            filteredJournalEntries.addAll(journalEntries);
        } else {
            for (JournalEntry entry : journalEntries) {
                if (entry.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        entry.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredJournalEntries.add(entry);
                }
            }
        }
        journalAdapter.notifyDataSetChanged();
    }

    private int getJournalEntryIndexById(String id) {
        for (int i = 0; i < journalEntries.size(); i++) {
            if (journalEntries.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
