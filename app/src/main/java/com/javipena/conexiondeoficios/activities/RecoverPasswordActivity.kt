package com.javipena.conexiondeoficios.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.javipena.conexiondeoficios.R

class RecoverPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)

        val secretQuestion = findViewById<EditText>(R.id.edit_secret_question)
        val secretAnswer = findViewById<EditText>(R.id.edit_secret_answer)
        val btnRecover = findViewById<Button>(R.id.btn_recover)

        btnRecover.setOnClickListener {
            val questionInput = secretQuestion.text.toString()
            val answerInput = secretAnswer.text.toString()

            FirebaseDatabase.getInstance().getReference("Users").child("secret_question").get()
                .addOnSuccessListener { dataSnapshot ->
                    val storedAnswer = dataSnapshot.child("secret_answer").value.toString()

                    if (storedAnswer == answerInput) {
                        Toast.makeText(this, "Contraseña enviada a tu correo", Toast.LENGTH_SHORT).show()
                        // Aquí iría la lógica para enviar la contraseña por email
                    } else {
                        Toast.makeText(this, "Respuesta incorrecta", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al verificar la respuesta", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
