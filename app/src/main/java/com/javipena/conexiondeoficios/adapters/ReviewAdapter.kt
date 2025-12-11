package com.javipena.conexiondeoficios.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.javipena.conexiondeoficios.R
import com.javipena.conexiondeoficios.models.Review // Asegúrate de tener una data class Review

class ReviewAdapter(
    private val reviewList: MutableList<Pair<String, Review>>, // Pair de (reviewId, Review)
    private val contractorId: String
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clientName: TextView = itemView.findViewById(R.id.text_client_name)
        val ratingBar: RatingBar = itemView.findViewById(R.id.rating_bar_item)
        val comment: TextView = itemView.findViewById(R.id.text_comment)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete_review)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val (reviewId, review) = reviewList[position]

        holder.clientName.text = review.clientName
        holder.ratingBar.rating = review.rating.toFloat()
        holder.comment.text = review.comment

        // --- LÓGICA MODIFICADA ---

        // 🚨 Impedimos que el cliente elimine su reseña.
        // Si el botón está visible por defecto, lo ocultamos.
        // Si quieres que el ADMIN pueda borrar, aquí pondrías la lógica del Admin.

        // Por defecto, ocultamos el botón de borrar
        holder.deleteButton.visibility = View.GONE

        // Si quisieras que solo el administrador pueda borrar, la lógica sería:
        /*
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == "TU_ID_DE_ADMIN") { // Reemplaza con el ID de tu cuenta Admin
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                 // ... Lógica de borrado (mantienes el código anterior) ...
            }
        } else {
            holder.deleteButton.visibility = View.GONE
        }
        */
        // -------------------------
    }

    override fun getItemCount() = reviewList.size
}