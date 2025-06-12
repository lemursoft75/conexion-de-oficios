package com.javipena.conexiondeoficios.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.javipena.conexiondeoficios.R

class RegisterContractorActivity : AppCompatActivity() {

    // --- Vistas de la UI ---
    private lateinit var editName: EditText
    private lateinit var editLastname: EditText
    private lateinit var editPhone: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editConfirmPassword: EditText
    private lateinit var editCompanyName: EditText
    private lateinit var spinnerSpecialty: Spinner
    private lateinit var editRFC: EditText
    private lateinit var editLatitude: EditText
    private lateinit var editLongitude: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnDetectLocation: Button // Botón nuevo

    // --- Firebase ---
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_contractor)

        auth = FirebaseAuth.getInstance()

        // Vinculación de todas las vistas del layout
        setupViews()

        // Configuración del Spinner de especialidades
        setupSpecialtySpinner()

        // Configuración de los listeners para los botones
        setupClickListeners()
    }

    private fun setupViews() {
        editName = findViewById(R.id.edit_name)
        editLastname = findViewById(R.id.edit_lastname)
        editPhone = findViewById(R.id.edit_phone)
        editEmail = findViewById(R.id.edit_email)
        editPassword = findViewById(R.id.edit_password)
        editConfirmPassword = findViewById(R.id.edit_confirm_password)
        editCompanyName = findViewById(R.id.edit_company)
        spinnerSpecialty = findViewById(R.id.spinner_specialty)
        editRFC = findViewById(R.id.edit_rfc)
        editLatitude = findViewById(R.id.edit_latitude)
        editLongitude = findViewById(R.id.edit_longitude)
        btnRegister = findViewById(R.id.btn_register)
        btnDetectLocation = findViewById(R.id.btn_detect_location)
    }

    private fun setupSpecialtySpinner() {
        val specialties = listOf("Selecciona una especialidad", "Albañil", "Electricista", "Plomero", "Carpintero", "Pintor", "Mecánico", "Técnico en refrigeración", "Técnico en computadoras", "Herrero", "Limpieza de hogar", "Jardinero", "Agente inmobiliario", "Médico", "Asesoría escolar", "Músico", "Animación para eventos", "Otro")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecialty.adapter = adapter
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            registerContractor()
        }
        btnDetectLocation.setOnClickListener {
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "El permiso de ubicación es necesario para detectar tu posición.", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        Toast.makeText(this, "Detectando ubicación...", Toast.LENGTH_SHORT).show()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    editLatitude.setText(String.format("%.6f", location.latitude))
                    editLongitude.setText(String.format("%.6f", location.longitude))
                    Toast.makeText(this, "Ubicación detectada.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación. Asegúrate de tener el GPS activado.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener la ubicación.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun registerContractor() {
        val name = editName.text.toString().trim()
        val lastname = editLastname.text.toString().trim()
        val phone = editPhone.text.toString().trim()
        val email = editEmail.text.toString().trim()
        val password = editPassword.text.toString()
        val confirmPassword = editConfirmPassword.text.toString()
        val companyName = editCompanyName.text.toString().trim()
        val specialty = spinnerSpecialty.selectedItem.toString()
        val rfc = editRFC.text.toString().trim()
        val latitudeStr = editLatitude.text.toString().trim()
        val longitudeStr = editLongitude.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || specialty == "Selecciona una especialidad" || latitudeStr.isEmpty() || longitudeStr.isEmpty()) {
            Toast.makeText(this, "❌ Por favor, completa todos los campos obligatorios.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "⚠ Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    Log.d("Register", "✅ Usuario creado en Firebase Authentication")
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val contractorData = mapOf(
                        "name" to name,
                        "lastname" to lastname,
                        "phone" to phone,
                        "email" to email,
                        "companyName" to companyName,
                        "specialty" to specialty,
                        "rfc" to rfc,
                        "latitude" to latitudeStr,
                        "longitude" to longitudeStr,
                        "userType" to "contractor",
                        "averageRating" to 0.0,
                        "reviewCount" to 0
                    )

                    FirebaseDatabase.getInstance().getReference("Users").child(userId)
                        .setValue(contractorData)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                Log.d("Register", "✅ Datos guardados en Firebase Database")
                                runOnUiThread { showSuccessDialog() }
                            } else {
                                Log.e("Register", "❌ Error al guardar datos: ${dbTask.exception?.message}")
                                Toast.makeText(this, "❌ Error al guardar datos.", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Log.e("Register", "❌ Error en autenticación: ${authTask.exception?.message}")
                    Toast.makeText(this, "❌ Error en la autenticación: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showSuccessDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("✅ Registro Completo")
            .setMessage("Tu cuenta ha sido creada exitosamente. Serás redirigido en unos segundos...")
            .setCancelable(false)
            .create()
        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            redirectToLogin()
        }, 3000)
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }
}