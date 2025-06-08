package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.javipena.conexiondeoficios.R

class RecoverPasswordActivity : AppCompatActivity() {
    private lateinit var editEmail: EditText
    private lateinit var btnRecover: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)

        auth = FirebaseAuth.getInstance()
        editEmail = findViewById(R.id.edit_email)
        btnRecover = findViewById(R.id.btn_recover)

        btnRecover.setOnClickListener {
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        val emailInput = editEmail.text.toString().trim()

        if (emailInput.isEmpty()) {
            Toast.makeText(this, "⚠ Ingresa tu correo electrónico", Toast.LENGTH_SHORT).show()
            return // Detiene la ejecución si el campo está vacío
        }

        auth.sendPasswordResetEmail(emailInput)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // El correo se envió (o el usuario no existe, Firebase no lo revela)
                    showSuccessDialog()
                } else {
                    // Muestra el error específico que dio Firebase
                    val errorMessage = task.exception?.message ?: "Ocurrió un error desconocido."
                    Toast.makeText(this, "❌ Error: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showSuccessDialog() {
        // Asegurándonos de que el diálogo se crea y muestra solo si la actividad está activa
        if (!isFinishing && !isDestroyed) {
            AlertDialog.Builder(this)
                .setTitle("📩 Revisar Correo")
                .setMessage("Si tu correo está registrado con nosotros, recibirás un enlace para restablecer tu contraseña en unos momentos.")
                .setPositiveButton("Aceptar") { dialog, _ ->
                    dialog.dismiss()
                    // Volvemos a la pantalla de Login
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }
}