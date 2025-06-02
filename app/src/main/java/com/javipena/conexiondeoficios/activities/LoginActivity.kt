package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.javipena.conexiondeoficios.R

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance() // 📌 Inicializa Firebase Auth

        val username = findViewById<EditText>(R.id.edit_username)
        val password = findViewById<EditText>(R.id.edit_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val btnGuestLogin = findViewById<Button>(R.id.btn_guest_login) // 📌 Botón para sesión anónima

        // 📌 Inicio de sesión con correo y contraseña
        btnLogin.setOnClickListener {
            val email = username.text.toString()
            val pass = password.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "✅ Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, DirectoryActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "❌ Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "⚠ Por favor, ingresa todos los datos", Toast.LENGTH_SHORT).show()
            }
        }

        // 📌 Inicio de sesión anónimo al tocar el botón
        btnGuestLogin.setOnClickListener {
            auth.signInAnonymously()
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid // 📌 UID asignado por Firebase
                    Toast.makeText(this, "✅ Sesión anónima iniciada: $userId", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, DirectoryActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "❌ Error al iniciar sesión anónima", Toast.LENGTH_LONG).show()
                }
        }

        // 📌 Ir a la pantalla de registro
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
