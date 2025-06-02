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

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_contractors)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val categories = listOf(
            "Plomeros", "Electricistas", "Albañiles", "Mecánicos", "Técnicos en refrigeración",
            "Técnicos en computadoras", "Herreros", "Limpieza de hogar", "Jardineros",
            "Agentes inmobiliarios", "Médicos", "Asesorías escolares", "Músicos", "Animación para eventos"
        )

        val adapter = CategoryAdapter(categories) { category ->
            val intent = Intent(this@DirectoryActivity, ContractorDetailActivity::class.java)
            intent.putExtra("CATEGORY", category)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        val btnExit = findViewById<Button>(R.id.btn_exit)
        btnExit.setOnClickListener {
            Toast.makeText(this, "Cerrando la aplicación", Toast.LENGTH_SHORT).show()
            finishAffinity() // Cierra todas las actividades y finaliza la app
        }
    }
}
