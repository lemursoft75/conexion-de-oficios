package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.adapters.CategoryAdapter

class DirectoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_directory)

        // Configuración de la Toolbar personalizada
        val toolbar: Toolbar = findViewById(R.id.toolbar_directory)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Ocultamos el título por defecto

        // Lógica para la lista de categorías por especialidad
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

        // Lógica para el botón de búsqueda cercana
        val btnFindNearby = findViewById<Button>(R.id.btn_find_nearby)
        btnFindNearby.setOnClickListener {
            startActivity(Intent(this, NearbyAdsActivity::class.java))
        }

        // 📌 LÓGICA AÑADIDA PARA EL AVISO DE RESPONSABILIDAD
        val textDisclaimer = findViewById<TextView>(R.id.text_disclaimer)
        textDisclaimer.setOnClickListener {
            showDisclaimerDialog()
        }
    }

    /**
     * Esta nueva función crea y muestra el diálogo con el aviso.
     */
    private fun showDisclaimerDialog() {
        AlertDialog.Builder(this)
            .setTitle("Aviso de Responsabilidad")
            .setMessage(
                "\"Conexión de Oficios\" es una plataforma de enlace que facilita la conexión entre clientes y proveedores de servicios. No verificamos ni garantizamos la calidad, cumplimiento o desempeño de los trabajos realizados por los contratistas. La responsabilidad de cada servicio contratado recae exclusivamente en las partes involucradas.\n\n" +
                        "Asimismo, nos reservamos el derecho de admisión de solicitudes de contratistas para garantizar el buen funcionamiento de la plataforma y la experiencia de los usuarios."
            )
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Métodos para manejar el menú de opciones (sin cambios)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.directory_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_edit_profile -> {
                startActivity(Intent(this, EditClientProfileActivity::class.java))
                true
            }
            R.id.menu_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}