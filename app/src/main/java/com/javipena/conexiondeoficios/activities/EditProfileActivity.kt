package com.javipena.conexiondeoficios.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.models.Contractor

class EditProfileActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editLastname: EditText
    private lateinit var editPhone: EditText
    private lateinit var editEmail: EditText
    private lateinit var editCompanyName: EditText
    private lateinit var spinnerSpecialty: Spinner
    private lateinit var editLatitude: EditText
    private lateinit var editLongitude: EditText

    // 🔥 NUEVOS CAMPOS
    private lateinit var switchEmergencies: Switch
    private lateinit var editDays: EditText
    private lateinit var editHoursFrom: EditText
    private lateinit var editHoursTo: EditText

    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("Users")

        setupViews()
        setupSpecialtySpinner()

        loadUserProfile()

        btnSave.setOnClickListener {
            updateUserDataInDatabase()
        }
    }

    private fun setupViews() {
        editName = findViewById(R.id.edit_profile_name)
        editLastname = findViewById(R.id.edit_profile_lastname)
        editPhone = findViewById(R.id.edit_profile_phone)
        editEmail = findViewById(R.id.edit_profile_email)
        editCompanyName = findViewById(R.id.edit_profile_company)
        spinnerSpecialty = findViewById(R.id.spinner_profile_specialty)
        editLatitude = findViewById(R.id.edit_profile_latitude)
        editLongitude = findViewById(R.id.edit_profile_longitude)

        switchEmergencies = findViewById(R.id.switch_edit_emergency)
        editDays = findViewById(R.id.edit_profile_days)
        editHoursFrom = findViewById(R.id.edit_profile_hours_from)
        editHoursTo = findViewById(R.id.edit_profile_hours_to)

        btnSave = findViewById(R.id.btn_save_profile)
        progressBar = findViewById(R.id.progress_bar_edit)
    }

    private fun setupSpecialtySpinner() {
        val specialties = listOf(
            "Albañil", "Electricista", "Plomero", "Carpintero", "Cerrajero", "Mecánico",
            "Técnico en refrigeración", "Técnico en computadoras", "Herrero", "Limpieza",
            "Jardinero", "Médico", "Otro"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecialty.adapter = adapter
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        progressBar.visibility = View.VISIBLE

        dbRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                progressBar.visibility = View.GONE

                if (!snapshot.exists()) return

                val contractor = snapshot.getValue(Contractor::class.java) ?: return

                editName.setText(contractor.name)
                editLastname.setText(contractor.lastname)
                editPhone.setText(contractor.phone)
                editEmail.setText(contractor.email)
                editCompanyName.setText(contractor.companyName)
                editLatitude.setText(contractor.latitude)
                editLongitude.setText(contractor.longitude)

                // 🔥 Nuevos campos
                switchEmergencies.isChecked = contractor.attendsEmergencies
                editDays.setText(contractor.attentionDays)
                editHoursFrom.setText(contractor.attentionFrom)
                editHoursTo.setText(contractor.attentionTo)

                // Spinner
                val position = (spinnerSpecialty.adapter as ArrayAdapter<String>)
                    .getPosition(contractor.specialty)
                spinnerSpecialty.setSelection(position)
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Log.e("EditProfile", "Error: ${error.message}")
            }
        })
    }

    private fun updateUserDataInDatabase() {
        val uid = auth.currentUser?.uid ?: return

        progressBar.visibility = View.VISIBLE

        val updates = mapOf(
            "name" to editName.text.toString(),
            "lastname" to editLastname.text.toString(),
            "phone" to editPhone.text.toString(),
            "email" to editEmail.text.toString(),
            "companyName" to editCompanyName.text.toString(),
            "specialty" to spinnerSpecialty.selectedItem.toString(),
            "latitude" to editLatitude.text.toString(),
            "longitude" to editLongitude.text.toString(),

            // 🔥 Nuevos
            "attendsEmergencies" to switchEmergencies.isChecked,
            "attentionDays" to editDays.text.toString(),
            "attentionFrom" to editHoursFrom.text.toString(),
            "attentionTo" to editHoursTo.text.toString()
        )

        dbRef.child(uid).updateChildren(updates).addOnCompleteListener {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
        }
    }
}
