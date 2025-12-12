// Archivo de configuración global para Gradle en el proyecto

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.13.2") // Plugin de Android (déjalo como está)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22") // 🚨 ACTUALIZADO a 1.9.22
        classpath("com.google.gms:google-services:4.3.15") // Firebase (déjalo como está)
    }
}