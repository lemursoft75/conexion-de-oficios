package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.zxing.integration.android.IntentIntegrator
import com.javipena.conexiondeoficios.R

class ScannerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        val btnScan = findViewById<Button>(R.id.btn_start_scan)
        btnScan.setOnClickListener { initScanner() }
    }

    private fun initScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Encuadra el código QR del cliente para finalizar el servicio")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
            } else {
                // Limpiamos el UID por si trae espacios
                confirmService(result.contents.trim())
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun confirmService(clientUid: String) {
        val contractorUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("CompletedServices")

        val serviceInfo = mapOf(
            "status" to "completed",
            "payment_pending" to true,
            "timestamp" to ServerValue.TIMESTAMP
        )

        // Estructura: CompletedServices -> ID_Contratista -> ID_Cliente
        dbRef.child(contractorUid).child(clientUid).setValue(serviceInfo)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Servicio confirmado con éxito!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al confirmar: Verifica tu conexión o permisos", Toast.LENGTH_SHORT).show()
            }
    }
}