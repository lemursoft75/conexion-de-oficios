package com.javipena.conexiondeoficios.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.javipena.conexiondeoficios.R


class PublicationActivity : AppCompatActivity() {
    private lateinit var editAdText: EditText
    private lateinit var btnPublish: Button
    private lateinit var btnUploadMedia: Button
    private lateinit var imagePreview: ImageView
    private lateinit var auth: FirebaseAuth
    private var mediaUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publication)

        auth = FirebaseAuth.getInstance()
        editAdText = findViewById(R.id.edit_ad_text)
        btnPublish = findViewById(R.id.btn_publish)
        btnUploadMedia = findViewById(R.id.btn_upload_media)
        imagePreview = findViewById(R.id.image_preview)

        btnUploadMedia.setOnClickListener {
            openFileChooser() // 📌 Permite seleccionar imagen o video
        }

        btnPublish.setOnClickListener {
            publishAd()
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/* video/*"
        startActivityForResult(intent, REQUEST_MEDIA_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MEDIA_PICK && resultCode == Activity.RESULT_OK) {
            mediaUri = data?.data
            imagePreview.setImageURI(mediaUri) // 📌 Vista previa si es imagen
        }
    }

    private fun publishAd() {
        val adText = editAdText.text.toString()
        val userId = auth.currentUser?.uid ?: return

        FirebaseDatabase.getInstance().getReference("Users").child(userId).get()
            .addOnSuccessListener { userSnapshot ->
                val phone = userSnapshot.child("phone").value.toString()
                val latitude = userSnapshot.child("latitude").value.toString()
                val longitude = userSnapshot.child("longitude").value.toString()

                if (mediaUri != null) {
                    uploadMediaAndSaveAd(userId, adText, phone, latitude, longitude)
                } else {
                    saveAdData(userId, adText, phone, latitude, longitude, null)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadMediaAndSaveAd(userId: String, adText: String, phone: String, latitude: String, longitude: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("ads/$userId")
        val uploadTask = storageRef.putFile(mediaUri!!)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { mediaUrl ->
                saveAdData(userId, adText, phone, latitude, longitude, mediaUrl.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "❌ Error al subir imagen/video", Toast.LENGTH_SHORT).show()
        }
    }



    private fun saveAdData(userId: String, adText: String, phone: String, latitude: String, longitude: String, mediaUrl: String?) {
        val adData = hashMapOf(
            "contractorId" to userId,
            "adText" to adText,
            "phone" to phone,
            "latitude" to latitude,
            "longitude" to longitude,
            "mediaUrl" to (mediaUrl ?: "")
        )

        FirebaseDatabase.getInstance().getReference("Ads")
            .child(userId)
            .setValue(adData)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Anuncio publicado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Error al publicar anuncio", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        const val REQUEST_MEDIA_PICK = 1001
    }
}
