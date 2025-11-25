package com.javipena.conexiondeoficios.models

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

    // 📌 Sistema de reseñas
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,

    // 📌 Nuevos campos de atención y emergencias
    val attendsEmergencies: Boolean = false,
    val attentionDays: String = "",
    val attentionFrom: String = "",
    val attentionTo: String = ""

) : Parcelable

