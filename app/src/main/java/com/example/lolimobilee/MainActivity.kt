package com.example.lolimobilee

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       Thread.sleep(3000)
        installSplashScreen()
        setContentView(R.layout.activity_main)
        val logoImageView = findViewById<ImageView>(R.id.logo)
        logoImageView.setOnClickListener {
            val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation)
            logoImageView.startAnimation(rotateAnimation)
        }
        val signin: Button = findViewById(R.id.signup_button)
        signin.setOnClickListener {
            val intent = Intent(this, Lolint::class.java)
            startActivity(intent)
        }
        val signup: Button = findViewById(R.id.login_button)
        signup.setOnClickListener {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }
        val btnSettings = findViewById<ImageView>(R.id.btnsetting)
        btnSettings.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

    }
}
