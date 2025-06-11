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

        // --- AQUÍ VA TU LÓGICA ---
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Mostrar el botón de borrar solo si el usuario actual es el autor de la reseña
        if (review.clientId == currentUserId) {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                // Lógica para borrar la reseña
                val reviewRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(contractorId) // ID del contratista al que pertenece la reseña
                    .child("reviews")
                    .child(reviewId)     // ID de la reseña específica a borrar

                reviewRef.removeValue().addOnSuccessListener {
                    // Elimina el item de la lista local y notifica al adaptador
                    reviewList.removeAt(holder.adapterPosition)
                    notifyItemRemoved(holder.adapterPosition)
                }
            }
        } else {
            holder.deleteButton.visibility = View.GONE
        }
    }

    override fun getItemCount() = reviewList.size
}