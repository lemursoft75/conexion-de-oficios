package com.javipena.conexiondeoficios.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.javipena.conexiondeoficios.Ad
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.activities.ContractorDetailActivity

class AdsAdapter(private val adsList: List<Ad>) :
    RecyclerView.Adapter<AdsAdapter.AdViewHolder>() {

    class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val adTextView: TextView = itemView.findViewById(R.id.text_ad_content)
        val phoneTextView: TextView = itemView.findViewById(R.id.text_ad_phone)
        val specialtyTextView: TextView = itemView.findViewById(R.id.text_ad_specialty)
        val adImageView: ImageView = itemView.findViewById(R.id.image_ad_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_ad, parent, false)
        return AdViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        val ad = adsList[position]

        holder.adTextView.text = ad.adText

        // ----------------------------------------------
        // 🔥 Aquí viene la magia: cargar datos del usuario en tiempo real
        // ----------------------------------------------
        val userRef =
            FirebaseDatabase.getInstance().getReference("Users").child(ad.contractorId)

        userRef.get().addOnSuccessListener { snapshot ->
            val phone = snapshot.child("phone").value?.toString() ?: "Sin teléfono"

            // Usar directamente la especialidad del anuncio si ya existe
            val specialtyFromAd = ad.specialty
            val specialtyFromUser = snapshot.child("specialty").value?.toString()

            val specialtyToShow = when {
                !specialtyFromAd.isNullOrEmpty() -> specialtyFromAd
                !specialtyFromUser.isNullOrEmpty() -> specialtyFromUser
                else -> "Sin especialidad"
            }

            holder.phoneTextView.text = "Contacto: $phone"
            holder.specialtyTextView.text = specialtyToShow
        }


        // 📌 Imagen del anuncio
        if (!ad.mediaUrl.isNullOrEmpty()) {
            holder.adImageView.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(ad.mediaUrl)
                .into(holder.adImageView)
        } else {
            holder.adImageView.visibility = View.GONE
        }

        // 📌 Navegar al detalle del contratista
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ContractorDetailActivity::class.java).apply {
                putExtra("AD_DETAIL", ad)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = adsList.size
}
