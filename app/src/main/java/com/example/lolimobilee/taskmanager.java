package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class taskmanager extends AppCompatActivity implements TaskAdapter.OnTaskCompleteListener {

    private static final String TAG = "taskmanager";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> tasks = new ArrayList<>();
    private List<Task> filteredTasks = new ArrayList<>();
    private MaterialButtonToggleGroup taskToggleGroup;
    private ProgressBar incompleteTasksProgressBar;
    private ProgressBar completedTasksProgressBar;
    private View markAllCompletedButton; // Reference to the button
    private String userId;
    private BottomNavBar bottomNavBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskmanager);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(taskmanager.this, login.class));
            finish();
            return;
        }

        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            userId = currentUser.getUid();
        }

        if (userId == null) {
            Log.e(TAG, "User ID is null. Please check if it was passed correctly from the previous activity.");
            startActivity(new Intent(taskmanager.this, login.class));
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new TaskAdapter(filteredTasks, this); // Pass `this` as the OnTaskCompleteListener
        taskRecyclerView.setAdapter(taskAdapter);

        incompleteTasksProgressBar = findViewById(R.id.incompleteTasksProgressBar);
        completedTasksProgressBar = findViewById(R.id.completedTasksProgressBar);

        markAllCompletedButton = findViewById(R.id.markAllCompletedButton); // Initialize the button
        markAllCompletedButton.setOnClickListener(this::markAllCheckedTasksAsDone);

        loadTasksFromFirebase(); // Initial data load
        setupBottomNavigation();

        taskToggleGroup = findViewById(R.id.taskToggleGroup);
        taskToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.incompleteTasksButton) {
                    filterTasksByStatus("incomplete");
                    markAllCompletedButton.setVisibility(View.VISIBLE); // Show the button
                } else if (checkedId == R.id.completedTasksButton) {
                    filterTasksByStatus("done");
                    markAllCompletedButton.setVisibility(View.GONE); // Hide the button
                }
            }
        });
    }

    private void loadTasksFromFirebase() {
        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    if (querySnapshot != null) {
                        tasks.clear();
                        filteredTasks.clear();
                        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                            Task task = dc.getDocument().toObject(Task.class);
                            task.setId(dc.getDocument().getId());
                            tasks.add(task);

                            switch (dc.getType()) {
                                case ADDED:
                                    filteredTasks.add(task);
                                    taskAdapter.notifyItemInserted(filteredTasks.size() - 1);
                                    break;
                                case MODIFIED:
                                    int modifiedIndex = getTaskIndexById(task.getId());
                                    if (modifiedIndex != -1) {
                                        filteredTasks.set(modifiedIndex, task);
                                        taskAdapter.notifyItemChanged(modifiedIndex);
                                    }
                                    break;
                                case REMOVED:
                                    int removedIndex = getTaskIndexById(task.getId());
                                    if (removedIndex != -1) {
                                        filteredTasks.remove(removedIndex);
                                        taskAdapter.notifyItemRemoved(removedIndex);
                                    }
                                    break;
                            }
                        }
                        updateProgressBars();
                    }
                });
    }

    private int getTaskIndexById(String id) {
        for (int i = 0; i < filteredTasks.size(); i++) {
            if (filteredTasks.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private void updateProgressBars() {
        int totalTasks = tasks.size();
        int completedTasks = 0;
        int incompleteTasks = 0;

        for (Task task : tasks) {
            if (task.isComplete()) {
                completedTasks++;
            } else {
                incompleteTasks++;
            }
        }

        int incompleteProgress = totalTasks > 0 ? (incompleteTasks * 100) / totalTasks : 0;
        int completeProgress = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;

        incompleteTasksProgressBar.setProgress(incompleteProgress);
        completedTasksProgressBar.setProgress(completeProgress);
    }

    private void filterTasksByStatus(String status) {
        loadTasksFromFirebase(); // Refresh the task list on filter change
        filteredTasks.clear();
        for (Task task : tasks) {
            if ((status.equals("incomplete") && !task.isComplete()) ||
                    (status.equals("done") && task.isComplete())) {
                filteredTasks.add(task);
            }
        }
        taskAdapter.notifyDataSetChanged();

        findViewById(R.id.noTasksMessage).setVisibility(filteredTasks.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onTaskComplete(Task task, boolean isComplete) {
        db.collection("tasks").document(task.getId())
                .update("isComplete", isComplete)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Task completion status updated");
                    loadTasksFromFirebase(); // Refresh the task list
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error updating task", e));
    }

    private void markAllCheckedTasksAsDone(View view) {
        boolean hasCheckedTasks = false;

        for (Task task : tasks) {
            if (task.isChecked()) {
                task.setStatus("done");
                db.collection("tasks").document(task.getId())
                        .update("status", "done", "isChecked", false)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Task marked as done: " + task.getId()))
                        .addOnFailureListener(e -> Log.w(TAG, "Error updating task", e));
                hasCheckedTasks = true;
            }
        }

        if (hasCheckedTasks) {
            loadTasksFromFirebase(); // Refresh the task list
            Toast.makeText(this, "All checked tasks marked as done", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No tasks are checked", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        ImageView assessmentIcon = findViewById(R.id.assessment);
        ImageView bookIcon = findViewById(R.id.journal);
        ImageView taskIcon = findViewById(R.id.taskIcon);
        ImageView accountIcon = findViewById(R.id.account);
        FloatingActionButton dashboardIcon = findViewById(R.id.dashboardIcon);

        bottomNavBar = new BottomNavBar(this, userId, assessmentIcon, bookIcon, taskIcon, accountIcon, dashboardIcon);
    }
}
