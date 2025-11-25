package com.javipena.conexiondeoficios.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.javipena.conexiondeoficios.Ad
import com.javipena.conexiondeoficios.R

class MyAdsAdapter(
    private val adsList: List<Pair<String, Ad>>,
    private val onDeleteClick: (String, Ad, Int) -> Unit,
    private val onEditClick: (String, Ad) -> Unit
) : RecyclerView.Adapter<MyAdsAdapter.MyAdViewHolder>() {

    class MyAdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textContent: TextView = view.findViewById(R.id.text_ad_content_item)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_ad)
        val btnEdit: ImageButton = view.findViewById(R.id.btn_edit_ad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_my_ad, parent, false)
        return MyAdViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyAdViewHolder, position: Int) {
        val (adId, ad) = adsList[position]

        holder.textContent.text = ad.adText

        holder.btnDelete.setOnClickListener {
            onDeleteClick(adId, ad, position)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(adId, ad)
        }
    }

    override fun getItemCount(): Int = adsList.size
}
