package com.talhaatif.financeapk

import android.annotation.SuppressLint
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import android.view.animation.AnimationUtils
import com.talhaatif.financeapk.databinding.ActivitySplashScreenBinding

@SuppressLint("CustomSplashScreen")
@Suppress("DEPRECATION")
class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load animations
        val logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_fade_in)
        val textAnimation = AnimationUtils.loadAnimation(this, R.anim.text_slide_up)

        // Start animations
        binding.logo.startAnimation(logoAnimation)
        binding.slogan.startAnimation(textAnimation)

        // Move to the next screen after 3 seconds
        Handler().postDelayed({
            val intent = Intent(this, SignUpScreen::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}
