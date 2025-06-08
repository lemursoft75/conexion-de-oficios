package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.javipena.conexiondeoficios.R

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Vinculación de todos los elementos de la UI
        val username = findViewById<EditText>(R.id.edit_username)
        val password = findViewById<EditText>(R.id.edit_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val btnGuestLogin = findViewById<Button>(R.id.btn_guest_login)
        val textForgotPassword = findViewById<TextView>(R.id.btn_recover_password)

        // Listener para el botón de Iniciar Sesión
        btnLogin.setOnClickListener {
            val email = username.text.toString().trim()
            val pass = password.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                // Verificamos el tipo de usuario para redirigir a la pantalla correcta
                                FirebaseDatabase.getInstance().getReference("Users").child(userId).child("userType")
                                    .get().addOnSuccessListener { snapshot ->
                                        val userType = snapshot.value.toString()
                                        if (userType == "contractor") {
                                            startActivity(Intent(this, ContractorDashboardActivity::class.java))
                                        } else {
                                            startActivity(Intent(this, DirectoryActivity::class.java))
                                        }
                                        finish() // Cierra la actividad de Login
                                    }.addOnFailureListener {
                                        // Por si falla la lectura, se va al directorio por defecto
                                        startActivity(Intent(this, DirectoryActivity::class.java))
                                        finish()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "❌ Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "⚠ Por favor, ingresa todos los datos", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener para entrar como invitado
        btnGuestLogin.setOnClickListener {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    Toast.makeText(this, "✅ Sesión anónima iniciada.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DirectoryActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "❌ Error al iniciar sesión anónima", Toast.LENGTH_LONG).show()
                }
        }

        // Listener para el botón de registrarse
        btnRegister.setOnClickListener {
            val options = arrayOf("Registrar como Cliente", "Registrar como Contratista")
            AlertDialog.Builder(this)
                .setTitle("Selecciona tu tipo de cuenta")
                .setItems(options) { _, selectedIndex ->
                    when (selectedIndex) {
                        0 -> startActivity(Intent(this, RegisterClientActivity::class.java))
                        1 -> startActivity(Intent(this, RegisterContractorActivity::class.java))
                    }
                }
                .show()
        }

        // Listener para el texto de "¿Olvidaste tu contraseña?"
        textForgotPassword.setOnClickListener {
            startActivity(Intent(this, RecoverPasswordActivity::class.java))
        }
    }
}