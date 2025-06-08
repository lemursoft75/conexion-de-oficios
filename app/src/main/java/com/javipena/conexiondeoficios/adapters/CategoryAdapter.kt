package com.javipena.conexiondeoficios.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView // 📌 IMPORTACIÓN AÑADIDA
import androidx.recyclerview.widget.RecyclerView
import com.javipena.conexiondeoficios.R

class CategoryAdapter(
    private val categories: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // 📌 CAMBIO 1: Añadimos la referencia al TextView
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.text_category_name)
        val viewButton: Button = itemView.findViewById(R.id.btn_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        // 📌 CAMBIO 2: Ahora ponemos el nombre de la categoría en el TextView
        holder.categoryName.text = category

        // El botón ahora puede tener un texto genérico o también el de la categoría
        // holder.viewButton.text = "Ver"

        // 📌 CAMBIO 3: Hacemos que toda la tarjeta sea clickeable
        holder.itemView.setOnClickListener { onClick(category) }
    }

    override fun getItemCount() = categories.size
}