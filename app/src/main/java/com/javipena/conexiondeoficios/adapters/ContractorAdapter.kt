package com.javipena.conexiondeoficios.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.models.Contractor

class ContractorAdapter(private val contractorList: ArrayList<Contractor>) :
    RecyclerView.Adapter<ContractorAdapter.ContractorViewHolder>() {

    class ContractorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.text_name)
        val specialtyText: TextView = itemView.findViewById(R.id.text_specialty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contractor, parent, false)
        return ContractorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContractorViewHolder, position: Int) {
        val contractor = contractorList[position]
        holder.nameText.text = contractor.name
        holder.specialtyText.text = contractor.specialty
    }

    override fun getItemCount(): Int = contractorList.size
}
