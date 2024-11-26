package com.example.lolimobilee;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BottomNavBar {

    private static final String TAG = "BottomNavBar";
    private final Activity activity;
    private final String userId;
    private final ImageView assessmentIcon;
    private final ImageView bookIcon;
    private final ImageView taskIcon;
    private final ImageView accountIcon;
    private final FloatingActionButton dashboardIcon;

    public BottomNavBar(Activity activity, String userId, ImageView assessmentIcon, ImageView bookIcon,
                        ImageView taskIcon, ImageView accountIcon, FloatingActionButton dashboardIcon) {
        this.activity = activity;
        this.userId = userId;
        this.assessmentIcon = assessmentIcon;
        this.bookIcon = bookIcon;
        this.taskIcon = taskIcon;
        this.accountIcon = accountIcon;
        this.dashboardIcon = dashboardIcon;
        setupNavigation();
    }

    private void setupNavigation() {
        if (assessmentIcon != null) {
            assessmentIcon.setOnClickListener(v -> navigateToAssessment());
        } else {
            Log.e(TAG, "setupNavigation: assessmentIcon is null");
            Toast.makeText(activity, "Assessment icon not found", Toast.LENGTH_SHORT).show();
        }

        if (bookIcon != null) {
            bookIcon.setOnClickListener(v -> navigateToJournal());
        } else {
            Log.e(TAG, "setupNavigation: bookIcon is null");
            Toast.makeText(activity, "Journal icon not found", Toast.LENGTH_SHORT).show();
        }

        if (taskIcon != null) {
            taskIcon.setOnClickListener(v -> navigateToTasks());
        } else {
            Log.e(TAG, "setupNavigation: taskIcon is null");
            Toast.makeText(activity, "Tasks icon not found", Toast.LENGTH_SHORT).show();
        }

        if (accountIcon != null) {
            accountIcon.setOnClickListener(v -> navigateToAccount());
        } else {
            Log.e(TAG, "setupNavigation: accountIcon is null");
            Toast.makeText(activity, "Account icon not found", Toast.LENGTH_SHORT).show();
        }

        if (dashboardIcon != null) {
            dashboardIcon.setOnClickListener(v -> navigateToDashboard());
        } else {
            Log.e(TAG, "setupNavigation: dashboardIcon is null");
            Toast.makeText(activity, "Dashboard icon not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToAssessment() {
        Intent intent = new Intent(activity, assessment.class);
        intent.putExtra("userId", userId);
        activity.startActivity(intent);
    }

    private void navigateToJournal() {
        Intent intent = new Intent(activity, journal.class);
        intent.putExtra("userId", userId);
        activity.startActivity(intent);
    }

    private void navigateToTasks() {
        Intent intent = new Intent(activity, taskmanager.class);
        intent.putExtra("userId", userId);
        activity.startActivity(intent);
    }

    private void navigateToAccount() {
        Intent intent = new Intent(activity, myaccount.class);
        intent.putExtra("userId", userId);
        activity.startActivity(intent);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(activity, homepage.class);
        intent.putExtra("userId", userId);
        activity.startActivity(intent);
    }
}
