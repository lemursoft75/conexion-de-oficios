package com.javipena.conexiondeoficios.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.javipena.conexiondeoficios.Ad
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.adapters.AdsAdapter
import android.util.Log // 🚨 ¡Asegúrate de que esta línea esté al inicio!

class ContractorListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noResultsText: TextView
    private lateinit var adsAdapter: AdsAdapter

    // Lista visible para el adaptador (contiene datos ordenados/filtrados)
    private val adsList = mutableListOf<Ad>()

    // Lista para guardar los datos crudos de Firebase (sin ordenar, solo filtrados por categoría)
    private val rawAdsList = mutableListOf<Ad>()

    private lateinit var spinnerSort: Spinner
    private var currentCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contractor_list)

        currentCategory = intent.getStringExtra("CATEGORY_NAME")

        if (currentCategory == null) {
            Toast.makeText(this, "Error: No se especificó una categoría.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        title = currentCategory

        setupViews()
        setupSpinner()

        // Cargar los anuncios y guardarlos en rawAdsList
        fetchAdsByCategory(currentCategory!!)
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recycler_ads)
        progressBar = findViewById(R.id.progress_bar)
        noResultsText = findViewById(R.id.text_no_results)
        spinnerSort = findViewById(R.id.spinner_sort_options)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adsAdapter = AdsAdapter(adsList)
        recyclerView.adapter = adsAdapter
    }

    private fun setupSpinner() {
        // Configuramos el Listener para el Spinner
        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // position 0 = Por Defecto, position 1 = Mejores Reseñas
                // 🚨 Aplicar ordenamiento CADA VEZ que el usuario selecciona una opción
                applySorting(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se hace nada
            }
        }
    }

    /**
     * Carga los anuncios de la categoría, y luego busca el rating más reciente en el perfil del contratista (/Users).
     *
     * ⚠️ NOTA: Esta implementación realiza una consulta adicional a Firebase por cada anuncio (N+1 problema),
     * lo cual garantiza la exactitud del rating para el ordenamiento.
     */
    private fun fetchAdsByCategory(category: String) {
        progressBar.visibility = View.VISIBLE
        noResultsText.visibility = View.GONE
        recyclerView.visibility = View.GONE

        val databaseRef = FirebaseDatabase.getInstance().getReference("Ads")
        val query = databaseRef.orderByChild("specialty").equalTo(category)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                rawAdsList.clear()

                if (snapshot.exists()) {
                    val totalAds = snapshot.childrenCount.toInt()

                    // Si no hay anuncios o están vacíos, terminamos la carga
                    if (totalAds == 0) {
                        applySorting(spinnerSort.selectedItemPosition)
                        progressBar.visibility = View.GONE
                        return
                    }

                    var loadedCount = 0
                    val userRef = FirebaseDatabase.getInstance().getReference("Users")

                    for (adSnapshot in snapshot.children) {
                        val adBase = adSnapshot.getValue(Ad::class.java)

                        if (adBase != null) {
                            // 🚨 PASO 1: Usamos el ID del contratista para buscar su rating fresco en /Users
                            // ASUMIMOS que adBase tiene un campo 'contractorId' o 'userId'
                            userRef.child(adBase.contractorId)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {

                                        // Leemos el rating real de la fuente de verdad (/Users)
                                        val freshRating = userSnapshot.child("averageRating").getValue(Double::class.java) ?: 0.0
                                        val freshCount = userSnapshot.child("reviewCount").getValue(Int::class.java) ?: 0

                                        // Creamos el Ad final con el rating CORREGIDO de /Users
                                        val ad = adBase.copy(
                                            averageRating = freshRating,
                                            reviewCount = freshCount
                                        )
                                        rawAdsList.add(ad)

                                        // ⚠️ PASO 2: Usamos un contador para saber cuándo todas las búsquedas asíncronas han terminado
                                        loadedCount++
                                        if (loadedCount == totalAds) {
                                            Log.d("ORDEN_CORRECCION", "Todas las calificaciones de /Users cargadas. Aplicando ordenamiento.")
                                            applySorting(spinnerSort.selectedItemPosition)
                                            progressBar.visibility = View.GONE
                                        }
                                    }
                                    // Manejo de errores
                                    override fun onCancelled(error: DatabaseError) {
                                        // Si falla la búsqueda del usuario, cargamos el ad con el rating 0.0 obsoleto
                                        loadedCount++
                                        if (loadedCount == totalAds) {
                                            applySorting(spinnerSort.selectedItemPosition)
                                            progressBar.visibility = View.GONE
                                        }
                                    }
                                })
                        } else {
                            // Si el adBase es nulo, también contamos para no bloquear el cargador
                            loadedCount++
                            if (loadedCount == totalAds) {
                                applySorting(spinnerSort.selectedItemPosition)
                                progressBar.visibility = View.GONE
                            }
                        }
                    }
                } else {
                    // No hay anuncios
                    applySorting(spinnerSort.selectedItemPosition)
                    progressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ContractorListActivity, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Aplica el ordenamiento según la selección del Spinner (0=Defecto, 1=Reseñas) y actualiza el RecyclerView.
     */
    private fun applySorting(sortOption: Int) {
        if (rawAdsList.isEmpty()) {
            adsList.clear()
            adsAdapter.notifyDataSetChanged()
            noResultsText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }

        val sortedList: List<Ad> = when (sortOption) {
            1 -> { // Opción: Mejores Reseñas
                // 🚨 ORDENAMIENTO CONTROLADO POR EL USUARIO
                rawAdsList.sortedWith(
                    compareByDescending<Ad> { it.averageRating }
                        .thenByDescending { it.reviewCount }
                )
            }
            0 -> { // Opción: Por Defecto (Mantener el orden en que se cargaron los datos de Firebase)
                rawAdsList.toList()
            }
            else -> rawAdsList.toList()
        }

        adsList.clear()
        adsList.addAll(sortedList)
        adsAdapter.notifyDataSetChanged()

        recyclerView.visibility = View.VISIBLE
        noResultsText.visibility = View.GONE
    }
}