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
// 📌 NOTA: Asegúrate de tener la clase CategoryAdapter en la ruta correcta.

class DirectoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_directory)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_categories) // Actualizar al nuevo ID
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 📌 CAMBIO 1: Estandarizar los nombres de las categorías.
        // Deben ser idénticos a los que usas en el Spinner de registro (en singular).
        // Esto es VITAL para que la búsqueda en la base de datos funcione después.
        val categories = listOf(
            "Albañil", "Electricista", "Plomero", "Carpintero", "Pintor",
            "Mecánico", "Técnico en refrigeración", "Técnico en computadoras",
            "Herrero", "Limpieza de hogar", "Jardinero", "Agente inmobiliario",
            "Médico", "Asesoría escolar", "Músico", "Animación para eventos", "Otro"
        )

        val adapter = CategoryAdapter(categories) { category ->
            // 📌 CAMBIO 2: La actividad que se abre.
            // Ya no abre el "Detalle" directamente. Ahora abre la "Lista de Contratistas".
            val intent = Intent(this@DirectoryActivity, ContractorListActivity::class.java)

            // 📌 CAMBIO 3: La "llave" para pasar el dato.
            // Usamos una llave clara como "CATEGORY_NAME" para pasar el nombre de la categoría.
            intent.putExtra("CATEGORY_NAME", category)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        val btnExit = findViewById<Button>(R.id.btn_exit)
        btnExit.setOnClickListener {
            Toast.makeText(this, "Cerrando la aplicación", Toast.LENGTH_SHORT).show()
            finishAffinity()
        }
    }
}