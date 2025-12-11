package com.javipena.conexiondeoficios // O com.javipena.conexiondeoficios.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Representa un único anuncio publicado por un contratista.
 */
@Parcelize
data class Ad(
    val contractorId: String = "",
    val adText: String = "",
    val phone: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val specialty: String = "",
    val mediaUrl: String? = null,

    // 🚨 CAMPOS AÑADIDOS PARA EL ORDENAMIENTO POR RESEÑAS

    /** Promedio de las calificaciones del contratista (copiado de su perfil al momento de publicar el anuncio) */
    val averageRating: Double = 0.0,

    /** Número total de reseñas que ha recibido el contratista */
    val reviewCount: Int = 0

) : Parcelable