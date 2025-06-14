package com.javipena.conexiondeoficios.activities


import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.javipena.conexiondeoficios.R


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.splash_logo)
        val fadeInOut = AlphaAnimation(0f, 1f).apply {
            duration = 1500
            fillAfter = true
        }
        logo.startAnimation(fadeInOut)

        // Sonido de bienvenida
        val mediaPlayer = MediaPlayer.create(this, R.raw.splash_sound)
        mediaPlayer.start()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3500)
    }
}
