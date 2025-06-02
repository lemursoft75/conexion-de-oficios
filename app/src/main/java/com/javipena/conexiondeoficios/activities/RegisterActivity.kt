package com.javipena.conexiondeoficios.activities

import com.javipena.conexiondeoficios.activities.RegisterContractorActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.javipena.conexiondeoficios.R


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnClient = findViewById<Button>(R.id.btn_client)
        val btnContractor = findViewById<Button>(R.id.btn_contractor)

        btnClient.setOnClickListener {
            startActivity(Intent(this, RegisterClientActivity::class.java))
        }

        btnContractor.setOnClickListener {
            startActivity(Intent(this, RegisterContractorActivity::class.java))
        }
    }
}

