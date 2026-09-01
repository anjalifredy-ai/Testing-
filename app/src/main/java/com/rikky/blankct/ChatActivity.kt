package com.rikky.blankct

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ValueEventListener
import com.rikky.blankct.data.AuthManager
import com.rikky.blankct.data.ChatRepository
import com.rikky.blankct.data.ImageUtils
import com.rikky.blankct.ui.chat.MessageAdapter

class ChatActivity : AppCompatActivity() {

    private var messagesListener: ValueEventListener? = null
    private lateinit var chatId: String
    private lateinit var adapter: MessageAdapter
    private lateinit var rv: RecyclerView

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                val base64 = ImageUtils.uriToCompressedBase64(this, uri)
                val myUid = AuthManager.currentUid()
                if (base64 != null && myUid != null) {
                    ChatRepository.sendImageMessage(chatId, myUid, base64)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatId = intent.getStringExtra("chatId") ?: ""
        val chatTitle = intent.getStringExtra("chatTitle") ?: "Chat"

        val tvTitle = findViewById<TextView>(R.id.tv_chat_header_title)
        val etMessage = findViewById<EditText>(R.id.et_message)
        val btnSend = findViewById<ImageButton>(R.id.btn_send)
        val btnAttach = findViewById<ImageButton>(R.id.btn_attach)
        rv = findViewById(R.id.rv_messages)

        tvTitle.text = chatTitle

        val myUid = AuthManager.currentUid() ?: return

        adapter = MessageAdapter(emptyList(), myUid)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        messagesListener = ChatRepository.listenToMessages(chatId) { messages ->
            runOnUiThread {
                adapter.updateMessages(messages)
                if (messages.isNotEmpty()) rv.scrollToPosition(messages.size - 1)
            }
        }

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                ChatRepository.sendTextMessage(chatId, myUid, text)
                etMessage.setText("")
            }
        }

        btnAttach.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListener?.let { ChatRepository.stopListening(chatId, it) }
    }
}
