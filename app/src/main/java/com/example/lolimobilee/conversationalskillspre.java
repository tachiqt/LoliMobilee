package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class conversationalskillspre extends AppCompatActivity {

    private LinearLayout questionContainer;
    private TextView totalScoreText;
    private MaterialButton submitButton;

    private String topicTitle;
    private String assessmentType;
    private String userId;
    private FirebaseFirestore db;

    private float totalScore = 0f;
    private int questionCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversationalskillspre);

        questionContainer = findViewById(R.id.questionContainer);
        totalScoreText = findViewById(R.id.totalScoreText);
        submitButton = findViewById(R.id.submitButton);

        // Get Intent Data
        topicTitle = getIntent().getStringExtra("topicTitle");
        assessmentType = getIntent().getStringExtra("assessmentType");
        userId = getIntent().getStringExtra("userId");

        db = FirebaseFirestore.getInstance();

        if (topicTitle != null) {
            addCategoryTitle(topicTitle);
        }

        // Load topics dynamically
        loadQuestionsForTopic();

        // Submit Button Listener
        submitButton.setOnClickListener(v -> showConfirmationDialog());
    }

    private void addCategoryTitle(String title) {
        TextView categoryTitle = new TextView(this);
        categoryTitle.setText(title);
        categoryTitle.setTextSize(18);
        categoryTitle.setTextColor(getResources().getColor(R.color.white));
        categoryTitle.setPadding(0, 16, 0, 8);
        questionContainer.addView(categoryTitle);
    }

    private void loadQuestionsForTopic() {
        List<Topic> topics = QuestionsProvider.getTopics();
        boolean topicFound = false;

        for (Topic topic : topics) {
            if (topic.getTitle().equalsIgnoreCase(topicTitle)) {
                topicFound = true;
                for (String question : topic.getQuestions()) {
                    addQuestionCard(question);
                }
                break;
            }
        }

        if (!topicFound) {
            Toast.makeText(this, "No questions available for this topic.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addQuestionCard(String question) {
        questionCount++;

        MaterialCardView cardView = new MaterialCardView(this);
        LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardLayoutParams.setMargins(16, 16, 16, 16);
        cardView.setLayoutParams(cardLayoutParams);
        cardView.setCardElevation(6f);
        cardView.setRadius(16f);
        cardView.setCardBackgroundColor(getResources().getColor(R.color.white));
        cardView.setUseCompatPadding(true);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(24, 24, 24, 24);

        TextView questionTextView = new TextView(this);
        questionTextView.setText(question);
        questionTextView.setTextSize(16);
        questionTextView.setTextColor(getResources().getColor(R.color.dark_purple));
        questionTextView.setPadding(0, 0, 0, 16);
        linearLayout.addView(questionTextView);

        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1f);
        ratingBar.setRating(0);
        ratingBar.setIsIndicator(false);
        ratingBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        ratingBar.setPadding(0, 8, 0, 16);
        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> updateTotalScore());
        linearLayout.addView(ratingBar);

        cardView.addView(linearLayout);
        questionContainer.addView(cardView);
    }

    private void updateTotalScore() {
        totalScore = 0f;
        for (int i = 0; i < questionContainer.getChildCount(); i++) {
            if (questionContainer.getChildAt(i) instanceof MaterialCardView) {
                MaterialCardView card = (MaterialCardView) questionContainer.getChildAt(i);
                LinearLayout linearLayout = (LinearLayout) card.getChildAt(0);
                RatingBar ratingBar = (RatingBar) linearLayout.getChildAt(1);
                totalScore += ratingBar.getRating();
            }
        }
        float averageScore = totalScore / questionCount;
        totalScoreText.setText(String.format("Total Score: %.1f", averageScore));
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Submit Assessment")
                .setMessage("Are you sure you want to submit your answers?")
                .setPositiveButton("Yes", (dialog, which) -> saveResponsesToFirestore())
                .setNegativeButton("No", null)
                .show();
    }

    private void saveResponsesToFirestore() {
        Map<String, Object> responses = new HashMap<>();

        for (int i = 0; i < questionContainer.getChildCount(); i++) {
            if (questionContainer.getChildAt(i) instanceof MaterialCardView) {
                MaterialCardView card = (MaterialCardView) questionContainer.getChildAt(i);
                LinearLayout linearLayout = (LinearLayout) card.getChildAt(0);
                TextView questionTextView = (TextView) linearLayout.getChildAt(0);
                String question = questionTextView.getText().toString();
                RatingBar ratingBar = (RatingBar) linearLayout.getChildAt(1);
                float rating = ratingBar.getRating();
                responses.put(question, rating);
            }
        }

        float averageScore = totalScore / questionCount;
        responses.put("Average Score", averageScore);
        responses.put("Topic", topicTitle);
        responses.put("Assessment Type", assessmentType);
        responses.put("UserId", userId);
        responses.put("Status", "Completed");

        String documentId = userId + "_" + topicTitle; // Unique ID per user and topic
        db.collection("pre_assessment")
                .document(documentId)
                .set(responses, SetOptions.merge()) // Avoid overwriting existing data
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Assessment Completed", Toast.LENGTH_SHORT).show();
                    navigateToAssessmentPage();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving assessment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToAssessmentPage() {
        Intent intent = new Intent(this, assessment.class);
        intent.putExtra("topicTitle", topicTitle);
        intent.putExtra("userId", userId); // Pass the userId to the next activity
        intent.putExtra("completed", true);
        startActivity(intent);
        finish();
    }
}
