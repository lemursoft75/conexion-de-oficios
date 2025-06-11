package com.javipena.conexiondeoficios.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.javipena.conexiondeoficios.Ad
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.adapters.ReviewAdapter
import com.javipena.conexiondeoficios.models.Contractor
import com.javipena.conexiondeoficios.models.Review

class ContractorDetailActivity : AppCompatActivity() {

    // Vistas de la UI
    private lateinit var textContractorName: TextView
    private lateinit var textCompanyName: TextView
    private lateinit var textAdDescription: TextView
    private lateinit var imageAdDetail: ImageView
    private lateinit var ratingBarAverage: RatingBar
    private lateinit var textReviewCount: TextView
    private lateinit var recyclerReviews: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var textPhone: TextView
    private lateinit var imageMap: ImageView
    private lateinit var btnWhatsApp: Button
    private lateinit var btnLeaveReview: Button
    private lateinit var btnBack: Button

    // Lista de datos
    private val reviewList = mutableListOf<Pair<String, Review>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contractor_detail)
        title = "Detalle del Anuncio"

        val ad = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("AD_DETAIL", Ad::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Ad>("AD_DETAIL")
        }

        if (ad == null) {
            Toast.makeText(this, "Error al cargar los datos del anuncio.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // --- Vinculación de todas las Vistas ---
        textContractorName = findViewById(R.id.text_contractor_name)
        textCompanyName = findViewById(R.id.text_company_name)
        textAdDescription = findViewById(R.id.text_ad_description)
        imageAdDetail = findViewById(R.id.image_ad_detail)
        textPhone = findViewById(R.id.text_phone)
        imageMap = findViewById(R.id.image_map)
        btnWhatsApp = findViewById(R.id.btn_whatsapp)
        btnLeaveReview = findViewById(R.id.btn_leave_review)
        btnBack = findViewById(R.id.btn_back)
        ratingBarAverage = findViewById(R.id.rating_bar_average)
        textReviewCount = findViewById(R.id.text_review_count)
        recyclerReviews = findViewById(R.id.recycler_reviews)

        // --- Configuración Inicial de la UI ---
        setupRecyclerView(ad.contractorId)
        textAdDescription.text = ad.adText
        textPhone.text = "Teléfono: ${ad.phone}"

        if (!ad.mediaUrl.isNullOrEmpty()) {
            imageAdDetail.visibility = View.VISIBLE
            Glide.with(this).load(ad.mediaUrl).into(imageAdDetail)
        } else {
            imageAdDetail.visibility = View.GONE
        }

        if (ad.contractorId.isNotEmpty()) {
            fetchContractorProfile(ad.contractorId)
            fetchReviews(ad.contractorId)
        } else {
            textContractorName.text = "Contratista Anónimo"
        }

        setupButtons(ad)
        setupReviewButton(ad.contractorId)
    }

    private fun setupRecyclerView(contractorId: String) {
        recyclerReviews.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter(reviewList, contractorId)
        recyclerReviews.adapter = reviewAdapter
    }

    private fun setupButtons(ad: Ad) {
        btnWhatsApp.setOnClickListener {
            val phoneNumber = ad.phone.replace(Regex("[^0-9]"), "")
            val url = "https://wa.me/$phoneNumber"
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                Toast.makeText(this, "No se pudo abrir WhatsApp.", Toast.LENGTH_SHORT).show()
            }
        }
        imageMap.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:${ad.latitude},${ad.longitude}?q=${ad.latitude},${ad.longitude}(${textContractorName.text})")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            try {
                startActivity(mapIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "No se encontró la aplicación de Google Maps.", Toast.LENGTH_LONG).show()
            }
        }
        btnBack.setOnClickListener { finish() }
    }

    private fun setupReviewButton(contractorId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && !currentUser.isAnonymous) {
            btnLeaveReview.visibility = View.VISIBLE
            btnLeaveReview.setOnClickListener { showLeaveReviewDialog(contractorId) }
        } else {
            btnLeaveReview.visibility = View.GONE
        }
    }

    /**
     * Obtiene los datos del perfil del contratista y actualiza la UI.
     * Usa un listener en tiempo real para que la calificación se actualice sola.
     */
    private fun fetchContractorProfile(userId: String) {
        FirebaseDatabase.getInstance().getReference("Users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val contractor = snapshot.getValue(Contractor::class.java)
                    if (contractor != null) {
                        textContractorName.text = "${contractor.name} ${contractor.lastname}"
                        textCompanyName.text = contractor.companyName
                        ratingBarAverage.rating = contractor.averageRating.toFloat()
                        textReviewCount.text = "(${contractor.reviewCount} opiniones)"
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("DetailActivity", "Error al cargar perfil: ${error.message}")
                }
            })
    }

    /**
     * Obtiene la lista de reseñas y actualiza el RecyclerView en tiempo real.
     */
    private fun fetchReviews(contractorId: String) {
        FirebaseDatabase.getInstance().getReference("Users").child(contractorId).child("reviews")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    reviewList.clear()
                    for (reviewSnapshot in snapshot.children) {
                        val review = reviewSnapshot.getValue(Review::class.java)
                        val reviewId = reviewSnapshot.key
                        if (review != null && reviewId != null) {
                            reviewList.add(Pair(reviewId, review))
                        }
                    }
                    reviewAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("DetailActivity", "Error al cargar reseñas: ${error.message}")
                }
            })
    }

    /**
     * Muestra el diálogo para que un usuario registrado deje su calificación y comentario.
     */
    private fun showLeaveReviewDialog(contractorId: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_leave_review, null)
        builder.setView(dialogView)

        val ratingBar = dialogView.findViewById<RatingBar>(R.id.rating_bar_review)
        val editTextComment = dialogView.findViewById<EditText>(R.id.edit_text_comment)

        builder.setTitle("Tu Opinión es Importante")
        builder.setPositiveButton("Publicar") { dialog, _ ->
            val rating = ratingBar.rating.toDouble()
            val comment = editTextComment.text.toString().trim()
            val currentUser = FirebaseAuth.getInstance().currentUser
            val clientId = currentUser?.uid
            val clientName = currentUser?.displayName ?: "Cliente"

            if (clientId != null && rating > 0) {
                val reviewData = Review(
                    clientId = clientId,
                    clientName = clientName,
                    rating = rating,
                    comment = comment,
                    timestamp = System.currentTimeMillis()
                )

                FirebaseDatabase.getInstance().getReference("Users")
                    .child(contractorId)
                    .child("reviews")
                    .push().setValue(reviewData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "¡Gracias por tu opinión!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        // Aquí es donde una Cloud Function se encargaría de actualizar el promedio.
                        // Por ahora, la nueva reseña aparecerá, pero el promedio no se actualizará hasta que el
                        // usuario vuelva a entrar a la pantalla.
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al publicar la reseña.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Por favor, selecciona una calificación (mínimo media estrella).", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.create().show()
    }
}