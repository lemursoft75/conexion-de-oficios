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
    private val onDeleteClick: (String, Ad, Int) -> Unit
) : RecyclerView.Adapter<MyAdsAdapter.MyAdViewHolder>() {

    class MyAdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val adTextView: TextView = itemView.findViewById(R.id.text_ad_content_item)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete_ad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_my_ad, parent, false)
        return MyAdViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyAdViewHolder, position: Int) {
        val (adId, ad) = adsList[position]
        holder.adTextView.text = ad.adText

        holder.deleteButton.setOnClickListener {
            onDeleteClick(adId, ad, position)
        }
    }

    override fun getItemCount() = adsList.size
}