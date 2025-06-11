package com.javipena.conexiondeoficios.models

data class Review(
    val clientId: String = "",
    val clientName: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val timestamp: Long = 0
)