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

        val btnCreateAd: Button = findViewById(R.id.btn_create_ad)
        val btnViewMyAds: Button = findViewById(R.id.btn_view_my_ads)
        val btnLogout: Button = findViewById(R.id.btn_logout)

        // Botón para ir a la pantalla de crear anuncio
        btnCreateAd.setOnClickListener {
            startActivity(Intent(this, PublicationActivity::class.java))
        }

        // 📌 ¡AQUÍ VA EL CÓDIGO!
        // Botón para ir a la pantalla de "Mis Anuncios"
        btnViewMyAds.setOnClickListener {
            startActivity(Intent(this, MyAdsActivity::class.java))
        }

        // Botón para cerrar sesión y volver al Login
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}