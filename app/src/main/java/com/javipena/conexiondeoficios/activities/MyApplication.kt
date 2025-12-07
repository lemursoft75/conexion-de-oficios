package com.javipena.conexiondeoficios

import android.app.Application
import com.cloudinary.android.MediaManager
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.ktx.Firebase
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.ktx.initialize

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Cloudinary seguro (sin api_secret)
        val config = mapOf(
            "cloud_name" to "dcutvougi",
            "api_key" to "347278685438983",
            "api_secret" to "IvwxT6hUUUHKIKQPGlb6VQnd9D4"
        )
        MediaManager.init(this, config)

        // Firebase + App Check
        Firebase.initialize(this)

        val appCheck = Firebase.appCheck
        appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        FirebaseAppCheck.getInstance().setTokenAutoRefreshEnabled(true)
    }
}

