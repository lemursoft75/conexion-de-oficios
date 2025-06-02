package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.javipena.conexiondeoficios.R

class ContractorDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contractor_detail)

        val contractorName = findViewById<TextView>(R.id.text_name)
        val contractorPhone = findViewById<TextView>(R.id.text_phone)
        val contractorLocation = findViewById<ImageView>(R.id.image_map)
        val btnWhatsApp = findViewById<Button>(R.id.btn_whatsapp)
        val btnBack = findViewById<Button>(R.id.btn_back)

        // Recibir datos del contratista desde DirectoryActivity
        val contractor = intent.getStringExtra("CATEGORY")
        contractorName.text = contractor

        // Simulación de número de WhatsApp (esto puede venir de la base de datos)
        val phoneNumber = "+521234567890"
        contractorPhone.text = phoneNumber



        // Acción para abrir WhatsApp
        btnWhatsApp.setOnClickListener {
            val url = "https://wa.me/$phoneNumber"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        // Acción para regresar
        btnBack.setOnClickListener {
            finish()
        }
    }
}
