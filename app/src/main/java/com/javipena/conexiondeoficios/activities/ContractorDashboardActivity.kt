package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.javipena.conexiondeoficios.R

class ContractorDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contractor_dashboard)

        // Inicialización de botones
        val btnCreateAd: Button = findViewById(R.id.btn_create_ad)
        val btnViewMyAds: Button = findViewById(R.id.btn_view_my_ads)
        val btnEditProfile: Button = findViewById(R.id.btn_edit_profile)
        val btnLogout: Button = findViewById(R.id.btn_logout)

        // --- NUEVO BOTÓN PARA EL ESCÁNER ---
        val btnScanQr: Button = findViewById(R.id.btn_scan_client_qr)

        // Lógica para publicar anuncio
        btnCreateAd.setOnClickListener {
            startActivity(Intent(this, PublicationActivity::class.java))
        }

        // Lógica para ver/editar mis anuncios
        btnViewMyAds.setOnClickListener {
            startActivity(Intent(this, MyAdsActivity::class.java))
        }

        // Lógica para editar perfil del contratista
        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // --- LÓGICA PARA ABRIR EL ESCÁNER ---
        btnScanQr.setOnClickListener {
            // Abre la actividad que configuramos con la cámara
            startActivity(Intent(this, ScannerActivity::class.java))
        }

        // Botón para cerrar sesión
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}