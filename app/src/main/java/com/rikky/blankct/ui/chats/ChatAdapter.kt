package com.rikky.blankct.ui.chats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rikky.blankct.R
import com.rikky.blankct.data.ChatSummary
import com.rikky.blankct.data.ImageUtils

class ChatAdapter(
    private var chats: List<ChatSummary>,
    private val onChatClick: (ChatSummary) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.iv_chat_icon)
        val title: TextView = view.findViewById(R.id.tv_chat_title)
        val lastMsg: TextView = view.findViewById(R.id.tv_chat_last_msg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_row, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.title.text = if (chat.title.isNotEmpty()) chat.title else "Chat"
        holder.lastMsg.text = chat.lastMessage.ifEmpty { "No messages yet" }

        if (chat.iconBase64.isNotEmpty()) {
            val bitmap = ImageUtils.base64ToBitmap(chat.iconBase64)
            if (bitmap != null) holder.icon.setImageBitmap(bitmap)
        } else {
            holder.icon.setImageDrawable(null)
        }

        holder.itemView.setOnClickListener { onChatClick(chat) }
    }

    override fun getItemCount(): Int = chats.size

    fun updateChats(newChats: List<ChatSummary>) {
        chats = newChats
        notifyDataSetChanged()
    }
}
