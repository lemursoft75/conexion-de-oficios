package com.javipena.conexiondeoficios.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
// --- IMPORTACIONES PARA EL VIGILANTE ---
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
// ---------------------------------------
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.adapters.CategoryAdapter
import com.javipena.conexiondeoficios.adapters.CategoryItem

class DirectoryActivity : AppCompatActivity() {

    private var paymentsRef: com.google.firebase.database.DatabaseReference? = null
    private var paymentsListener: com.google.firebase.database.ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_directory)

        // Configuración de la Toolbar personalizada
        val toolbar: Toolbar = findViewById(R.id.toolbar_directory)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // --- LÓGICA DE CATEGORÍAS ---
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_categories)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        val categories = listOf(
            CategoryItem("Albañil", R.drawable.ic_construction),
            CategoryItem("Electricista", R.drawable.ic_bolt),
            CategoryItem("Plomero", R.drawable.ic_plumbing),
            CategoryItem("Carpintero", R.drawable.ic_carpenter),
            CategoryItem("Cerrajero", R.drawable.ic_cerrajeria),
            CategoryItem("Mecánico", R.drawable.ic_mechanic),
            CategoryItem("Refrigeración y Lavadoras", R.drawable.ic_ac_unit),
            CategoryItem("PC y Celulares", R.drawable.ic_computer),
            CategoryItem("Herrero", R.drawable.ic_welding),
            CategoryItem("Limpieza de hogar", R.drawable.ic_cleaning),
            CategoryItem("Jardinero", R.drawable.ic_gardening),
            CategoryItem("Agente inmobiliario", R.drawable.ic_real_estate),
            CategoryItem("Médico", R.drawable.ic_medical),
            CategoryItem("Pintor", R.drawable.ic_painter),
            CategoryItem("Músico", R.drawable.ic_music),
            CategoryItem("Animación para eventos", R.drawable.ic_celebration),
            CategoryItem("Otro", R.drawable.ic_more)
        )

        val categoryAdapter = CategoryAdapter(categories) { categoryItem ->
            val intent = Intent(this, ContractorListActivity::class.java)
            intent.putExtra("CATEGORY_NAME", categoryItem.name)
            startActivity(intent)
        }
        recyclerView.adapter = categoryAdapter

        // --- BOTÓN BÚSQUEDA CERCANA ---
        val btnFindNearby = findViewById<Button>(R.id.btn_find_nearby)
        btnFindNearby.setOnClickListener {
            startActivity(Intent(this, NearbyAdsActivity::class.java))
        }

        // --- 📌 NUEVA LÓGICA: BOTÓN MOSTRAR QR DE CLIENTE ---
        val btnShowQr = findViewById<Button>(R.id.btn_open_qr_dialog)
        btnShowQr.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                showQrDialog(uid)
            } else {
                Toast.makeText(this, "Debes iniciar sesión para ver tu QR", Toast.LENGTH_SHORT).show()
            }
        }

        // --- 🚨 VIGILANTE DE SERVICIOS FINALIZADOS ---
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            listenForPayments(currentUserId)
        }

        // --- AVISO DE RESPONSABILIDAD ---
        val textDisclaimer = findViewById<TextView>(R.id.text_disclaimer)
        textDisclaimer.setOnClickListener {
            showDisclaimer() // <--- Antes decía showDisclaimerDialog(), cámbialo a showDisclaimer()
        }

        // --- BOTÓN CHATBOT ---
        val btnChat = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btn_chatbot)
        btnChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }

    /**
     * Escucha cambios en CompletedServices para mostrar el aviso al cliente
     */
    private fun listenForPayments(userId: String) {
        paymentsRef = FirebaseDatabase.getInstance().getReference("CompletedServices")

        paymentsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return
                for (contractorSnapshot in snapshot.children) {
                    val contractorId = contractorSnapshot.key
                    if (contractorSnapshot.hasChild(userId)) {
                        val serviceData = contractorSnapshot.child(userId)
                        val status = serviceData.child("status").getValue(String::class.java)
                        val isPending = serviceData.child("payment_pending").getValue(Boolean::class.java) ?: false

                        if (status == "completed" && isPending && contractorId != null) {
                            runOnUiThread { showPaymentAlert(contractorId) }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Este es el error que ves en el Toast actual
                // Si el error es por falta de permisos tras cerrar sesión, lo ignoramos
                if (!error.message.contains("Permission denied", ignoreCase = true)) {
                    Toast.makeText(this@DirectoryActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Activar el listener
        paymentsRef?.addValueEventListener(paymentsListener!!)
    }

    // 1. Declara la variable de la alerta arriba en la clase
    private var paymentDialog: AlertDialog? = null

    // 2. Modifica la función showPaymentAlert
    private fun showPaymentAlert(contractorId: String) {
        if (isFinishing || (paymentDialog != null && paymentDialog!!.isShowing)) return

        val builder = AlertDialog.Builder(this)
        builder.setTitle("¡Servicio Finalizado!")
        builder.setMessage("El contratista ha confirmado el trabajo. ¿Deseas ir al perfil para dejar tu calificación?")

        builder.setPositiveButton("Ir a Calificar") { dialog, _ ->
            // CERRAR LA ALERTA ANTES DE NAVEGAR
            dialog.dismiss()
            paymentDialog = null

            val intent = Intent(this, ContractorDetailActivity::class.java)
            intent.putExtra("CONTRACTOR_ID", contractorId)
            startActivity(intent)
        }

        builder.setNegativeButton("Más tarde") { dialog, _ ->
            dialog.dismiss()
            paymentDialog = null
        }

        builder.setCancelable(false)
        paymentDialog = builder.create()
        paymentDialog?.show()
    }

    /**
     * Genera el QR y lo muestra en un diálogo flotante
     */
    private fun showQrDialog(userId: String) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_user_qr, null)
        builder.setView(view)

        val dialog = builder.create()
        val imgQr = view.findViewById<ImageView>(R.id.img_qr_placeholder)
        val btnClose = view.findViewById<Button>(R.id.btn_close_qr)

        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(userId, BarcodeFormat.QR_CODE, 500, 500)
            imgQr.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al generar código QR", Toast.LENGTH_SHORT).show()
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showDisclaimer() {
        val message = "\"Conexión de Oficios\" es una plataforma de enlace que facilita la conexión entre clientes y proveedores de servicios (contratistas). La aplicación no garantiza la calidad, seguridad o legalidad de los servicios prestados. Cualquier acuerdo, pago o disputa es responsabilidad exclusiva de las partes involucradas. Recomendamos verificar referencias antes de contratar."

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Aviso de Responsabilidad")
        builder.setMessage(message)
        builder.setPositiveButton("Entendido") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

        // --- ESTO ES LO QUE ELIMINA LOS PUNTOS SUSPENSIVOS ---
        val textView = dialog.findViewById<TextView>(android.R.id.message)
        textView?.let {
            it.maxLines = 100         // Permitimos hasta 100 líneas si es necesario
            it.ellipsize = null       // Desactiva los puntos suspensivos
            it.isSingleLine = false   // Fuerza a que no sea una sola línea
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.directory_menu, menu)

        // Opcional: Forzar color negro a los iconos si tampoco se ven
        if (menu != null) {
            for (i in 0 until menu.size()) {
                val item = menu.getItem(i)
                val icon = item.icon
                if (icon != null) {
                    icon.setTint(android.graphics.Color.BLACK)
                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_edit_profile -> {
                startActivity(Intent(this, EditClientProfileActivity::class.java))
                true
            }
            R.id.menu_logout -> {
                paymentsListener?.let { paymentsRef?.removeEventListener(it) }
                FirebaseAuth.getInstance().signOut()


                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        paymentsListener?.let { paymentsRef?.removeEventListener(it) }
    }
}