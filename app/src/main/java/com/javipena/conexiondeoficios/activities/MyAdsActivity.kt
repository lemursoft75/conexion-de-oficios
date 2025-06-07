package com.javipena.conexiondeoficios.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.javipena.conexiondeoficios.Ad
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.adapters.MyAdsAdapter

class MyAdsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var textNoAds: TextView
    private lateinit var myAdsAdapter: MyAdsAdapter

    // 📌 Guardaremos el ID del anuncio junto con el objeto Ad. Esto es clave para el borrado.
    private val adsList = mutableListOf<Pair<String, Ad>>()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_ads)

        title = "Mis Anuncios Publicados"

        recyclerView = findViewById(R.id.recycler_my_ads)
        progressBar = findViewById(R.id.progress_bar_my_ads)
        textNoAds = findViewById(R.id.text_no_ads)

        setupRecyclerView()
        fetchUserAds()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        // El adapter recibirá una función para manejar el clic en el botón de borrar.
        myAdsAdapter = MyAdsAdapter(adsList) { adId, ad, position ->
            showDeleteConfirmationDialog(adId, ad, position)
        }
        recyclerView.adapter = myAdsAdapter
    }

    private fun fetchUserAds() {
        progressBar.visibility = View.VISIBLE
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Error: No se pudo verificar el usuario.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        // 📌 Consulta a Firebase para obtener solo los anuncios del usuario actual.
        val query = database.getReference("Ads").orderByChild("contractorId").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                adsList.clear()
                if (snapshot.exists()) {
                    for (adSnapshot in snapshot.children) {
                        val ad = adSnapshot.getValue(Ad::class.java)
                        val adId = adSnapshot.key // Obtenemos el ID único del anuncio
                        if (ad != null && adId != null) {
                            adsList.add(Pair(adId, ad))
                        }
                    }
                    myAdsAdapter.notifyDataSetChanged()
                    textNoAds.visibility = View.GONE
                } else {
                    textNoAds.visibility = View.VISIBLE
                }
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@MyAdsActivity, "Error al cargar anuncios: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmationDialog(adId: String, ad: Ad, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar este anuncio? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { dialog, _ ->
                deleteAd(adId, ad, position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(R.drawable.ic_delete) // Necesitarás un ícono de basura (ic_delete)
            .show()
    }

    private fun deleteAd(adId: String, ad: Ad, position: Int) {
        // 1. Borrar de Firebase Database
        database.getReference("Ads").child(adId).removeValue()
            .addOnSuccessListener {
                // 2. Si hay imagen, borrar de Firebase Storage
                if (!ad.mediaUrl.isNullOrEmpty()) {
                    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(ad.mediaUrl)
                    storageRef.delete().addOnFailureListener {
                        Log.e("MyAdsActivity", "Error al borrar imagen de Storage: ${it.message}")
                    }
                }

                // 3. Actualizar la UI localmente
                adsList.removeAt(position)
                myAdsAdapter.notifyItemRemoved(position)
                myAdsAdapter.notifyItemRangeChanged(position, adsList.size)
                Toast.makeText(this, "Anuncio eliminado correctamente.", Toast.LENGTH_SHORT).show()

                if (adsList.isEmpty()) {
                    textNoAds.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar el anuncio.", Toast.LENGTH_SHORT).show()
            }
    }
}