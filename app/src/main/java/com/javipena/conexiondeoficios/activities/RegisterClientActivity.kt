package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.javipena.conexiondeoficios.R


class RegisterClientActivity : AppCompatActivity() {
    private lateinit var editName: EditText
    private lateinit var editPhone: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editConfirmPassword: EditText
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
        editConfirmPassword = findViewById(R.id.edit_confirm_password)
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
        val confirmPassword = editConfirmPassword.text.toString()

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "❌ Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // 📌 Validación de confirmación de contraseña
        if (password != confirmPassword) {
            Toast.makeText(this, "⚠ Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        // 📌 Crear usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    Log.d("RegisterClient", "✅ Usuario creado en Firebase Authentication")

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
                                Log.d("RegisterClient", "✅ Datos guardados en Firebase Database")
                                runOnUiThread {
                                    showSuccessDialog()
                                }
                            } else {
                                Log.e("RegisterClient", "❌ Error al guardar datos en Firebase Database: ${dbTask.exception?.message}")
                                Toast.makeText(this, "❌ Error al guardar datos en Firebase", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Log.e("RegisterClient", "❌ Error en autenticación: ${authTask.exception?.message}")
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

        Handler(mainLooper).postDelayed({
            dialog.dismiss()
            redirectToLogin()
        }, 3000) // Espera 3 segundos y redirige
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // 📌 Borra actividades anteriores
        startActivity(intent)
        finish()
    }
}