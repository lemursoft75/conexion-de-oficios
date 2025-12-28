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
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.javipena.conexiondeoficios.R

class RegisterContractorActivity : AppCompatActivity() {

    // --- Vistas ---
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
    private lateinit var btnDetectLocation: Button

    // ✔ NUEVOS CAMPOS (Que ya tenías)
    private lateinit var switchEmergencies: Switch
    private lateinit var editDays: EditText
    private lateinit var editHoursFrom: EditText
    private lateinit var editHoursTo: EditText

    // ✔ NUEVO: PRIVACIDAD
    private lateinit var checkboxPrivacy: CheckBox

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_contractor)

        auth = FirebaseAuth.getInstance()

        setupViews()
        setupSpecialtySpinner()
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

        // ✔ NUEVOS
        switchEmergencies = findViewById(R.id.switch_emergency)
        editDays = findViewById(R.id.edit_days)
        editHoursFrom = findViewById(R.id.edit_hours_from)
        editHoursTo = findViewById(R.id.edit_hours_to)

        // ✔ NUEVO: Inicialización de privacidad
        checkboxPrivacy = findViewById(R.id.checkbox_privacy)
    }

    private fun setupSpecialtySpinner() {
        val specialties = listOf(
            "Selecciona una especialidad", "Albañil", "Electricista", "Plomero", "Carpintero",
            "Cerrajero", "Mecánico", "Refrigeracion o Lavadoras", "PC o Celulares",
            "Herrero", "Limpieza de hogar", "Jardinero", "Agente inmobiliario", "Médico",
            "Asesoría escolar", "Músico", "Animación para eventos", "Otro"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecialty.adapter = adapter
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener { registerContractor() }
        btnDetectLocation.setOnClickListener { checkLocationPermission() }

        // Nueva línea para ver el aviso
        findViewById<TextView>(R.id.text_view_privacy).setOnClickListener {
            showPrivacyPolicyDialog()
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Necesitas permitir la ubicación.", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // Mantengo tu formato exacto de decimales: %.6f
                    editLatitude.setText("%.6f".format(location.latitude))
                    editLongitude.setText("%.6f".format(location.longitude))
                    Toast.makeText(this, "Ubicación detectada.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación.", Toast.LENGTH_LONG).show()
                }
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

        // ✔ NUEVOS CAMPOS
        val attendsEmergencies = switchEmergencies.isChecked
        val days = editDays.text.toString().trim()
        val hourFrom = editHoursFrom.text.toString().trim()
        val hourTo = editHoursTo.text.toString().trim()

        // --- VALIDACIONES ---
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || specialty == "Selecciona una especialidad") {
            Toast.makeText(this, "Completa todos los campos obligatorios.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
            return
        }

        // ✔ NUEVA VALIDACIÓN: PRIVACIDAD
        if (!checkboxPrivacy.isChecked) {
            Toast.makeText(this, "Debes aceptar el aviso de privacidad y términos para registrarte.", Toast.LENGTH_LONG).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { taskAuth ->
                if (taskAuth.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Guardar en Firebase (Mantengo tu estructura original y agrego los consentimientos)
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
                        "reviewCount" to 0,

                        // ✔ NUEVOS (Los que ya tenías)
                        "attendsEmergencies" to attendsEmergencies,
                        "attentionDays" to days,
                        "attentionFrom" to hourFrom,
                        "attentionTo" to hourTo,

                        // ✔ NUEVOS (Consentimiento legal solicitado)
                        "privacyAccepted" to true,
                        "diffusionConsent" to true,
                        "reviewsAccepted" to true,
                        "dataPolicy" to "delete_on_cancellation",
                        "consentTimestamp" to System.currentTimeMillis()
                    )

                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(userId)
                        .setValue(contractorData)
                        .addOnSuccessListener {
                            showSuccessDialog()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al guardar.", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Error: ${taskAuth.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showPrivacyPolicyDialog() {
        val privacyText = """
        AVISO DE PRIVACIDAD Y TÉRMINOS DE SERVICIO
        
        1. Difusión de Servicios: Al registrarse, usted autoriza a Conexión de Oficios a mostrar su nombre, especialidad, empresa y datos de contacto a los usuarios de la plataforma.
        
        2. Sistema de Reseñas: Usted acepta que los usuarios califiquen la calidad de su trabajo. Estas calificaciones son públicas para ayudar a otros usuarios.
        
        3. Uso de Datos: Sus datos personales (RFC, Teléfono, Ubicación) se usarán exclusivamente para la conexión de servicios de oficio.
        
        4. Cancelación: En cumplimiento con la ley de protección de datos, al cancelar su cuenta, toda su información personal y registros de servicios serán eliminados de nuestra base de datos activa de forma definitiva.
        
        5. Consentimiento: Al marcar la casilla de registro, usted otorga su consentimiento expreso para los fines antes mencionados.
    """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Aviso de Privacidad")
            .setMessage(privacyText)
            .setPositiveButton("Entendido") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showSuccessDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Registro Completo")
            .setMessage("Tu cuenta ha sido creada exitosamente.")
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
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }
}