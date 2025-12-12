package com.javipena.conexiondeoficios.bot

class LocalBot {

    // Preguntas sugeridas (se le muestran al usuario al abrir el chat)
    val suggestedQuestions = listOf(
        "¿Cómo publico un anuncio?",
        "¿Cómo edito mi anuncio?",
        "¿Cómo contacto a un tecnico?",
        "¿Por qué no veo anuncios?",
        "¿Cómo funciona mi cuenta?"
    )

    // Mensaje de bienvenida
    fun getWelcomeMessage(): String {
        return "¡Hola! 👋 Soy tu asistente de Conexión de Oficios.\n" +
                "¿En qué puedo ayudarte hoy?\n\n" +
                "Aquí tienes algunas preguntas sugeridas:"
    }

    // Lista de preguntas y respuestas del bot
    private val responses = mapOf(
        "publicar anuncio" to "Para publicar un anuncio, ve al menú y selecciona 'Publicar anuncio'. Completa los datos y guarda.",
        "editar anuncio" to "Para editar tu anuncio, entra a tu perfil, selecciona tu anuncio y presiona 'Editar'.",
        "eliminar anuncio" to "Para eliminar un anuncio, abre tu perfil, selecciona el anuncio y presiona 'Eliminar'.",
        "contactar tecnico" to "Para contactar un tecnico, entra a una categoría, selecciona un anuncio y verás el número de contacto.",
        "no veo anuncios" to "Si no ves anuncios, revisa tu conexión o prueba otra categoría. A veces aún no hay técnicos registrados ahí.",
        "cuenta" to "Tu cuenta se crea automáticamente al iniciar sesión. Puedes actualizar tus datos desde tu perfil."
    )

    fun getResponse(userMessage: String): String {
        val msg = userMessage.lowercase()

        // Buscar coincidencias por palabras clave
        for ((key, value) in responses) {
            if (msg.contains(key)) {
                return value
            }
        }

        // Respuesta genérica si no encuentra
        return "No estoy seguro de eso, pero puedo ayudarte con:\n" +
                "- Publicar anuncio\n" +
                "- Editar anuncio\n" +
                "- Contactar tecnico\n" +
                "- Problemas con anuncios\n" +
                "- Información de la cuenta"
    }
}
