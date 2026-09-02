package com.rikky.blankct

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ValueEventListener
import com.rikky.blankct.data.*
import com.rikky.blankct.ui.chat.MessageAdapter
import java.io.File

class ChatActivity : AppCompatActivity() {

    private var messagesListener: ValueEventListener? = null
    private lateinit var chatId: String
    private var otherUid: String = ""
    private var chatTitle: String = "Chat"
    private lateinit var adapter: MessageAdapter
    private lateinit var rv: RecyclerView
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false
    private var player: MediaPlayer? = null

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
        chatTitle = intent.getStringExtra("chatTitle") ?: "Chat"
        otherUid = intent.getStringExtra("otherUid") ?: ""

        val tvTitle = findViewById<TextView>(R.id.tv_chat_header_title)
        val ivIcon = findViewById<ImageView>(R.id.iv_chat_header_icon)
        val etMessage = findViewById<EditText>(R.id.et_message)
        val btnSend = findViewById<ImageButton>(R.id.btn_send)
        val btnAttach = findViewById<ImageButton>(R.id.btn_attach)
        val btnCall = findViewById<ImageButton>(R.id.btn_call)
        val btnMic = findViewById<ImageButton>(R.id.btn_mic)
        rv = findViewById(R.id.rv_messages)

        tvTitle.text = chatTitle

        if (otherUid.isNotEmpty()) {
            ChatRepository.listenToUserProfile(otherUid) { profile ->
                runOnUiThread {
                    if (profile.displayName.isNotEmpty()) tvTitle.text = profile.displayName
                    if (profile.iconBase64.isNotEmpty()) {
                        ivIcon.setImageBitmap(ImageUtils.base64ToBitmap(profile.iconBase64))
                    }
                }
            }
        }

        val myUid = AuthManager.currentUid() ?: return

        adapter = MessageAdapter(emptyList(), myUid) { audioBase64 ->
            playAudio(audioBase64)
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        messagesListener = ChatRepository.listenToMessages(chatId) { messages ->
            runOnUiThread {
                adapter.updateMessages(messages)
                if (messages.isNotEmpty()) rv.scrollToPosition(messages.size - 1)
                for (m in messages) {
                    if (m.senderId != myUid && m.seenBy[myUid] != true) {
                        ChatRepository.markMessageSeen(chatId, m.id, myUid)
                    }
                }
            }
        }

        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val empty = s.toString().trim().isEmpty()
                btnSend.visibility = if (empty) android.view.View.GONE else android.view.View.VISIBLE
                btnMic.visibility = if (empty) android.view.View.VISIBLE else android.view.View.GONE
            }
        })
        btnMic.visibility = android.view.View.VISIBLE
        btnSend.visibility = android.view.View.GONE

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                ChatRepository.sendTextMessage(chatId, myUid, text)
                etMessage.setText("")
            }
        }

        btnAttach.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(i)
        }

        btnCall.setOnClickListener {
            if (otherUid.isEmpty()) {
                android.widget.Toast.makeText(this, "Cannot call in this chat", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                val callId = "call_${System.currentTimeMillis()}"
                val callIntent = Intent(this, CallActivity::class.java)
                callIntent.putExtra("callId", callId)
                callIntent.putExtra("otherUid", otherUid)
                callIntent.putExtra("otherName", chatTitle)
                callIntent.putExtra("isCaller", true)
                startActivity(callIntent)
            }
        }

        btnMic.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> startRecording()
                android.view.MotionEvent.ACTION_UP -> stopRecordingAndSend(myUid)
            }
            true
        }
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
            return
        }
        try {
            audioFile = File(cacheDir, "voice_${System.currentTimeMillis()}.3gp")
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile!!.absolutePath)
                prepare()
                start()
            }
            isRecording = true
        } catch (e: Exception) {
            isRecording = false
        }
    }

    private fun stopRecordingAndSend(myUid: String) {
        if (!isRecording) return
        try {
            recorder?.stop()
            recorder?.release()
            recorder = null
            isRecording = false
            audioFile?.let { file ->
                val base64 = AudioUtils.fileToBase64(file)
                if (base64 != null) {
                    ChatRepository.sendAudioMessage(chatId, myUid, base64)
                }
                file.delete()
            }
        } catch (e: Exception) {
            isRecording = false
        }
    }

    private fun playAudio(base64: String) {
        try {
            player?.release()
            val tempFile = File(cacheDir, "play_${System.currentTimeMillis()}.3gp")
            AudioUtils.base64ToFile(base64, tempFile)
            player = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListener?.let { ChatRepository.stopListening(chatId, it) }
        player?.release()
    }
}
