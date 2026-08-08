package com.example.whoplays.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whoplays.R
import com.example.whoplays.models.User

class ParticipantAdapter(private val participants: List<User>) :
    RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder>() {

    class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ImageView = itemView.findViewById(R.id.ivParticipantProfile)
        val tvName: TextView = itemView.findViewById(R.id.tvParticipantName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participant, parent, false)
        return ParticipantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val user = participants[position]
        val fullName = "${user.firstName} ${user.lastName}".trim()
        val displayName = if (fullName.isNotEmpty()) "$fullName, ${user.age}" else user.email.substringBefore("@")
        holder.tvName.text = displayName
        
        Glide.with(holder.itemView.context)
            .load(user.profileImageUrl)
            .placeholder(android.R.drawable.ic_menu_report_image)
            .circleCrop()
            .into(holder.ivProfile)
    }

    override fun getItemCount() = participants.size
}
