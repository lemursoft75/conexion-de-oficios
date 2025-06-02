package com.javipena.conexiondeoficios.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.javipena.conexiondeoficios.R

class RegisterClientActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_client)

        val name = findViewById<EditText>(R.id.edit_name)
        val phone = findViewById<EditText>(R.id.edit_phone)
        val email = findViewById<EditText>(R.id.edit_email)
        val password = findViewById<EditText>(R.id.edit_password)
        val btnRegister = findViewById<Button>(R.id.btn_register)

        btnRegister.setOnClickListener {
            val clientData = hashMapOf(
                "name" to name.text.toString(),
                "phone" to phone.text.toString(),
                "email" to email.text.toString(),
                "password" to password.text.toString()
            )

            FirebaseDatabase.getInstance().getReference("Clients")
                .push()
                .setValue(clientData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
