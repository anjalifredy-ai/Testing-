package com.rikky.blankct.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rikky.blankct.R
import com.rikky.blankct.data.ChatMessage
import com.rikky.blankct.data.ImageUtils

class MessageAdapter(
    private var messages: List<ChatMessage>,
    private val myUid: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2
    }

    class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.tv_msg_text)
        val image: ImageView = view.findViewById(R.id.iv_msg_image)
    }

    class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.tv_msg_text)
        val image: ImageView = view.findViewById(R.id.iv_msg_image)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == myUid) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_SENT) {
            SentViewHolder(inflater.inflate(R.layout.item_message_sent, parent, false))
        } else {
            ReceivedViewHolder(inflater.inflate(R.layout.item_message_received, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]

        val textView: TextView
        val imageView: ImageView

        if (holder is SentViewHolder) {
            textView = holder.text
            imageView = holder.image
        } else {
            holder as ReceivedViewHolder
            textView = holder.text
            imageView = holder.image
        }

        if (msg.type == "image" && msg.imageBase64.isNotEmpty()) {
            textView.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            val bitmap = ImageUtils.base64ToBitmap(msg.imageBase64)
            if (bitmap != null) imageView.setImageBitmap(bitmap)
        } else {
            textView.visibility = View.VISIBLE
            imageView.visibility = View.GONE
            textView.text = msg.text
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}
