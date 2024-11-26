package com.example.lolimobilee;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class userlog extends AppCompatActivity {

    private static final String TAG = "userlog";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView userLogRecyclerView;
    private UserLogAdapter userLogAdapter;
    private List<log> userLogs = new ArrayList<>();
    private BottomNavBar bottomNavBar;

    private EditText startDateEditText, endDateEditText;
    private Button applyFilterButton;
    private String userId;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlog);


        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID not provided. Cannot retrieve data.");
            Toast.makeText(this, "Error: User ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userLogRecyclerView = findViewById(R.id.conversationRecyclerView);
        userLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userLogAdapter = new UserLogAdapter(userLogs);
        userLogRecyclerView.setAdapter(userLogAdapter);

        startDateEditText = findViewById(R.id.startDate);
        endDateEditText = findViewById(R.id.endDate);
        applyFilterButton = findViewById(R.id.applyFilterButton);
        ImageView backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish()); // Back button functionality

        startDateEditText.setOnClickListener(v -> showDatePickerDialog(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(endDateEditText));

        applyFilterButton.setOnClickListener(v -> applyDateFilter());

        loadUserLogs();
        setupBottomNavigation();
    }

    private void loadUserLogs() {
        db.collection("loliresponse")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        return;
                    }

                    if (querySnapshot != null) {
                        userLogs.clear();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String userinput = doc.getString("userinput");
                            String lolioutput = doc.getString("lolioutput");
                            String time = doc.getString("Time");
                            String formattedTime = formatTime(time);

                            log log = new log(userinput, lolioutput, formattedTime);
                            userLogs.add(log);
                        }
                        userLogAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void applyDateFilter() {
        String startDateStr = startDateEditText.getText().toString();
        String endDateStr = endDateEditText.getText().toString();

        Date startDate = null;
        Date endDate = null;

        try {
            if (!startDateStr.isEmpty()) startDate = dateFormat.parse(startDateStr);
            if (!endDateStr.isEmpty()) endDate = dateFormat.parse(endDateStr);
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDate == null || endDate == null) {
            Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("user_logs")
                .whereEqualTo("userId", userId)
                .orderBy("Time", Query.Direction.ASCENDING)
                .whereGreaterThanOrEqualTo("Time", startDate)
                .whereLessThanOrEqualTo("Time", endDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userLogs.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String userinput = doc.getString("userinput");
                        String lolioutput = doc.getString("lolioutput");
                        String time = doc.getString("Time");

                        // Format the time
                        String formattedTime = formatTime(time);

                        log log = new log(userinput, lolioutput, formattedTime);
                        userLogs.add(log);
                    }
                    userLogAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error loading filtered logs", e));
    }

    private String formatTime(String time) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
            Date date = inputFormat.parse(time);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.w(TAG, "Time format error", e);
            return time;
        }
    }

    private void showDatePickerDialog(EditText dateEditText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    dateEditText.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
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
