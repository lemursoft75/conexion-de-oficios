package com.javipena.conexiondeoficios.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.adapters.ContractorAdapter
import com.javipena.conexiondeoficios.models.Contractor

class RegisterContractorActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var contractorList: ArrayList<Contractor>
    private lateinit var adapter: ContractorAdapter
    private lateinit var database: DatabaseReference
    private lateinit var btnRegister: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_contractor)

        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recycler_contractors) // 📌 Corrección aquí
        recyclerView.layoutManager = LinearLayoutManager(this)
        contractorList = ArrayList()
        adapter = ContractorAdapter(contractorList)
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance().getReference("Contractors")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contractorList.clear()
                for (contractorSnapshot in snapshot.children) {
                    val contractor = contractorSnapshot.getValue(Contractor::class.java)
                    contractor?.let { contractorList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegisterContractorActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
        })

        btnRegister = findViewById(R.id.btn_register)
        btnRegister.setOnClickListener {
            registerContractor()
        }
    }

    private fun registerContractor() {
        val name = findViewById<EditText>(R.id.edit_name).text.toString()
        val lastname = findViewById<EditText>(R.id.edit_lastname).text.toString()
        val phone = findViewById<EditText>(R.id.edit_phone).text.toString()
        val email = findViewById<EditText>(R.id.edit_email).text.toString()
        val password = findViewById<EditText>(R.id.edit_password).text.toString()
        val companyName = findViewById<EditText>(R.id.edit_company).text.toString()
        val specialty = findViewById<Spinner>(R.id.spinner_specialty).selectedItem.toString()
        val rfc = findViewById<EditText>(R.id.edit_rfc).text.toString()
        val latitude = findViewById<EditText>(R.id.edit_latitude).text.toString()
        val longitude = findViewById<EditText>(R.id.edit_longitude).text.toString()
        val secretQuestion = findViewById<EditText>(R.id.edit_secret_question).text.toString()
        val secretAnswer = findViewById<EditText>(R.id.edit_secret_answer).text.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || secretQuestion.isEmpty() || secretAnswer.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInAnonymously().addOnSuccessListener { result ->
            val userId = result.user?.uid

            val contractorData = hashMapOf(
                "name" to name,
                "lastname" to lastname,
                "phone" to phone,
                "email" to email,
                "password" to password,
                "companyName" to companyName,
                "specialty" to specialty,
                "rfc" to rfc,
                "latitude" to latitude,
                "longitude" to longitude,
                "secretQuestion" to secretQuestion,
                "secretAnswer" to secretAnswer
            )

            FirebaseDatabase.getInstance().getReference("Contractors")
                .child(userId!!)
                .setValue(contractorData)
                .addOnSuccessListener {
                    Toast.makeText(this, "✅ Registro exitoso", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "❌ Error al registrar", Toast.LENGTH_LONG).show()
                }
        }.addOnFailureListener {
            Toast.makeText(this, "❌ Error al generar sesión anónima", Toast.LENGTH_LONG).show()
        }
    }
}
