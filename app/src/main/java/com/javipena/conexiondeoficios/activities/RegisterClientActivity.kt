package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.javipena.conexiondeoficios.R

class RegisterClientActivity : AppCompatActivity() {
    private lateinit var editName: EditText
    private lateinit var editPhone: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_client)

        auth = FirebaseAuth.getInstance()
        editName = findViewById(R.id.edit_name)
        editPhone = findViewById(R.id.edit_phone)
        editEmail = findViewById(R.id.edit_email)
        editPassword = findViewById(R.id.edit_password)
        btnRegister = findViewById(R.id.btn_register)

        btnRegister.setOnClickListener {
            registerClient()
        }
    }

    private fun registerClient() {
        val name = editName.text.toString()
        val phone = editPhone.text.toString()
        val email = editEmail.text.toString()
        val password = editPassword.text.toString()

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "❌ Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // 📌 Crear usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val clientData = hashMapOf(
                        "name" to name,
                        "phone" to phone,
                        "email" to email,
                        "userType" to "client"
                    )

                    // 📌 Guardar usuario en Firebase Database
                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(userId!!)
                        .setValue(clientData)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                Toast.makeText(this, "✅ Cliente registrado exitosamente", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this, DirectoryActivity::class.java)) // 📌 Redirección al directorio
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
