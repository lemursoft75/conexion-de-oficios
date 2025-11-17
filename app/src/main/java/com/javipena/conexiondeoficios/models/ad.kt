package com.javipena.conexiondeoficios // O com.javipena.conexiondeoficios.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Representa un único anuncio publicado por un contratista.
 *
 * Implementa 'Parcelable' para que podamos pasar objetos de este tipo
 * entre actividades (por ejemplo, de la lista de anuncios a la pantalla de detalle).
 * La anotación @Parcelize genera automáticamente todo el código necesario para esto.
 */
@Parcelize
data class Ad(
    val contractorId: String = "",
    val adText: String = "",
    val phone: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val specialty: String = "",
    val mediaUrl: String? = null
) : Parcelable


