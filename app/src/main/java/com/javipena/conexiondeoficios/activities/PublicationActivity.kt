package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.javipena.conexiondeoficios.R

class PublicationActivity : AppCompatActivity() {
    private lateinit var editAdText: EditText
    private lateinit var btnPublish: Button
    private lateinit var btnWhatsApp: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publication)

        auth = FirebaseAuth.getInstance()
        editAdText = findViewById(R.id.edit_ad_text)
        btnPublish = findViewById(R.id.btn_publish)
        btnWhatsApp = findViewById(R.id.btn_whatsapp)

        // 📌 Botón para publicar anuncio en Firebase
        btnPublish.setOnClickListener {
            val adText = editAdText.text.toString()
            val userId = auth.currentUser?.uid

            val adData = hashMapOf(
                "contractorId" to userId,
                "adText" to adText,
                "latitude" to "20.97",
                "longitude" to "-89.62"
            )

            FirebaseDatabase.getInstance().getReference("Ads")
                .child(userId!!)
                .setValue(adData)
                .addOnSuccessListener {
                    Toast.makeText(this, "✅ Anuncio publicado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "❌ Error al publicar anuncio", Toast.LENGTH_SHORT).show()
                }
        }

        // 📌 Botón para abrir WhatsApp
        btnWhatsApp.setOnClickListener {
            val phoneNumber = "+521234567890" // 📌 Número del contratista, obténlo de Firebase
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/$phoneNumber")
            startActivity(intent)
        }
    }
}
