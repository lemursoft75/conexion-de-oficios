package com.javipena.conexiondeoficios.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.javipena.conexiondeoficios.Ad // 📌 CAMBIO 1: Asegúrate de importar tu data class Ad
import com.javipena.conexiondeoficios.R

class PublicationActivity : AppCompatActivity() {
    private lateinit var editAdText: EditText
    private lateinit var btnPublish: Button
    private lateinit var btnUploadMedia: Button
    private lateinit var btnBackToMenu: Button
    private lateinit var imagePreview: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth
    private var mediaUri: Uri? = null

    // Debes tener esta clase en tu proyecto, por ejemplo en un archivo Models.kt
    // @Parcelize
    // data class Ad(val contractorId: String = "", ..., val mediaUrl: String = "") : Parcelable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publication)

        auth = FirebaseAuth.getInstance()
        editAdText = findViewById(R.id.edit_ad_text)
        btnPublish = findViewById(R.id.btn_publish)
        btnUploadMedia = findViewById(R.id.btn_upload_media)
        btnBackToMenu = findViewById(R.id.btn_back_to_menu)
        imagePreview = findViewById(R.id.image_preview)
        progressBar = findViewById(R.id.progress_bar_publication) // Asegúrate de tener un ProgressBar en tu XML

        btnUploadMedia.setOnClickListener { openFileChooser() }
        btnPublish.setOnClickListener { publishAd() }
        btnBackToMenu.setOnClickListener { returnToMenu() }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        startActivityForResult(intent, REQUEST_MEDIA_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MEDIA_PICK && resultCode == Activity.RESULT_OK && data?.data != null) {
            mediaUri = data.data
            imagePreview.setImageURI(mediaUri)
        }
    }

    private fun publishAd() {
        val adText = editAdText.text.toString().trim()
        if (adText.isEmpty()) {
            Toast.makeText(this, "El texto del anuncio no puede estar vacío.", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        val userId = auth.currentUser?.uid ?: return

        // 1. Obtener los datos del perfil del contratista
        FirebaseDatabase.getInstance().getReference("Users").child(userId).get()
            .addOnSuccessListener { userSnapshot ->
                val phone = userSnapshot.child("phone").value.toString()
                val latitude = userSnapshot.child("latitude").value.toString()
                val longitude = userSnapshot.child("longitude").value.toString()
                val specialty = userSnapshot.child("specialty").value.toString()

                // 2. 📌 CAMBIO 2: Generar un ID único para el anuncio ANTES de subir la imagen.
                val adId = FirebaseDatabase.getInstance().getReference("Ads").push().key ?: ""

                // 3. Proceder a guardar
                if (mediaUri != null) {
                    uploadMediaAndSaveAd(adId, userId, adText, phone, latitude, longitude, specialty)
                } else {
                    saveAdData(adId, userId, adText, phone, latitude, longitude, specialty, null)
                }
            }
            .addOnFailureListener {
                Log.e("PublicationActivity", "Error al obtener datos del usuario: ${it.message}")
                Toast.makeText(this, "Error al obtener datos del usuario.", Toast.LENGTH_SHORT).show()
                setLoading(false)
            }
    }

    private fun uploadMediaAndSaveAd(adId: String, userId: String, adText: String, phone: String, latitude: String, longitude: String, specialty: String) {
        // 📌 CAMBIO 3: Usar el ID único del anuncio para el nombre del archivo.
        // Esto evita que una imagen nueva reemplace a una antigua.
        val storageRef = FirebaseStorage.getInstance().reference.child("ads_media/$adId")

        mediaUri?.let {
            storageRef.putFile(it).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveAdData(adId, userId, adText, phone, latitude, longitude, specialty, uri.toString())
                }
            }.addOnFailureListener {
                Log.e("PublicationActivity", "Error al subir imagen: ${it.message}")
                Toast.makeText(this, "Error al subir la imagen.", Toast.LENGTH_SHORT).show()
                setLoading(false)
            }
        }
    }

    private fun saveAdData(adId: String, userId: String, adText: String, phone: String, latitude: String, longitude: String, specialty: String, mediaUrl: String?) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Ads")

        // 📌 CAMBIO 4: Usar la data class 'Ad' para crear el objeto. Es más limpio y seguro.
        val adData = Ad(
            contractorId = userId,
            adText = adText,
            phone = phone,
            latitude = latitude,
            longitude = longitude,
            specialty = specialty,
            mediaUrl = mediaUrl ?: ""
        )

        databaseRef.child(adId).setValue(adData).addOnSuccessListener {
            Log.d("PublicationActivity", "Anuncio publicado con éxito.")
            Toast.makeText(this, "✅ Anuncio publicado correctamente", Toast.LENGTH_LONG).show()
            returnToMenu()
        }.addOnFailureListener {
            Log.e("PublicationActivity", "Error al guardar anuncio: ${it.message}")
            Toast.makeText(this, "Error al guardar el anuncio.", Toast.LENGTH_SHORT).show()
            setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnPublish.isEnabled = !isLoading
        btnUploadMedia.isEnabled = !isLoading
    }

    // Dentro de PublicationActivity.kt

    private fun returnToMenu() {
        // 📌 CAMBIO: Ahora vuelve al panel de control del contratista, no a una MainActivity genérica.
        val intent = Intent(this, ContractorDashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        private const val REQUEST_MEDIA_PICK = 1001
    }
}