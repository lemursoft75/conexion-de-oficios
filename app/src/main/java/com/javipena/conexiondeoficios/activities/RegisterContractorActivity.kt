package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.javipena.conexiondeoficios.R

class RegisterContractorActivity : AppCompatActivity() {
    private lateinit var editName: EditText
    private lateinit var editLastname: EditText
    private lateinit var editPhone: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editCompanyName: EditText
    private lateinit var spinnerSpecialty: Spinner
    private lateinit var editRFC: EditText
    private lateinit var editLatitude: EditText
    private lateinit var editLongitude: EditText
    private lateinit var editSecretQuestion: EditText
    private lateinit var editSecretAnswer: EditText
    private lateinit var btnRegister: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_contractor)

        auth = FirebaseAuth.getInstance()
        editName = findViewById(R.id.edit_name)
        editLastname = findViewById(R.id.edit_lastname)
        editPhone = findViewById(R.id.edit_phone)
        editEmail = findViewById(R.id.edit_email)
        editPassword = findViewById(R.id.edit_password)
        editCompanyName = findViewById(R.id.edit_company)
        spinnerSpecialty = findViewById(R.id.spinner_specialty)
        editRFC = findViewById(R.id.edit_rfc)
        editLatitude = findViewById(R.id.edit_latitude)
        editLongitude = findViewById(R.id.edit_longitude)
        btnRegister = findViewById(R.id.btn_register)

        // 📌 Cargar especialidades en el Spinner
        val specialties = listOf("Selecciona una especialidad", "Albañil", "Electricista", "Plomero", "Carpintero", "Pintor", "Otro")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecialty.adapter = adapter

        btnRegister.setOnClickListener {
            registerContractor()
        }
    }

    private fun registerContractor() {
        val name = editName.text.toString()
        val lastname = editLastname.text.toString()
        val phone = editPhone.text.toString()
        val email = editEmail.text.toString()
        val password = editPassword.text.toString()
        val companyName = editCompanyName.text.toString()
        val specialty = spinnerSpecialty.selectedItem?.toString() ?: ""

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || specialty.isEmpty() || specialty == "Selecciona una especialidad") {
            Toast.makeText(this, "❌ Por favor, completa todos los campos y selecciona una especialidad", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val contractorData = hashMapOf(
                        "name" to name,
                        "lastname" to lastname,
                        "phone" to phone,
                        "email" to email,
                        "companyName" to companyName,
                        "specialty" to specialty,
                        "rfc" to editRFC.text.toString(),
                        "latitude" to editLatitude.text.toString(),
                        "longitude" to editLongitude.text.toString(),
                        "userType" to "contractor"
                    )

                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(userId!!)
                        .setValue(contractorData)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                Toast.makeText(this, "✅ Contratista registrado exitosamente", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this, PublicationActivity::class.java)) // 📌 Redirección
                                finish()
                            } else {
                                Toast.makeText(this, "❌ Error al guardar datos en Firebase", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "❌ Error en la autenticación: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

}
