package com.example.lolimobilee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

public class Lolint extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lolint);

        // Adjust for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        TextView textView = findViewById(R.id.introtxt);
        TextView loginnavv = findViewById(R.id.loginnav);
        LottieAnimationView nextButtonAnimation = findViewById(R.id.nextButtonAnimation); // Updated Next Button
        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimation);

        // Initially hide the Lottie button
        nextButtonAnimation.setVisibility(View.GONE);

        // Play robot animation
        lottieAnimationView.setVisibility(View.VISIBLE);
        lottieAnimationView.playAnimation();

        // Typing animation for the intro text
        String label = "Hi! I'm Loli, your companion robot here to help you improve your social skills. I'll ask you a few questions to get to know you better so I can tailor my support just for you. Your info stays private—let's get started!";
        StringBuilder stringBuilder = new StringBuilder();

        new Thread(() -> {
            for (char letter : label.toCharArray()) {
                stringBuilder.append(letter);
                try {
                    Thread.sleep(50); // Adjust typing speed
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                runOnUiThread(() -> textView.setText(stringBuilder.toString()));
            }


            runOnUiThread(() -> {
                nextButtonAnimation.setVisibility(View.VISIBLE);
                nextButtonAnimation.setProgress(0f); // Reset animation
                nextButtonAnimation.playAnimation();

                // Set click listener on Lottie animation
                nextButtonAnimation.setOnClickListener(v -> {
                    Intent intent = new Intent(Lolint.this, loliregister1.class);
                    startActivity(intent);
                });
            });
        }).start();

        // Navigate to login activity on "Already Have an Account?" click
        loginnavv.setOnClickListener(v -> {
            Intent intent = new Intent(Lolint.this, login.class);
            startActivity(intent);
        });
    }
}
