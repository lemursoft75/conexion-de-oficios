package com.javipena.conexiondeoficios.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
// 📌 NOTA: Ya no necesitamos importar FirebaseStorage
import com.javipena.conexiondeoficios.Ad
import com.javipena.conexiondeoficios.R

class PublicationActivity : AppCompatActivity() {

    // --- Variables de la UI ---
    private lateinit var editAdText: EditText
    private lateinit var btnPublish: Button
    private lateinit var btnUploadMedia: Button
    private lateinit var btnBackToMenu: Button
    private lateinit var imagePreview: ImageView
    private lateinit var progressBar: ProgressBar

    // --- Variables de Firebase y de estado ---
    private lateinit var auth: FirebaseAuth
    private var mediaUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publication)
        title = "Crear Anuncio"

        auth = FirebaseAuth.getInstance()
        editAdText = findViewById(R.id.edit_ad_text)
        btnPublish = findViewById(R.id.btn_publish)
        btnUploadMedia = findViewById(R.id.btn_upload_media)
        btnBackToMenu = findViewById(R.id.btn_back_to_menu)
        imagePreview = findViewById(R.id.image_preview)
        progressBar = findViewById(R.id.progress_bar_publication)

        setupClickListeners()
    }

    private fun setupClickListeners() {
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
            imagePreview.visibility = View.VISIBLE
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

        FirebaseDatabase.getInstance().getReference("Users").child(userId).get()
            .addOnSuccessListener { userSnapshot ->
                val phone = userSnapshot.child("phone").value.toString()
                val latitude = userSnapshot.child("latitude").value.toString()
                val longitude = userSnapshot.child("longitude").value.toString()
                val specialty = userSnapshot.child("specialty").value.toString()
                val adId = FirebaseDatabase.getInstance().getReference("Ads").push().key ?: ""

                if (mediaUri != null) {
                    // 📌 CAMBIO: Llamamos a la nueva función de Cloudinary
                    uploadMediaToCloudinaryAndSaveAd(adId, userId, adText, phone, latitude, longitude, specialty)
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

    /**
     * 📌 FUNCIÓN REEMPLAZADA: Sube la imagen a Cloudinary y, si tiene éxito, llama a guardar los datos.
     */
    private fun uploadMediaToCloudinaryAndSaveAd(adId: String, userId: String, adText: String, phone: String, latitude: String, longitude: String, specialty: String) {
        mediaUri?.let { uri ->
            MediaManager.get().upload(uri)
                .option("public_id", adId)
                .option("folder", "ads_media")
                .callback(object : UploadCallback {
                    override fun onSuccess(requestId: String, resultData: Map<*, *>?) {
                        val secureUrl = resultData?.get("secure_url").toString()
                        Log.d("PublicationActivity", "Imagen subida a Cloudinary: $secureUrl")
                        saveAdData(adId, userId, adText, phone, latitude, longitude, specialty, secureUrl)
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("PublicationActivity", "Error al subir a Cloudinary: ${error.description}")
                        Toast.makeText(baseContext, "Error al subir la imagen.", Toast.LENGTH_SHORT).show()
                        setLoading(false)
                    }

                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                })
                .dispatch()
        }
    }

    /**
     * Guarda el objeto 'Ad' completo en la Realtime Database. Esta función no cambia.
     */
    private fun saveAdData(adId: String, userId: String, adText: String, phone: String, latitude: String, longitude: String, specialty: String, mediaUrl: String?) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Ads")
        val adData = Ad(
            contractorId = userId,
            adText = adText,
            phone = phone,
            latitude = latitude,
            longitude = longitude,
            specialty = specialty,
            mediaUrl = mediaUrl
        )

        databaseRef.child(adId).setValue(adData)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Anuncio publicado correctamente", Toast.LENGTH_LONG).show()
                returnToMenu()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar el anuncio.", Toast.LENGTH_SHORT).show()
                setLoading(false)
            }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnPublish.isEnabled = !isLoading
        btnUploadMedia.isEnabled = !isLoading
        btnBackToMenu.isEnabled = !isLoading
    }



    private fun returnToMenu() {
        val intent = Intent(this, ContractorDashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        private const val REQUEST_MEDIA_PICK = 1001
    }
}