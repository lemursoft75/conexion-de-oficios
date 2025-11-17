package com.javipena.conexiondeoficios.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.models.Contractor

class EditProfileActivity : AppCompatActivity() {

    // Vistas de la UI
    private lateinit var editName: EditText
    private lateinit var editLastname: EditText
    private lateinit var editPhone: EditText
    private lateinit var editEmail: EditText // Campo añadido para el correo
    private lateinit var editCompanyName: EditText
    private lateinit var spinnerSpecialty: Spinner
    private lateinit var editLatitude: EditText
    private lateinit var editLongitude: EditText
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: FirebaseDatabase

    private val specialtiesList = listOf("Selecciona una especialidad", "Albañil", "Electricista", "Plomero", "Carpintero", "Cerrajero", "Mecánico", "Técnico en refrigeración", "Técnico en computadoras", "Herrero", "Limpieza de hogar", "Jardinero", "Agente inmobiliario", "Médico", "Asesoría escolar", "Músico", "Animación para eventos", "Otro")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        title = "Editar Perfil"

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance()

        // Vincular vistas
        editName = findViewById(R.id.edit_profile_name)
        editLastname = findViewById(R.id.edit_profile_lastname)
        editPhone = findViewById(R.id.edit_profile_phone)
        editEmail = findViewById(R.id.edit_profile_email) // Vista del nuevo campo de correo
        editCompanyName = findViewById(R.id.edit_profile_company)
        spinnerSpecialty = findViewById(R.id.spinner_profile_specialty)
        editLatitude = findViewById(R.id.edit_profile_latitude)
        editLongitude = findViewById(R.id.edit_profile_longitude)
        btnSave = findViewById(R.id.btn_save_profile)
        progressBar = findViewById(R.id.progress_bar_edit)

        // Configurar el Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialtiesList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecialty.adapter = adapter

        // Cargar los datos del usuario
        loadUserProfile()

        // Listener para el botón de guardar
        btnSave.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun loadUserProfile() {
        progressBar.visibility = View.VISIBLE
        val user = auth.currentUser ?: return

        // Cargar el correo desde Firebase Authentication
        editEmail.setText(user.email)

        dbRef.getReference("Users").child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contractor = snapshot.getValue(Contractor::class.java)
                if (contractor != null) {
                    editName.setText(contractor.name)
                    editLastname.setText(contractor.lastname)
                    editPhone.setText(contractor.phone)
                    editCompanyName.setText(contractor.companyName)
                    editLatitude.setText(contractor.latitude)
                    editLongitude.setText(contractor.longitude)

                    val specialtyPosition = specialtiesList.indexOf(contractor.specialty)
                    if (specialtyPosition >= 0) {
                        spinnerSpecialty.setSelection(specialtyPosition)
                    }
                }
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@EditProfileActivity, "Error al cargar el perfil.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserProfile() {
        progressBar.visibility = View.VISIBLE
        val user = auth.currentUser ?: return
        val newEmail = editEmail.text.toString().trim()

        // 1. Verificar si el correo ha cambiado
        if (newEmail.isNotEmpty() && newEmail != user.email) {
            // Si el correo cambió, primero actualizamos Firebase Authentication
            user.updateEmail(newEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("EditProfile", "Correo actualizado en Firebase Authentication.")
                        // Si tuvo éxito, ahora actualizamos todos los datos en la Realtime Database
                        updateUserDataInDatabase(newEmail)
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Error al actualizar el correo: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            // Si el correo no cambió, solo actualizamos los datos en la Realtime Database
            updateUserDataInDatabase(user.email!!) // Pasamos el correo antiguo
        }
    }

    private fun updateUserDataInDatabase(email: String) {
        val userId = auth.currentUser?.uid ?: return

        val updatesMap = mapOf<String, Any>(
            "name" to editName.text.toString().trim(),
            "lastname" to editLastname.text.toString().trim(),
            "phone" to editPhone.text.toString().trim(),
            "email" to email, // Se guarda el correo (nuevo o el antiguo)
            "companyName" to editCompanyName.text.toString().trim(),
            "specialty" to spinnerSpecialty.selectedItem.toString(),
            "latitude" to editLatitude.text.toString().trim(),
            "longitude" to editLongitude.text.toString().trim()
        )

        dbRef.getReference("Users").child(userId).updateChildren(updatesMap)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "✅ Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                finish() // Cierra la pantalla de edición y vuelve al dashboard
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "❌ Error al actualizar los datos del perfil", Toast.LENGTH_SHORT).show()
            }
    }
}