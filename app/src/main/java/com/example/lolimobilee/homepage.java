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

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private TextView statSummary;
    private TextView noJournalText, noTaskText;
    private LottieAnimationView loadingAnimation; // Lottie animation view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            userId = "";
            Log.e(TAG, "User ID not passed; defaulting to empty string.");
        }

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView helloText = findViewById(R.id.helloText);
        statSummary = findViewById(R.id.statSummary);
        noJournalText = findViewById(R.id.noJournalText);
        noTaskText = findViewById(R.id.noTaskText);
        loadingAnimation = findViewById(R.id.loadingAnimation);

        if (!userId.isEmpty()) {
            loadUserData(helloText);
        } else {
            helloText.setText("Hi, User!");
        }

        setupJournalRecyclerView();
        setupTaskRecyclerView();
        setupBottomNavigation();

        findViewById(R.id.addJournalButton).setOnClickListener(v -> {
            Intent addIntent = new Intent(homepage.this, addjournal.class);
            addIntent.putExtra("userId", userId);
            startActivity(addIntent);
        });

        findViewById(R.id.submitButton).setOnClickListener(v -> markAllCheckedTasksAsDone());

        // Initial Data Load
        refreshJournalList();
        refreshTaskList();
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

    public void refreshJournalList() {
        showLoadingAnimation();
        db.collection("journalEntries")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "created")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    journalEntries.clear();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        JournalEntry entry = document.toObject(JournalEntry.class);
                        if (entry != null) {
                            entry.setId(document.getId());
                            journalEntries.add(entry);
                        }
                    }
                    journalAdapter.notifyDataSetChanged();
                    updateEmptyJournalState();
                    updateStatSummary();
                    hideLoadingAnimation();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to refresh journal list", Toast.LENGTH_SHORT).show();
                    hideLoadingAnimation();
                });
    }

    public void refreshTaskList() {
        showLoadingAnimation();

        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "incomplete")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    tasks.clear();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Task task = document.toObject(Task.class);
                        if (task != null) {
                            task.setId(document.getId());
                            tasks.add(task);
                        }
                    }
                    taskAdapter.notifyDataSetChanged();
                    updateEmptyTaskState();
                    updateStatSummary();
                    hideLoadingAnimation();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to refresh task list", Toast.LENGTH_SHORT).show();
                    hideLoadingAnimation();
                });
    }


    private void updateEmptyJournalState() {
        noJournalText.setVisibility(journalEntries.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateEmptyTaskState() {
        noTaskText.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateStatSummary() {
        String summary = String.format("You have %d journal entries and %d tasks to complete.",
                journalEntries.size(), tasks.size());
        statSummary.setText(summary);
    }

    private void showLoadingAnimation() {
        loadingAnimation.setVisibility(View.VISIBLE);
        loadingAnimation.playAnimation();
    }

    private void hideLoadingAnimation() {
        loadingAnimation.cancelAnimation();
        loadingAnimation.setVisibility(View.GONE);
    }

    @Override
    public void onTaskComplete(Task task, boolean isComplete) {
        db.collection("tasks").document(task.getId())
                .update("isCompleted", isComplete)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Task completion status updated");
                    refreshTaskList();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error updating task", e));
    }



private void markAllCheckedTasksAsDone() {
        boolean hasCheckedTasks = false;

        for (Task task : tasks) {
            if (task.isChecked()) {
                hasCheckedTasks = true;
                db.collection("tasks").document(task.getId())
                        .update("status", "done", "isChecked", false)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Task marked as done: " + task.getId()))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating task", e));
            }
        }

        if (hasCheckedTasks) {
            refreshTaskList();
            Toast.makeText(this, "All checked tasks marked as done", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No marked tasks to complete", Toast.LENGTH_SHORT).show();
        }
    }
}
