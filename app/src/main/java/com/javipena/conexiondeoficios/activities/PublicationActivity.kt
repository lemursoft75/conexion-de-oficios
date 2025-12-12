package com.javipena.conexiondeoficios.activities

import android.Manifest
import android.app.Activity // Se mantiene si usas Activity.RESULT_OK en otro lugar, pero no en este código refactorizado
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.VideoView
// 📌 NUEVAS IMPORTACIONES
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.javipena.conexiondeoficios.Ad
import com.javipena.conexiondeoficios.R

class PublicationActivity : AppCompatActivity() {

    // --- Variables de la UI ---
    private lateinit var editAdText: EditText
    private lateinit var btnPublish: Button
    private lateinit var btnUploadMedia: Button
    private lateinit var btnBackToMenu: Button
    private lateinit var imagePreview: ImageView
    private lateinit var videoPreview: VideoView
    private lateinit var progressBar: ProgressBar

    // --- Variables de Firebase y de estado ---
    private lateinit var auth: FirebaseAuth
    private var mediaUri: Uri? = null

    // 📌 NUEVO: LECTOR DE RESULTADOS (ACTIVITY RESULT API)
    private val mediaPickerLauncher = registerForActivityResult(
        // Utilizamos el contrato para obtener cualquier tipo de contenido
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // ESTE BLOQUE REEMPLAZA TODA LA LÓGICA DE onActivityResult
        if (uri != null) {
            mediaUri = uri
            val mimeType = contentResolver.getType(mediaUri!!)

            if (mimeType?.startsWith("video") == true) {
                imagePreview.visibility = View.GONE
                videoPreview.visibility = View.VISIBLE
                videoPreview.setVideoURI(mediaUri)
                videoPreview.setOnPreparedListener { it.isLooping = true }
                videoPreview.start()
            } else {
                videoPreview.visibility = View.GONE
                imagePreview.visibility = View.VISIBLE
                imagePreview.setImageURI(mediaUri)
            }
        }
    }


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
        videoPreview = findViewById(R.id.video_preview)
        progressBar = findViewById(R.id.progress_bar_publication)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnUploadMedia.setOnClickListener { checkPermissionsAndOpenFileChooser() }
        btnPublish.setOnClickListener { publishAd() }
        btnBackToMenu.setOnClickListener { returnToMenu() }
    }

    // --- LÓGICA DE PERMISOS Y SELECCIÓN DE ARCHIVOS ---

    private fun checkPermissionsAndOpenFileChooser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionsToRequest = mutableListOf<String>()
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            }

            if (permissionsToRequest.isNotEmpty()) {
                requestPermissions(permissionsToRequest.toTypedArray(), REQUEST_PERMISSIONS_CODE)
            } else {
                // 📌 CAMBIO CLAVE: Usa el nuevo lanzador
                launchMediaPicker()
            }
        } else {
            // 📌 CAMBIO CLAVE: Usa el nuevo lanzador
            launchMediaPicker()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Si se conceden los permisos, llama al lanzador
                launchMediaPicker()
            } else {
                Toast.makeText(this, "Se necesitan permisos para acceder a la galería.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 📌 NUEVO MÉTODO DE LANZAMIENTO
    private fun launchMediaPicker() {
        // El input para GetContent es el MIME type o un conjunto de MIME types separados por comas.
        // Aquí pedimos imágenes y videos.
        mediaPickerLauncher.launch("image/*, video/*")
    }


    // 🚨 MÉTODOS OBSOLETOS ELIMINADOS:
    // ELIMINAR launchFileChooserIntent() y onActivityResult()
    // Esto resuelve las advertencias en las líneas 108 y 111.
    // --- LÓGICA DE PUBLICACIÓN ---

    private fun publishAd() {
        val adText = editAdText.text.toString().trim()
        if (adText.isEmpty() && mediaUri == null) {
            Toast.makeText(this, "El anuncio debe tener texto o un archivo multimedia.", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        val userId = auth.currentUser?.uid ?: run {
            setLoading(false)
            return
        }

        FirebaseDatabase.getInstance().getReference("Users").child(userId).get()
            .addOnSuccessListener { userSnapshot ->
                val phone = userSnapshot.child("phone").value.toString()
                val latitude = userSnapshot.child("latitude").value.toString()
                val longitude = userSnapshot.child("longitude").value.toString()
                val specialty = userSnapshot.child("specialty").value.toString()
                val adId = FirebaseDatabase.getInstance().getReference("Ads").push().key ?: ""

                if (mediaUri != null) {
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

    private fun uploadMediaToCloudinaryAndSaveAd(
        adId: String,
        userId: String,
        adText: String,
        phone: String,
        latitude: String,
        longitude: String,
        specialty: String
    ) {
        mediaUri?.let { uri ->
            // Determina el tipo de recurso (imagen o video)
            val resourceType = if (contentResolver.getType(uri)?.startsWith("video") == true) "video" else "image"

            // 🚨 CORRECCIÓN CLAVE: Se añade el "upload_preset" requerido por Cloudinary
            // para subidas sin firmar (unsigned uploads).
            MediaManager.get().upload(uri)
                .option("upload_preset", "unsigned_ads") // <--- ¡Esta línea es la corrección!
                .option("public_id", adId)
                .option("folder", "ads_media")
                .option("resource_type", resourceType)
                .option("chunk_size", 6_000_000) // Para archivos grandes
                .callback(object : UploadCallback {
                    override fun onSuccess(requestId: String, resultData: Map<*, *>?) {
                        // Obtiene la URL segura del resultado de Cloudinary
                        val secureUrl = resultData?.get("secure_url").toString()
                        // Guarda los datos del anuncio con la URL del medio
                        saveAdData(adId, userId, adText, phone, latitude, longitude, specialty, secureUrl)
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        // Muestra el error de Cloudinary y detiene la carga
                        Log.e("PublicationActivity", "Error al subir a Cloudinary: ${error.description}")
                        Toast.makeText(baseContext, "Error al subir el archivo.", Toast.LENGTH_SHORT).show()
                        setLoading(false)
                    }

                    // Métodos obligatorios de la interfaz UploadCallback (no necesitan lógica aquí)
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                })
                .dispatch() // Inicia el proceso de subida
        }
    }

    private fun saveAdData(
        adId: String,
        userId: String,
        adText: String,
        phone: String,
        latitude: String,
        longitude: String,
        specialty: String,
        mediaUrl: String?
    ) {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")

        // 1. 🚨 PRIMERO, OBTENER LOS DATOS DE CALIFICACIÓN DEL CONTRATISTA
        usersRef.child(userId).get()
            .addOnSuccessListener { userSnapshot ->

                // 2. Extraer la calificación y el conteo (asegurando valores por defecto si son null)
                // Se asume que estos campos existen en el perfil del usuario (User/Contractor)
                val avgRating = userSnapshot.child("averageRating").getValue(Double::class.java) ?: 0.0
                val reviewCt = userSnapshot.child("reviewCount").getValue(Int::class.java) ?: 0

                // 3. Crear el objeto Ad con los datos de calificación
                val adData = Ad(
                    contractorId = userId,
                    adText = adText,
                    phone = phone,
                    latitude = latitude,
                    longitude = longitude,
                    specialty = specialty,
                    mediaUrl = mediaUrl,

                    // 🚨 INCLUIR LOS DATOS DE ORDENAMIENTO
                    averageRating = avgRating,
                    reviewCount = reviewCt
                )

                // 4. Guardar el objeto Ad completo en el nodo "Ads"
                val adsRef = FirebaseDatabase.getInstance().getReference("Ads")
                adsRef.child(adId).setValue(adData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "✅ Anuncio publicado correctamente", Toast.LENGTH_LONG).show()
                        returnToMenu()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "❌ Error al guardar el anuncio.", Toast.LENGTH_SHORT).show()
                        setLoading(false)
                    }

            }
            .addOnFailureListener {
                // Manejo de error si no se pudo leer el perfil del usuario
                Toast.makeText(this, "❌ Error: No se pudo verificar la calificación del contratista.", Toast.LENGTH_SHORT).show()
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
        // 🚨 ELIMINAMOS ESTA CONSTANTE (ya no se usa)
        // private const val REQUEST_MEDIA_PICK = 1001
        private const val REQUEST_PERMISSIONS_CODE = 2001
    }
}