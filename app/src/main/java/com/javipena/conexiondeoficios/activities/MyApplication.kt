package com.javipena.conexiondeoficios

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Configura tus credenciales de Cloudinary aquí
        val config = mapOf(
            "cloud_name" to "dcutvougi",
            "api_key" to "347278685438983",
            "api_secret" to "IvwxT6hUUUHKIKQPGlb6VQnd9D4"
        )
        MediaManager.init(this, config)
    }
}