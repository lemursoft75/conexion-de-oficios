package com.javipena.conexiondeoficios.activities

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
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

class ContractorListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var noResultsText: TextView
    private lateinit var adsAdapter: AdsAdapter
    private val adsList = mutableListOf<Ad>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contractor_list)

        // Recibir la categoría pasada desde DirectoryActivity
        val category = intent.getStringExtra("CATEGORY_NAME")

        if (category == null) {
            Toast.makeText(this, "Error: No se especificó una categoría.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        title = category // Pone el nombre de la categoría en la barra de título

        recyclerView = findViewById(R.id.recycler_ads)
        progressBar = findViewById(R.id.progress_bar)
        noResultsText = findViewById(R.id.text_no_results)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adsAdapter = AdsAdapter(adsList)
        recyclerView.adapter = adsAdapter

        // Función para buscar los anuncios en Firebase
        fetchAdsByCategory(category)
    }

    private fun fetchAdsByCategory(category: String) {
        progressBar.visibility = View.VISIBLE
        noResultsText.visibility = View.GONE
        recyclerView.visibility = View.GONE

        val databaseRef = FirebaseDatabase.getInstance().getReference("Ads")
        val query = databaseRef.orderByChild("specialty").equalTo(category)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                adsList.clear()

                if (snapshot.exists()) {
                    for (adSnapshot in snapshot.children) {
                        val ad = adSnapshot.getValue(Ad::class.java)
                        if (ad != null) {
                            adsList.add(ad)
                        }
                    }

                    adsAdapter.notifyDataSetChanged()
                    recyclerView.visibility = View.VISIBLE
                    noResultsText.visibility = View.GONE

                } else {
                    noResultsText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }

                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ContractorListActivity, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}