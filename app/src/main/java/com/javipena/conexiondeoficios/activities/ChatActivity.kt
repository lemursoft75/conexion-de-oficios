package com.javipena.conexiondeoficios.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.javipena.conexiondeoficios.databinding.ActivityChatBinding
import com.javipena.conexiondeoficios.bot.LocalBot
import com.javipena.conexiondeoficios.adapters.ChatAdapter

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val messages = mutableListOf<Pair<String, Boolean>>()   // String = mensaje, Boolean = isUser
    private lateinit var adapter: ChatAdapter
    private val bot = LocalBot()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ChatAdapter(messages)
        binding.recyclerChat.layoutManager = LinearLayoutManager(this)
        binding.recyclerChat.adapter = adapter

        // ⏳ Bienvenida automática del bot
        addMessage(
            "¡Hola! 👋 Bienvenido al centro de ayuda.\n" +
                    "Puedo orientarte con lo siguiente:\n\n" +
                    "• Publicar anuncio\n" +
                    "• Editar anuncio\n" +
                    "• Eliminar anuncio\n" +
                    "• Contactar técnico\n" +
                    "• Problemas con la cuenta\n\n" +
                    "Escribe una opción o tu duda.",
            false
        )

        binding.btnSend.setOnClickListener {
            val text = binding.inputMessage.text.toString().trim()

            if (text.isNotEmpty()) {
                addMessage(text, true)
                binding.inputMessage.text.clear()

                val reply = bot.getResponse(text)
                addMessage(reply, false)
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(Pair(text, isUser))
        adapter.notifyItemInserted(messages.size - 1)
        binding.recyclerChat.scrollToPosition(messages.size - 1)
    }
}
