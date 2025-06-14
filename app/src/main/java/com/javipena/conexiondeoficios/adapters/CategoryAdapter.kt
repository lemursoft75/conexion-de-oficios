package com.javipena.conexiondeoficios.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.javipena.conexiondeoficios.R



class CategoryAdapter(
    private val categories: List<CategoryItem>,
    private val onClick: (CategoryItem) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // 2. ViewHolder ahora hace referencia a las vistas del nuevo layout (icono y texto)
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryIcon: ImageView = itemView.findViewById(R.id.icon_category)
        val categoryName: TextView = itemView.findViewById(R.id.text_category_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        // Asegúrate de que el layout que se infla es el correcto: "item_category.xml"
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryItem = categories[position]

        // 3. Asignamos el texto y el icono a las vistas correspondientes
        holder.categoryName.text = categoryItem.name
        holder.categoryIcon.setImageResource(categoryItem.iconResId)

        // 4. Hacemos que toda la tarjeta sea clickeable
        holder.itemView.setOnClickListener {
            onClick(categoryItem)
        }
    }

    override fun getItemCount() = categories.size
}