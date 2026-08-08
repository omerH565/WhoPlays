package com.example.whoplays.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whoplays.R
import com.example.whoplays.models.Game

class GameAdapter(
    private var games: List<Game>,
    private val currentUserId: String,
    private val onJoinClicked: (Game) -> Unit,
    private val onLeaveClicked: (Game) -> Unit,
    private val onParticipantsClicked: (Game) -> Unit,
    private val onChatClicked: (Game) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCourtImage: ImageView = itemView.findViewById(R.id.ivCourtImage)
        val tvSportType: TextView = itemView.findViewById(R.id.tvSportType)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvCapacity: TextView = itemView.findViewById(R.id.tvCapacity)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val btnJoin: Button = itemView.findViewById(R.id.btnJoin)
        val btnChat: Button = itemView.findViewById(R.id.btnChat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]

        holder.tvSportType.text = game.sportType
        holder.tvLocation.text = game.locationName
        holder.tvCapacity.text = "Players: ${game.currentPlayers}/${game.maxPlayers}"
        
        val format = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val dateStr = try {
            format.format(java.util.Date(game.dateTime.toLong()))
        } catch (e: Exception) {
            "No date"
        }
        holder.tvDateTime.text = dateStr

        if (game.courtImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(game.courtImageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivCourtImage)
        }

        // Show participants on capacity click
        holder.tvCapacity.setOnClickListener {
            onParticipantsClicked(game)
        }

        if (game.participantIds.contains(currentUserId)) {
            holder.btnJoin.text = "Leave"
            holder.btnJoin.isEnabled = true
            holder.btnJoin.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
            holder.btnJoin.setOnClickListener {
                onLeaveClicked(game)
            }
            holder.btnChat.visibility = View.VISIBLE
            holder.btnChat.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
            holder.btnChat.setOnClickListener {
                onChatClicked(game)
            }
        } else {
            holder.btnChat.visibility = View.GONE
            if (game.currentPlayers >= game.maxPlayers) {
                holder.btnJoin.text = "Full"
                holder.btnJoin.isEnabled = false
                holder.btnJoin.setBackgroundColor(holder.itemView.context.getColor(android.R.color.darker_gray))
            } else {
                holder.btnJoin.text = "Join"
                holder.btnJoin.isEnabled = true
                holder.btnJoin.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_blue_dark))
                holder.btnJoin.setOnClickListener {
                    onJoinClicked(game)
                }
            }
        }
    }

    override fun getItemCount(): Int = games.size

    fun updateData(newGames: List<Game>) {
        this.games = newGames
        notifyDataSetChanged()
    }
}