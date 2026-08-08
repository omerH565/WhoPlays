package com.example.whoplays.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whoplays.R
import com.example.whoplays.models.Message

class MessageAdapter(private var messages: List<Message>, private val currentUserId: String) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ImageView = itemView.findViewById(R.id.ivSenderProfile)
        val tvName: TextView = itemView.findViewById(R.id.tvSenderName)
        val tvText: TextView = itemView.findViewById(R.id.tvMessageText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.tvName.text = message.senderName
        holder.tvText.text = message.text

        if (message.senderId == "system") {
            holder.ivProfile.setImageResource(android.R.drawable.ic_menu_info_details)
            holder.tvName.visibility = View.GONE
            holder.itemView.setPadding(32, 8, 32, 8)
            holder.tvText.textAlignment = View.TEXT_ALIGNMENT_CENTER
            holder.tvText.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
        } else {
            holder.tvName.visibility = View.VISIBLE
            holder.tvText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.tvText.setTextColor(holder.itemView.context.getColor(android.R.color.black))
            
            Glide.with(holder.itemView.context)
                .load(message.senderProfileImageUrl)
                .placeholder(android.R.drawable.ic_menu_report_image)
                .circleCrop()
                .into(holder.ivProfile)

            if (message.senderId == currentUserId) {
                holder.itemView.setPadding(64, 8, 8, 8)
            } else {
                holder.itemView.setPadding(8, 8, 64, 8)
            }
        }
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}
