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
import com.google.firebase.database.*
import com.javipena.conexiondeoficios.Ad
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.adapters.ReviewAdapter
import com.javipena.conexiondeoficios.models.Contractor
import com.javipena.conexiondeoficios.models.Review

class ContractorDetailActivity : AppCompatActivity() {

    // Vistas
    private lateinit var textContractorName: TextView
    private lateinit var textCompanyName: TextView
    private lateinit var textAdDescription: TextView
    private lateinit var imageAdDetail: ImageView
    private lateinit var videoAdDetail: VideoView
    private lateinit var ratingBarAverage: RatingBar
    private lateinit var textReviewCount: TextView
    private lateinit var recyclerReviews: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var textPhone: TextView
    private lateinit var imageMap: ImageView
    private lateinit var btnWhatsApp: Button
    private lateinit var btnLeaveReview: Button
    private lateinit var btnBack: Button

    // Lista de reseñas
    private val reviewList = mutableListOf<Pair<String, Review>>()

    // Datos cargados del contratista
    private var contractorPhone: String = ""
    private var contractorLat: String = ""
    private var contractorLon: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contractor_detail)
        title = "Detalle del Servicio"

        setupViews()

        // 1. Intentamos obtener el ID directo (desde MainActivity)
        val directId = intent.getStringExtra("CONTRACTOR_ID")

        // 2. Intentamos obtener el objeto Ad (desde la lista de anuncios)
        val ad = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("AD_DETAIL", Ad::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Ad>("AD_DETAIL")
        }

        // 3. Decidimos qué cargar
        when {
            directId != null -> {
                // Caso: Venimos de la alerta de pago
                setupRecyclerView(directId)
                fetchContractorProfile(directId)
                fetchReviews(directId)
                setupReviewButton(directId)
            }
            ad != null -> {
                // Caso: Venimos de la navegación normal
                setupRecyclerView(ad.contractorId)
                populateUI(ad) // Esto ya llama a fetchProfile y fetchReviews
                setupReviewButton(ad.contractorId)
            }
            else -> {
                Toast.makeText(this, "Error al cargar datos.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        setupButtons()
    }

    private fun setupViews() {
        textContractorName = findViewById(R.id.text_contractor_name)
        textCompanyName = findViewById(R.id.text_company_name)
        textAdDescription = findViewById(R.id.text_ad_description)
        imageAdDetail = findViewById(R.id.image_ad_detail)
        videoAdDetail = findViewById(R.id.video_ad_detail)
        textPhone = findViewById(R.id.text_phone)
        imageMap = findViewById(R.id.image_map)
        btnWhatsApp = findViewById(R.id.btn_whatsapp)
        btnLeaveReview = findViewById(R.id.btn_leave_review)
        btnBack = findViewById(R.id.btn_back)
        ratingBarAverage = findViewById(R.id.rating_bar_average)
        textReviewCount = findViewById(R.id.text_review_count)
        recyclerReviews = findViewById(R.id.recycler_reviews)
    }

    private fun setupRecyclerView(contractorId: String) {
        recyclerReviews.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter(reviewList, contractorId)
        recyclerReviews.adapter = reviewAdapter
    }

    private fun populateUI(ad: Ad) {
        textAdDescription.text = ad.adText

        // Mostrar imagen o video
        val mediaUrl = ad.mediaUrl
        if (!mediaUrl.isNullOrEmpty()) {
            if (mediaUrl.contains("/video/")) {
                imageAdDetail.visibility = View.GONE
                videoAdDetail.visibility = View.VISIBLE
                videoAdDetail.setVideoPath(mediaUrl)
                videoAdDetail.setOnPreparedListener { it.isLooping = true }
                videoAdDetail.start()
            } else {
                videoAdDetail.visibility = View.GONE
                imageAdDetail.visibility = View.VISIBLE
                Glide.with(this).load(mediaUrl).into(imageAdDetail)
            }
        } else {
            imageAdDetail.visibility = View.GONE
            videoAdDetail.visibility = View.GONE
        }

        if (ad.contractorId.isNotEmpty()) {
            fetchContractorProfile(ad.contractorId)
            fetchReviews(ad.contractorId)
        } else {
            textContractorName.text = "Contratista Anónimo"
        }
    }

    private fun setupButtons() {
        btnWhatsApp.setOnClickListener {
            if (contractorPhone.isEmpty()) {
                Toast.makeText(this, "El contratista no tiene teléfono registrado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val phoneNumber = contractorPhone.replace(Regex("[^0-9]"), "")
            val url = "https://wa.me/$phoneNumber"

            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                Toast.makeText(this, "No se pudo abrir WhatsApp.", Toast.LENGTH_SHORT).show()
            }
        }

        imageMap.setOnClickListener {
            if (contractorLat.isEmpty() || contractorLon.isEmpty()) {
                Toast.makeText(this, "Ubicación no disponible.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uri = Uri.parse("geo:$contractorLat,$contractorLon?q=$contractorLat,$contractorLon(${textContractorName.text})")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")

            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Google Maps no está instalado.", Toast.LENGTH_LONG).show()
            }
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun setupReviewButton(contractorId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && !currentUser.isAnonymous) {
            btnLeaveReview.visibility = View.VISIBLE
            // Llamamos a la función que define qué pasa al hacer clic
            checkReviewPermission(contractorId)
        } else {
            btnLeaveReview.visibility = View.GONE
        }
    }

    private fun fetchContractorProfile(userId: String) {
        FirebaseDatabase.getInstance().getReference("Users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val contractor = snapshot.getValue(Contractor::class.java)

                    if (contractor != null) {
                        textContractorName.text = "${contractor.name} ${contractor.lastname}"
                        textCompanyName.text = contractor.companyName
                        textPhone.text = "Teléfono: ${contractor.phone}"
                        ratingBarAverage.rating = contractor.averageRating.toFloat()
                        textReviewCount.text = "(${contractor.reviewCount} opiniones)"

                        // Guardar para WhatsApp y Maps
                        contractorPhone = contractor.phone
                        contractorLat = contractor.latitude
                        contractorLon = contractor.longitude
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DetailActivity", "Error al cargar perfil: ${error.message}")
                }
            })
    }

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

                    // 🚨 IMPLEMENTACIÓN DEL ORDENAMIENTO:
                    // Ordenamos la lista de reseñas de forma descendente (de mayor a menor)
                    // basándonos en el campo 'rating' del objeto Review (que es el segundo elemento del Pair).

                    // Ordenamiento por calificación (de 5 estrellas a 1)
                    reviewList.sortByDescending { it.second.rating }

                    // Opcional: Si quieres un ordenamiento más robusto (rating y luego fecha):
                    /*
                    reviewList.sortWith(compareByDescending<Pair<String, Review>> { it.second.rating }
                        .thenByDescending { it.second.timestamp }
                    )
                    */

                    // Después de ordenar, notificamos al adaptador
                    reviewAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DetailActivity", "Error al cargar reseñas: ${error.message}")
                }
            })
    }

    private fun showLeaveReviewDialog(contractorId: String) {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_leave_review, null)
        builder.setView(dialogView)

        val ratingBar = dialogView.findViewById<RatingBar>(R.id.rating_bar_review)
        val editTextComment = dialogView.findViewById<EditText>(R.id.edit_text_comment)

        builder.setTitle("Tu Opinión es Importante")
        builder.setPositiveButton("Publicar") { dialog, _ ->
            val rating = ratingBar.rating.toDouble()
            val comment = editTextComment.text.toString().trim()
            val currentUser = FirebaseAuth.getInstance().currentUser
            val clientId = currentUser?.uid
            val clientName = currentUser?.email ?: "Usuario sin correo"

            if (clientId != null && rating > 0) {
                val reviewData = Review(
                    clientId = clientId,
                    clientName = clientName,
                    rating = rating,
                    comment = comment,
                    timestamp = System.currentTimeMillis()
                )

                // 1. Guardamos la reseña en el perfil del contratista
                val dbRef = FirebaseDatabase.getInstance().getReference("Users")
                dbRef.child(contractorId).child("reviews").push().setValue(reviewData)
                    .addOnSuccessListener {

                        // 2. 🚨 LIMPIEZA: Eliminamos el registro de servicio completado
                        // para que no pueda calificar dos veces por el mismo escaneo.
                        FirebaseDatabase.getInstance().getReference("CompletedServices")
                            .child(contractorId)
                            .child(clientId)
                            .removeValue() // O puedes usar .child("status").setValue("reviewed")

                        Toast.makeText(this, "¡Gracias por tu opinión!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()

                        // Opcional: Refrescar la vista o cerrar la actividad
                        fetchReviews(contractorId)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al publicar la reseña.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Por favor, selecciona una calificación.", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar", null)
        builder.create().show()
    }
    private fun checkReviewPermission(contractorId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 1. Creamos una variable local para rastrear el estado
        var isConfirmed = false

        val dbRef = FirebaseDatabase.getInstance().getReference("CompletedServices")
            .child(contractorId)
            .child(currentUserId)

        // 2. Mantenemos el listener para actualizar la variable 'isConfirmed'
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Si el nodo existe, cambiamos el estado a true
                isConfirmed = snapshot.exists()

                // Opcional: Cambiamos la opacidad para dar una pista visual
                btnLeaveReview.alpha = if (isConfirmed) 1.0f else 0.7f
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DetailActivity", "Error al verificar permiso: ${error.message}")
            }
        })

        // 3. 🚨 CLAVE: El click listener va FUERA del addValueEventListener
        // Así, el botón responderá siempre, incluso si no hay internet o si el nodo no existe.
        btnLeaveReview.setOnClickListener {
            if (isConfirmed) {
                // ✅ CASO POSITIVO: Abrimos el diálogo para escribir la reseña
                showLeaveReviewDialog(contractorId)
            } else {
                // ❌ CASO NEGATIVO: Mostramos el mensaje de advertencia que pediste
                AlertDialog.Builder(this)
                    .setTitle("Evaluación Pendiente")
                    .setMessage("No puedes escribir una reseña todavía. Es necesario que el contratista escanee tu código QR al finalizar el trabajo para habilitar esta opción.")
                    .setPositiveButton("Entendido", null)
                    .show()
            }
        }
    }
}
