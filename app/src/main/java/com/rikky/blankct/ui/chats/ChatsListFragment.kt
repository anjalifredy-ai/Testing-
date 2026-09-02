package com.rikky.blankct.ui.chats

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ValueEventListener
import com.rikky.blankct.ChatActivity
import com.rikky.blankct.R
import com.rikky.blankct.data.AuthManager
import com.rikky.blankct.data.ChatRepository
import com.rikky.blankct.data.ChatSummary

class ChatsListFragment : Fragment(R.layout.fragment_chats_list) {

    private var chatsListener: ValueEventListener? = null
    private var myUid: String? = null
    private lateinit var adapter: ChatAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvId = view.findViewById<TextView>(R.id.tv_your_id_label)
        val btnCopy = view.findViewById<View>(R.id.btn_copy_id)
        val rv = view.findViewById<RecyclerView>(R.id.rv_chats)

        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChatAdapter(emptyList()) { chat: ChatSummary ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("chatId", chat.chatId)
            intent.putExtra("chatTitle", chat.title)
            if (!chat.isGroup) {
                val otherUid = chat.memberIds.firstOrNull { it != myUid } ?: ""
                intent.putExtra("otherUid", otherUid)
            }
            startActivity(intent)
        }
        rv.adapter = adapter

        var currentId = ""

        AuthManager.ensureSignedIn { shortId ->
            currentId = shortId
            myUid = AuthManager.currentUid()
            activity?.runOnUiThread {
                tvId.text = "Your ID: $shortId"
            }
            myUid?.let { uid ->
                chatsListener = ChatRepository.listenToMyChats(uid) { chats ->
                    activity?.runOnUiThread {
                        adapter.updateChats(chats)
                    }
                }
            }
        }

        btnCopy.setOnClickListener {
            if (currentId.isNotEmpty()) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("BLANK CT ID", currentId))
                Toast.makeText(requireContext(), "ID copied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chatsListener = null
    }
}
