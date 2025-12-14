package com.javipena.conexiondeoficios.bot

class LocalBot {

    fun getWelcomeMessage(): String {
        return """
            ¡Hola! 👋 Soy tu asistente de Conexión de Oficios.
            Puedo ayudarte con lo siguiente:

            • Publicar anuncio
            • Editar anuncio
            • Eliminar anuncio
            • Contactar técnico
            • Problemas con la cuenta
            • Asistencia

            Escribe una opción o tu duda.
        """.trimIndent()
    }

    private val faqIntents = listOf(

        Intent(
            keywords = listOf("publicar", "subir", "crear", "poner"),
            response = "Para publicar un anuncio, ve al menú y selecciona 'Publicar anuncio'. Completa los datos y guarda."
        ),

        Intent(
            keywords = listOf("editar", "modificar", "cambiar"),
            response = "Para editar tu anuncio, entra a tu perfil, selecciona el anuncio y presiona 'Editar'."
        ),

        Intent(
            keywords = listOf("eliminar", "borrar", "quitar"),
            response = "Para eliminar un anuncio, entra a tu perfil, selecciona el anuncio y presiona 'Eliminar'."
        ),

        Intent(
            keywords = listOf("contactar", "hablar", "llamar", "whatsapp", "tecnico"),
            response = "Selecciona un anuncio y verás el botón para contactar por WhatsApp o ver la ubicación del técnico."
        ),

        Intent(
            keywords = listOf("no veo", "no aparecen", "sin anuncios"),
            response = "Si no ves anuncios, es posible que aún no haya técnicos registrados en esa categoría."
        ),

        Intent(
            keywords = listOf("cuenta", "perfil", "registro"),
            response = "Regístrate desde 'Regístrate aquí'. Puedes editar tus datos desde tu perfil."
        ),

        Intent(
            keywords = listOf("asistencia", "ayuda", "soporte"),
            response = "Asistencia:\n9995499691\nWhatsApp disponible"

        )

    )

    fun getResponse(userMessage: String): String {
        val msg = normalize(userMessage)

        for (intent in faqIntents) {
            if (intent.keywords.any { msg.contains(it) }) {
                return intent.response
            }
        }

        return """
            No entendí del todo, pero puedo ayudarte con:
            • Publicar anuncios
            • Editar o eliminar anuncios
            • Contactar técnicos
            • Problemas con anuncios
            • Información de tu cuenta
            • Asistencia
        """.trimIndent()
    }

    private fun normalize(text: String): String {
        return text.lowercase()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
    }
}

data class Intent(
    val keywords: List<String>,
    val response: String
)
