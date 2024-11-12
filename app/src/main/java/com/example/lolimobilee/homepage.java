package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class homepage extends AppCompatActivity implements TaskAdapter.OnTaskCompleteListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "homepage";
    private List<JournalEntry> journalEntries;
    private List<Task> tasks;
    private JournalEntryAdapter journalAdapter;
    private TaskAdapter taskAdapter;
    private String userId;
    private BottomNavBar bottomNavBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);


        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            userId = "";  // Default value if userId is missing
            Log.e(TAG, "User ID not passed; defaulting to empty string.");
        }

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize greeting text
        TextView helloText = findViewById(R.id.helloText);
        if (!userId.isEmpty()) {
            loadUserData(helloText);
        } else {
            helloText.setText("Hi, User!");
        }

        // Initialize RecyclerViews and adapters
        setupJournalRecyclerView();
        setupTaskRecyclerView();

        // Load data from Firestore
        loadJournalEntries();
        loadTasks();


        setupBottomNavigation();
        findViewById(R.id.addJournalButton).setOnClickListener(v -> {
            Intent addIntent = new Intent(homepage.this, addjournal.class);
            addIntent.putExtra("userId", userId);
            startActivity(addIntent);
        });


        findViewById(R.id.submitButton).setOnClickListener(v -> markAllCheckedTasksAsDone());
    }

    private void setupJournalRecyclerView() {
        RecyclerView journalRecyclerView = findViewById(R.id.journalRecyclerView);
        journalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        journalEntries = new ArrayList<>();
        journalAdapter = new JournalEntryAdapter(this, journalEntries, userId);
        journalRecyclerView.setAdapter(journalAdapter);
    }

    private void setupTaskRecyclerView() {
        RecyclerView taskRecyclerView = findViewById(R.id.taskRecyclerView);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(tasks, this);
        taskRecyclerView.setAdapter(taskAdapter);
    }
    private void setupBottomNavigation() {

        ImageView assessmentIcon = findViewById(R.id.assessment);
        ImageView bookIcon = findViewById(R.id.bookIcon);
        ImageView taskIcon = findViewById(R.id.taskIcon);
        ImageView accountIcon = findViewById(R.id.account);
        FloatingActionButton dashboardIcon = findViewById(R.id.dashboardIcon);


        bottomNavBar = new BottomNavBar(this, userId, assessmentIcon, bookIcon, taskIcon, accountIcon, dashboardIcon);
    }

    private void loadUserData(TextView helloText) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String firstName = (fullName != null && !fullName.isEmpty()) ? fullName.split(" ")[0] : "User";
                        helloText.setText("Hi, " + firstName + "!");
                    } else {
                        helloText.setText("Hi, User!");
                        Log.d(TAG, "No such document for user.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user data", e);
                    helloText.setText("Hi, User!");
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadJournalEntries() {
        db.collection("journalEntries")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "created") // Only load entries with status "created"
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    if (querySnapshot != null) {
                        journalEntries.clear(); // Clear list to avoid duplications
                        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                            JournalEntry entry = dc.getDocument().toObject(JournalEntry.class);
                            entry.setId(dc.getDocument().getId());
                            switch (dc.getType()) {
                                case ADDED:
                                    journalEntries.add(entry);
                                    journalAdapter.notifyItemInserted(journalEntries.size() - 1);
                                    break;
                                case MODIFIED:
                                    int modifiedIndex = getJournalEntryIndexById(entry.getId());
                                    if (modifiedIndex != -1) {
                                        journalEntries.set(modifiedIndex, entry);
                                        journalAdapter.notifyItemChanged(modifiedIndex);
                                    }
                                    break;
                                case REMOVED:
                                    int removedIndex = getJournalEntryIndexById(entry.getId());
                                    if (removedIndex != -1) {
                                        journalEntries.remove(removedIndex);
                                        journalAdapter.notifyItemRemoved(removedIndex);
                                    }
                                    break;
                            }
                        }
                    }
                });
    }


    private void loadTasks() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat.format(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String yesterday = dateFormat.format(calendar.getTime());

        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "incomplete")
                .whereIn("date", Arrays.asList(today, yesterday))
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    if (querySnapshot != null) {
                        tasks.clear(); // Avoid duplications
                        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                            Task task = dc.getDocument().toObject(Task.class);
                            task.setId(dc.getDocument().getId());
                            switch (dc.getType()) {
                                case ADDED:
                                    tasks.add(task);
                                    taskAdapter.notifyItemInserted(tasks.size() - 1);
                                    break;
                                case MODIFIED:
                                    int modifiedIndex = getTaskIndexById(task.getId());
                                    if (modifiedIndex != -1) {
                                        tasks.set(modifiedIndex, task);
                                        taskAdapter.notifyItemChanged(modifiedIndex);
                                    }
                                    break;
                                case REMOVED:
                                    int removedIndex = getTaskIndexById(task.getId());
                                    if (removedIndex != -1) {
                                        tasks.remove(removedIndex);
                                        taskAdapter.notifyItemRemoved(removedIndex);
                                    }
                                    break;
                            }
                        }
                        findViewById(R.id.noPendingTasks).setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private int getTaskIndexById(String id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private int getJournalEntryIndexById(String id) {
        for (int i = 0; i < journalEntries.size(); i++) {
            if (journalEntries.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onTaskComplete(Task task, boolean isComplete) {
        db.collection("tasks").document(task.getId())
                .update("isCompleted", isComplete)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Task completion status updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating task", e));
    }

    private void markAllCheckedTasksAsDone() {
        for (Task task : tasks) {
            if (task.isChecked()) {
                db.collection("tasks").document(task.getId())
                        .update("status", "done", "isChecked", false)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Task marked as done: " + task.getId()))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating task", e));
            }
        }
        taskAdapter.notifyDataSetChanged();
        Toast.makeText(this, "All checked tasks marked as done", Toast.LENGTH_SHORT).show();
    }
}
