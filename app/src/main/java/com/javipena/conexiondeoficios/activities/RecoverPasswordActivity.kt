package com.javipena.conexiondeoficios.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
            val emailInput = editEmail.text.toString()

            if (emailInput.isEmpty()) {
                Toast.makeText(this, "⚠ Ingresa tu correo para recuperar la contraseña", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(emailInput)
                    .addOnSuccessListener {
                        showSuccessDialog() // 📌 Mensaje de éxito más claro
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "❌ Error al enviar correo: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun showSuccessDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("📩 Recuperación de Contraseña")
        builder.setMessage("Hemos enviado un email con instrucciones para restablecer tu contraseña.")
        builder.setPositiveButton("Aceptar") { _, _ -> finish() }
        builder.setCancelable(false)
        builder.show()
    }
}
