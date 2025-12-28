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

        // Si review.clientName contiene el correo en Firebase, aquí se mostrará.
        holder.clientName.text = review.clientName

        holder.ratingBar.rating = review.rating.toFloat()
        holder.comment.text = review.comment

        // Ocultar botón de borrar (según tu lógica actual)
        holder.deleteButton.visibility = View.GONE
    }

    override fun getItemCount() = reviewList.size
}