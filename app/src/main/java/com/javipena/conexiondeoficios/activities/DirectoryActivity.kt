package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
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
import com.javipena.conexiondeoficios.adapters.CategoryItem

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
        recyclerView.layoutManager = GridLayoutManager(this, 3) // 3 columnas
        val categories = listOf(
            CategoryItem("Albañil", R.drawable.ic_construction),
            CategoryItem("Electricista", R.drawable.ic_bolt),
            CategoryItem("Plomero", R.drawable.ic_plumbing),
            CategoryItem("Carpintero", R.drawable.ic_carpenter),
            CategoryItem("Cerrajero", R.drawable.ic_cerrajeria),
            CategoryItem("Mecánico", R.drawable.ic_mechanic),
            CategoryItem("Técnico en refrigeración", R.drawable.ic_ac_unit),
            CategoryItem("Técnico en computadoras", R.drawable.ic_computer),
            CategoryItem("Herrero", R.drawable.ic_welding),
            CategoryItem("Limpieza de hogar", R.drawable.ic_cleaning),
            CategoryItem("Jardinero", R.drawable.ic_gardening),
            CategoryItem("Agente inmobiliario", R.drawable.ic_real_estate),
            CategoryItem("Médico", R.drawable.ic_medical),
            CategoryItem("Pintor", R.drawable.ic_painter),
            CategoryItem("Músico", R.drawable.ic_music),
            CategoryItem("Animación para eventos", R.drawable.ic_celebration),
            CategoryItem("Otro", R.drawable.ic_more)
        )

        // El adaptador ahora recibe esta nueva lista de objetos
        val categoryAdapter = CategoryAdapter(categories) { categoryItem ->
            val intent = Intent(this, ContractorListActivity::class.java)
            intent.putExtra("CATEGORY_NAME", categoryItem.name) // Pasamos solo el nombre a la siguiente actividad
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

        val btnChat = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btn_chatbot)

        btnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
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