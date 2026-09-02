package com.rikky.blankct.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rikky.blankct.R
import com.rikky.blankct.data.ChatMessage
import com.rikky.blankct.data.ImageUtils
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private var messages: List<ChatMessage>,
    private val myUid: String,
    private val onPlayAudio: ((String) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_SENT = 1
        const val TYPE_RECEIVED = 2
    }

    class SentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.tv_msg_text)
        val image: ImageView = view.findViewById(R.id.iv_msg_image)
        val time: TextView = view.findViewById(R.id.tv_msg_time)
        val tick: TextView = view.findViewById(R.id.tv_msg_tick)
        val audioLayout: LinearLayout = view.findViewById(R.id.layout_audio_msg)
        val playAudio: ImageView = view.findViewById(R.id.iv_play_audio)
    }

    class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.tv_msg_text)
        val image: ImageView = view.findViewById(R.id.iv_msg_image)
        val time: TextView = view.findViewById(R.id.tv_msg_time)
        val audioLayout: LinearLayout = view.findViewById(R.id.layout_audio_msg)
        val playAudio: ImageView = view.findViewById(R.id.iv_play_audio)
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

    private fun formatTime(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]

        fun bindContent(textView: TextView, imageView: ImageView, audioLayout: LinearLayout, playAudio: ImageView, timeView: TextView) {
            textView.visibility = View.GONE
            imageView.visibility = View.GONE
            audioLayout.visibility = View.GONE

            when (msg.type) {
                "image" -> {
                    imageView.visibility = View.VISIBLE
                    val bitmap = ImageUtils.base64ToBitmap(msg.imageBase64)
                    if (bitmap != null) imageView.setImageBitmap(bitmap)
                }
                "audio" -> {
                    audioLayout.visibility = View.VISIBLE
                    playAudio.setOnClickListener { onPlayAudio?.invoke(msg.audioBase64) }
                }
                else -> {
                    textView.visibility = View.VISIBLE
                    textView.text = msg.text
                }
            }
            timeView.text = formatTime(msg.timestamp)
        }

        if (holder is SentViewHolder) {
            bindContent(holder.text, holder.image, holder.audioLayout, holder.playAudio, holder.time)
            val seenCount = msg.seenBy.keys.count { it != myUid }
            holder.tick.text = if (seenCount > 0) "✓✓" else "✓"
            holder.tick.setTextColor(if (seenCount > 0) 0xFF25F4EE.toInt() else 0xDDFFFFFF.toInt())
        } else if (holder is ReceivedViewHolder) {
            bindContent(holder.text, holder.image, holder.audioLayout, holder.playAudio, holder.time)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }
}
