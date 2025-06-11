package com.javipena.conexiondeoficios.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location // 📌 Añadida para el tipo de dato
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices // 📌 Importación añadida
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.javipena.conexiondeoficios.Ad
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.adapters.AdsAdapter

class NearbyAdsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adsAdapter: AdsAdapter
    private val nearbyAdsList = mutableListOf<Ad>()

    private lateinit var progressBar: ProgressBar
    private lateinit var textNoResults: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_ads)
        title = "Anuncios Cercanos"

        recyclerView = findViewById(R.id.recycler_nearby_ads)
        progressBar = findViewById(R.id.progress_bar_nearby)
        textNoResults = findViewById(R.id.text_no_results_nearby)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adsAdapter = AdsAdapter(nearbyAdsList)
        recyclerView.adapter = adsAdapter

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndFindAds()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndFindAds()
        } else {
            Toast.makeText(this, "El permiso de ubicación es necesario para buscar anuncios cercanos.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndFindAds() {
        progressBar.visibility = View.VISIBLE
        textNoResults.visibility = View.GONE

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // 📌 CORRECCIÓN 1: 'userLocation' renombrado a 'location' para consistencia
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                findNearbyContractors(location)
            } else {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "No se pudo obtener la ubicación. Actívala y prueba de nuevo.", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun findNearbyContractors(userLocation: Location) {
        val contractorsRef = FirebaseDatabase.getInstance().getReference("Users")
        val radiusKm = 5.5

        contractorsRef.orderByChild("userType").equalTo("contractor")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        progressBar.visibility = View.GONE
                        textNoResults.visibility = View.VISIBLE
                        return
                    }

                    val nearbyContractorIds = mutableListOf<String>()

                    for (contractorSnapshot in snapshot.children) {
                        val latStr = contractorSnapshot.child("latitude").getValue(String::class.java)
                        val lonStr = contractorSnapshot.child("longitude").getValue(String::class.java)
                        val contractorId = contractorSnapshot.key

                        if (latStr != null && lonStr != null && contractorId != null) {
                            val contractorLat = latStr.toDoubleOrNull()
                            val contractorLon = lonStr.toDoubleOrNull()

                            if (contractorLat != null && contractorLon != null) {
                                val results = FloatArray(1)
                                Location.distanceBetween(
                                    userLocation.latitude, userLocation.longitude,
                                    contractorLat, contractorLon,
                                    results
                                )
                                val distanceInMeters = results[0]

                                if ((distanceInMeters / 1000) <= radiusKm) {
                                    nearbyContractorIds.add(contractorId)
                                }
                            }
                        }
                    }
                    if (nearbyContractorIds.isNotEmpty()) {
                        fetchAdsForContractors(nearbyContractorIds)
                    } else {
                        progressBar.visibility = View.GONE
                        textNoResults.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@NearbyAdsActivity, "Error al leer datos: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchAdsForContractors(contractorIds: List<String>) {
        val adsRef = FirebaseDatabase.getInstance().getReference("Ads")
        var fetchedCount = 0

        if (contractorIds.isEmpty()){
            progressBar.visibility = View.GONE
            textNoResults.visibility = View.VISIBLE
            return
        }

        for (id in contractorIds) {
            adsRef.orderByChild("contractorId").equalTo(id).limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (adSnapshot in snapshot.children) {
                                val ad = adSnapshot.getValue(Ad::class.java)
                                if (ad != null) {
                                    nearbyAdsList.add(ad)
                                }
                            }
                        }

                        fetchedCount++
                        if (fetchedCount == contractorIds.size) {
                            progressBar.visibility = View.GONE
                            if (nearbyAdsList.isEmpty()) {
                                textNoResults.visibility = View.VISIBLE
                            } else {
                                adsAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        fetchedCount++
                        if (fetchedCount == contractorIds.size) {
                            progressBar.visibility = View.GONE
                        }
                    }
                })
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}