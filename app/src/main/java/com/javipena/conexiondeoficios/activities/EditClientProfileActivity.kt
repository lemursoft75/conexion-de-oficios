package com.javipena.conexiondeoficios.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.javipena.conexiondeoficios.R

class EditClientProfileActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editLastname: EditText
    private lateinit var editPhone: EditText
    private lateinit var editEmail: EditText
    private lateinit var btnSave: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_client_profile)
        title = "Editar Perfil de Cliente"

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance()

        editName = findViewById(R.id.edit_client_name)
        editLastname = findViewById(R.id.edit_client_lastname)
        editPhone = findViewById(R.id.edit_client_phone)
        editEmail = findViewById(R.id.edit_client_email)
        btnSave = findViewById(R.id.btn_save_client_profile)

        loadClientProfile()

        btnSave.setOnClickListener {
            saveClientProfile()
        }
    }

    private fun loadClientProfile() {
        val user = auth.currentUser ?: return
        editEmail.setText(user.email)

        dbRef.getReference("Users").child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                editName.setText(snapshot.child("name").getValue(String::class.java))
                editLastname.setText(snapshot.child("lastname").getValue(String::class.java))
                editPhone.setText(snapshot.child("phone").getValue(String::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditClientProfileActivity, "Error al cargar el perfil.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveClientProfile() {
        val user = auth.currentUser ?: return
        val newEmail = editEmail.text.toString().trim()

        if (newEmail.isNotEmpty() && newEmail != user.email) {
            user.updateEmail(newEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateClientDataInDatabase(newEmail)
                } else {
                    Toast.makeText(this, "Error al actualizar correo: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            updateClientDataInDatabase(user.email!!)
        }
    }

    private fun updateClientDataInDatabase(email: String) {
        val userId = auth.currentUser?.uid ?: return

        val updatesMap = mapOf<String, Any>(
            "name" to editName.text.toString().trim(),
            "lastname" to editLastname.text.toString().trim(),
            "phone" to editPhone.text.toString().trim(),
            "email" to email
        )

        dbRef.getReference("Users").child(userId).updateChildren(updatesMap)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Error al actualizar los datos del perfil", Toast.LENGTH_SHORT).show()
            }
    }
}