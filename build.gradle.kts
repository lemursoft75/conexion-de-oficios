// Archivo de configuración global para Gradle en el proyecto

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.5.0") // 📌 Plugin de Android
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0") // 📌 Plugin Kotlin
        classpath("com.google.gms:google-services:4.3.15") // 📌 Firebase
    }
}
