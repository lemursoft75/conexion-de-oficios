package com.javipena.conexiondeoficios.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.javipena.conexiondeoficios.Ad
import com.javipena.conexiondeoficios.R
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

class EditAdActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var btnSave: Button
    private lateinit var btnChangeImage: Button
    private lateinit var imagePreview: ImageView

    private var selectedImageUri: Uri? = null

    private val db = FirebaseDatabase.getInstance()

    private lateinit var adId: String
    private lateinit var ad: Ad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_ad)

        editText = findViewById(R.id.edit_text_ad)
        btnSave = findViewById(R.id.btn_save_ad)
        btnChangeImage = findViewById(R.id.btn_change_image)
        imagePreview = findViewById(R.id.image_preview)

        adId = intent.getStringExtra("AD_ID") ?: ""
        ad = intent.getParcelableExtra("AD_OBJECT") ?: run {
            Toast.makeText(this, "Error al cargar anuncio", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        editText.setText(ad.adText)

        // Evita cargar imagen vieja
        if (!ad.mediaUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(ad.mediaUrl + "?t=" + System.currentTimeMillis())
                .into(imagePreview)
        }

        btnChangeImage.setOnClickListener { pickImageFromGallery() }
        btnSave.setOnClickListener { saveChanges() }
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 2001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2001 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            imagePreview.setImageURI(selectedImageUri)
        }
    }

    private fun saveChanges() {
        val newText = editText.text.toString().trim()

        if (newText.isEmpty()) {
            Toast.makeText(this, "El anuncio no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            updateTextOnly(newText)
        } else {
            uploadNewImage(newText)
        }
    }

    private fun updateTextOnly(newText: String) {
        db.getReference("Ads").child(adId).child("adText")
            .setValue(newText)
            .addOnSuccessListener {
                Toast.makeText(this, "Anuncio actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadNewImage(newText: String) {

        selectedImageUri?.let { uri ->

            MediaManager.get().upload(uri)
                .option("upload_preset", "unsigned_ads")
                .option("resource_type", "image")
                .option("folder", "ads_media")   // ← NECESARIO PARA PRESET UNSIGNED
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                        val secureUrl = resultData["secure_url"] as String
                        updateAdFull(newText, secureUrl)
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Toast.makeText(
                            this@EditAdActivity,
                            "Error subiendo imagen a Cloudinary: ${error?.description}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                })
                .dispatch()


        }
    }


    private fun updateAdFull(newText: String, newImageUrl: String) {
        val updates = mapOf(
            "adText" to newText,
            "mediaUrl" to newImageUrl
        )

        db.getReference("Ads").child(adId)
            .updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Anuncio actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }
}
