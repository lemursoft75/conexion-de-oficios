package com.javipena.conexiondeoficios.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.javipena.conexiondeoficios.Ad
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.activities.ContractorDetailActivity

/**
 * Este Adapter conecta la lista de objetos 'Ad' con el 'RecyclerView'.
 * Para cada 'Ad' en la lista, crea una vista (un 'item') y la llena con los datos.
 */
class AdsAdapter(private val adsList: List<Ad>) : RecyclerView.Adapter<AdsAdapter.AdViewHolder>() {

    /**
     * Esta clase interna representa la vista de un único item en la lista.
     * Contiene las referencias a los elementos de la UI del layout del item (ej. TextViews).
     */
    class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val adTextView: TextView = itemView.findViewById(R.id.text_ad_content)
        val phoneTextView: TextView = itemView.findViewById(R.id.text_ad_phone)
        val specialtyTextView: TextView = itemView.findViewById(R.id.text_ad_specialty)
    }

    /**
     * Se llama cuando el RecyclerView necesita crear una nueva vista de item.
     * 'Infla' (crea) el layout XML para un item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_ad, parent, false)
        return AdViewHolder(view)
    }

    /**
     * Se llama cuando el RecyclerView necesita mostrar los datos en un item específico.
     * Toma el objeto 'Ad' de la posición correspondiente y pone sus datos en las vistas.
     */
    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        val ad = adsList[position]

        // Poner los datos del anuncio en los TextViews del item.
        holder.adTextView.text = ad.adText
        holder.phoneTextView.text = "Contacto: ${ad.phone}"
        holder.specialtyTextView.text = ad.specialty

        // Configurar el click listener para todo el item.
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ContractorDetailActivity::class.java).apply {
                // Pasamos el objeto 'Ad' completo a la pantalla de detalle.
                putExtra("AD_DETAIL", ad)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Devuelve el número total de items en la lista de datos.
     */
    override fun getItemCount(): Int {
        return adsList.size
    }
}