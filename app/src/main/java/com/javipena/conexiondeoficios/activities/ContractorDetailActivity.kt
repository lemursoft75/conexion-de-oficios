package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.javipena.conexiondeoficios.Ad
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.models.Contractor
import android.util.Log

class ContractorDetailActivity : AppCompatActivity() {

    private lateinit var textContractorName: TextView
    private lateinit var textCompanyName: TextView
    private lateinit var textAdDescription: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contractor_detail)

        // 📌 PASO 1: Recibir el objeto 'Ad' completo que se envió desde la lista.
        // Nota: Es importante que tu clase 'Ad' sea 'Parcelable' para que esto funcione.
        val ad = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("AD_DETAIL", Ad::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Ad>("AD_DETAIL")
        }

        // Si por alguna razón el anuncio no llega, mostramos un error y cerramos.
        if (ad == null) {
            Toast.makeText(this, "Error: No se pudieron cargar los datos del anuncio.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Referencias a las vistas del layout (asegúrate de que los IDs coincidan en tu XML)
        textContractorName = findViewById(R.id.text_contractor_name)
        textCompanyName = findViewById(R.id.text_company_name)
        textAdDescription = findViewById(R.id.text_ad_description)
        val textPhone = findViewById<TextView>(R.id.text_phone)
        val imageMap = findViewById<ImageView>(R.id.image_map)
        val btnWhatsApp = findViewById<Button>(R.id.btn_whatsapp)
        val btnBack = findViewById<Button>(R.id.btn_back)

        // 📌 PASO 2: Mostrar la información que ya tenemos del objeto 'Ad'.
        textAdDescription.text = ad.adText
        textPhone.text = "Teléfono: ${ad.phone}"

        // 📌 PASO 3: Usar el 'contractorId' del anuncio para buscar el perfil completo.
        if (ad.contractorId.isNotEmpty()) {
            fetchContractorProfile(ad.contractorId)
        } else {
            textContractorName.text = "Contratista Anónimo"
        }

        // --- Acciones de los Botones con Datos Reales ---

        // Acción para abrir WhatsApp con el número REAL del anuncio.
        btnWhatsApp.setOnClickListener {
            val phoneNumber = ad.phone.replace(Regex("[^0-9]"), "") // Limpiar el número
            val url = "https://wa.me/$phoneNumber"
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                Toast.makeText(this, "No se pudo abrir WhatsApp.", Toast.LENGTH_SHORT).show()
            }
        }

        // Acción para abrir el mapa con las coordenadas REALES.
        imageMap.setOnClickListener {
            val uri = "geo:${ad.latitude},${ad.longitude}?q=${ad.latitude},${ad.longitude}(Ubicación)"
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            // Comprueba si hay una app de mapas que pueda manejar el intent
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(this, "No se encontró una aplicación de mapas.", Toast.LENGTH_SHORT).show()
            }
        }

        // Acción para regresar a la lista anterior.
        btnBack.setOnClickListener {
            finish()
        }
    }

    /**
     * Busca en la base de datos el perfil del contratista usando su ID.
     */
    private fun fetchContractorProfile(userId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val contractor = snapshot.getValue(Contractor::class.java)
                    if (contractor != null) {
                        // 📌 PASO 4: Actualizar la UI con los datos del perfil encontrado.
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