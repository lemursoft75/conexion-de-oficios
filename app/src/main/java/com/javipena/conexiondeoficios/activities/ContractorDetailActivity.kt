package com.javipena.conexiondeoficios.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.javipena.conexiondeoficios.Ad
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.models.Contractor

class ContractorDetailActivity : AppCompatActivity() {

    private lateinit var textContractorName: TextView
    private lateinit var textCompanyName: TextView
    private lateinit var textAdDescription: TextView
    private lateinit var imageAdDetail: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contractor_detail)
        title = "Detalle del Anuncio"

        // Recibir el objeto 'Ad' que se envió desde el adaptador
        val ad = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("AD_DETAIL", Ad::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Ad>("AD_DETAIL")
        }

        // Si el anuncio es nulo, mostrar error y cerrar la pantalla
        if (ad == null) {
            Toast.makeText(this, "Error: No se pudieron cargar los datos del anuncio.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Referencias a las vistas del layout
        textContractorName = findViewById(R.id.text_contractor_name)
        textCompanyName = findViewById(R.id.text_company_name)
        textAdDescription = findViewById(R.id.text_ad_description)
        imageAdDetail = findViewById(R.id.image_ad_detail)
        val textPhone = findViewById<TextView>(R.id.text_phone)
        val imageMap = findViewById<ImageView>(R.id.image_map)
        val btnWhatsApp = findViewById<Button>(R.id.btn_whatsapp)
        val btnBack = findViewById<Button>(R.id.btn_back)

        // Asignar datos del anuncio a las vistas
        textAdDescription.text = ad.adText
        textPhone.text = "Teléfono: ${ad.phone}"

        // Cargar la imagen del anuncio con Glide si existe
        if (!ad.mediaUrl.isNullOrEmpty()) {
            imageAdDetail.visibility = View.VISIBLE
            Glide.with(this)
                .load(ad.mediaUrl)
                .placeholder(R.drawable.ic_image_placeholder) // Opcional: un placeholder mientras carga
                .error(R.drawable.ic_image_error) // Opcional: una imagen de error si falla la carga
                .into(imageAdDetail)
        } else {
            imageAdDetail.visibility = View.GONE
        }

        // Buscar y mostrar el perfil del contratista
        if (ad.contractorId.isNotEmpty()) {
            fetchContractorProfile(ad.contractorId)
        } else {
            textContractorName.text = "Contratista Anónimo"
        }

        // --- Configuración de los botones ---
        btnWhatsApp.setOnClickListener {
            val phoneNumber = ad.phone.replace(Regex("[^0-9]"), "")
            val url = "https://wa.me/$phoneNumber"
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                Toast.makeText(this, "No se pudo abrir WhatsApp.", Toast.LENGTH_SHORT).show()
            }
        }

        imageMap.setOnClickListener {
            // 📌 ESTA ES LA LÍNEA CORREGIDA PARA EL MAPA
            val gmmIntentUri = Uri.parse("geo:${ad.latitude},${ad.longitude}?q=${ad.latitude},${ad.longitude}(${textContractorName.text})")

            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            try {
                startActivity(mapIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No se encontró la aplicación de Google Maps.", Toast.LENGTH_LONG).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun fetchContractorProfile(userId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val contractor = snapshot.getValue(Contractor::class.java)
                    if (contractor != null) {
                        textContractorName.text = "${contractor.name} ${contractor.lastname}"
                        if (contractor.companyName.isNotEmpty()) {
                            textCompanyName.text = contractor.companyName
                            textCompanyName.visibility = View.VISIBLE
                        } else {
                            textCompanyName.visibility = View.GONE
                        }
                    }
                } else {
                    textContractorName.text = "Perfil no encontrado"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DetailActivity", "Error al cargar perfil: ${error.message}")
                textContractorName.text = "Error al cargar perfil"
            }
        })
    }
}