package com.javipena.conexiondeoficios.models

// No olvides hacerla Parcelable si necesitas pasarla entre actividades
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contractor(
    val name: String = "",
    val lastname: String = "",
    val phone: String = "",
    val email: String = "",
    val companyName: String = "",
    val specialty: String = "",
    val rfc: String = "",
    val latitude: String = "",
    val longitude: String = "",

    // 📌 CAMPOS AÑADIDOS PARA EL SISTEMA DE RESEÑAS
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0

) : Parcelable