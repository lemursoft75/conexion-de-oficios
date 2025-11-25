package com.javipena.conexiondeoficios.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.javipena.conexiondeoficios.Ad
import com.javipena.conexiondeoficios.R

class EditAdActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var btnSave: Button

    private val db = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_ad)

        editText = findViewById(R.id.edit_text_ad)
        btnSave = findViewById(R.id.btn_save_ad)

        val adId = intent.getStringExtra("AD_ID")
        val ad = intent.getParcelableExtra<Ad>("AD_OBJECT")

        if (adId == null || ad == null) {
            Toast.makeText(this, "Error al cargar anuncio", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        editText.setText(ad.adText)

        btnSave.setOnClickListener {
            val newText = editText.text.toString().trim()

            if (newText.isEmpty()) {
                Toast.makeText(this, "El anuncio no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.getReference("Ads").child(adId).child("adText")
                .setValue(newText)
                .addOnSuccessListener {
                    Toast.makeText(this, "Anuncio actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
