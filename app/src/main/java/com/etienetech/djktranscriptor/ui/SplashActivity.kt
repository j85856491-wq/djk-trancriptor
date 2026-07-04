package com.etienetech.djktranscriptor.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.etienetech.djktranscriptor.R
import com.etienetech.djktranscriptor.util.PrefsManager

/**
 * Splash screen with app branding and animated loading.
 *
 * DJK Transcriptor - Developed by Etienne Tech
 */
class SplashActivity : AppCompatActivity() {

    private val splashDuration = 2500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Animate logo
        val logo = findViewById<View>(R.id.ivLogo)
        logo.alpha = 0f
        logo.scaleX = 0.5f
        logo.scaleY = 0.5f
        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setInterpolator(OvershootInterpolator(1.5f))
            .start()

        // Animate app name
        val appName = findViewById<View>(R.id.tvAppName)
        appName.alpha = 0f
        appName.translationY = 50f
        appName.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(400)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Animate tagline
        val tagline = findViewById<View>(R.id.tvTagline)
        tagline.alpha = 0f
        tagline.translationY = 30f
        tagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Animate powered by text
        val poweredBy = findViewById<View>(R.id.tvPoweredBy)
        poweredBy.alpha = 0f
        poweredBy.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(1000)
            .start()

        // Navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, splashDuration)
    }

    private fun navigateNext() {
        val prefs = PrefsManager(this)
        val intent = if (prefs.isOnboardingCompleted()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, OnboardingActivity::class.java)
        }

        // Fade out animation
        val root = findViewById<View>(android.R.id.content)
        ObjectAnimator.ofFloat(root, "alpha", 1f, 0f).apply {
            duration = 300
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            })
            start()
        }
    }

    override fun onBackPressed() {
        // Disable back on splash
    }
}
