package com.example.lolimobilee;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Lolint extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lolint);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView textView = findViewById(R.id.introtxt);
        TextView loginnavv = findViewById(R.id.loginnav);
        ImageView next = findViewById(R.id.imgnext);
        String label = "Hi! I'm Loli, your companion robot here to help you improve your social skills. I'll ask you a few questions to get to know you better so I can tailor my support just for you. Your info stays private—let's get started!";
        StringBuilder stringBuilder = new StringBuilder();

        // Apply typing animation to the text
        new Thread(() -> {
            for (char letter : label.toCharArray()) {
                stringBuilder.append(letter);
                try {
                    Thread.sleep(100); // Adjust delayas needed
                } catch (InterruptedException e) {
                    // Handle the InterruptedException
                    Thread.currentThread().interrupt(); // Preserve interrupt status
                    // You can add logging or other error handling here if needed
                    // e.printStackTrace(); // Example: Print the stack trace
                }
                runOnUiThread(() -> textView.setText(stringBuilder.toString()));
            }

            // After animation is complete, make the button visible and clickable
            runOnUiThread(() -> {
                next.setVisibility(View.VISIBLE);
                Animation fadeInAnimation = AnimationUtils.loadAnimation(Lolint.this, android.R.anim.fade_in);
                next.startAnimation(fadeInAnimation);

                next.setOnClickListener(v -> {
                    Intent intent = new Intent(Lolint.this, loliregister1.class);
                    startActivity(intent);
                });
            });
        }).start();

        loginnavv.setOnClickListener(v -> {
            Intent intent = new Intent(Lolint.this, login.class);
            startActivity(intent);
        });
    }
}