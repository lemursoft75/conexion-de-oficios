package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.adapters.CategoryAdapter

class DirectoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_directory)

        // --- Lógica para la lista de categorías por especialidad ---
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_categories)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val categories = listOf(
            "Albañil", "Electricista", "Plomero", "Carpintero", "Pintor",
            "Mecánico", "Técnico en refrigeración", "Técnico en computadoras",
            "Herrero", "Limpieza de hogar", "Jardinero", "Agente inmobiliario",
            "Médico", "Asesoría escolar", "Músico", "Animación para eventos", "Otro"
        )

        val categoryAdapter = CategoryAdapter(categories) { category ->
            val intent = Intent(this, ContractorListActivity::class.java)
            intent.putExtra("CATEGORY_NAME", category)
            startActivity(intent)
        }
        recyclerView.adapter = categoryAdapter


        // --- Lógica para el botón de búsqueda cercana ---
        val btnFindNearby = findViewById<Button>(R.id.btn_find_nearby)
        btnFindNearby.setOnClickListener {
            // Inicia la actividad que busca por geolocalización
            startActivity(Intent(this, NearbyAdsActivity::class.java))
        }


        // --- Lógica para el botón de salir ---
        val btnExit = findViewById<Button>(R.id.btn_exit)
        btnExit.setOnClickListener {
            Toast.makeText(this, "Cerrando la aplicación", Toast.LENGTH_SHORT).show()
            finishAffinity()
        }
    }
}