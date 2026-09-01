package com.rikky.blankct.data

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

object ChatRepository {

    private val db = FirebaseDatabase.getInstance().reference

    fun findUidByShortId(shortId: String, onResult: (String?) -> Unit) {
        db.child("users").orderByChild("shortId").equalTo(shortId)
            .get()
            .addOnSuccessListener { snapshot ->
                val uid = snapshot.children.firstOrNull()?.key
                onResult(uid)
            }
            .addOnFailureListener { onResult(null) }
    }

    fun startDirectChat(myUid: String, otherUid: String, onResult: (String) -> Unit) {
        val chatId = if (myUid < otherUid) "dm_${myUid}_$otherUid" else "dm_${otherUid}_$myUid"
        val chatRef = db.child("chats").child(chatId)
        chatRef.child("isGroup").setValue(false)
        chatRef.child("members").child(myUid).setValue(true)
        chatRef.child("members").child(otherUid).setValue(true)
        onResult(chatId)
    }

    fun startSelfChat(myUid: String, onResult: (String) -> Unit) {
        val chatId = "self_$myUid"
        val chatRef = db.child("chats").child(chatId)
        chatRef.child("isGroup").setValue(false)
        chatRef.child("title").setValue("You")
        chatRef.child("members").child(myUid).setValue(true)
        onResult(chatId)
    }

    fun createGroup(myUid: String, title: String, iconBase64: String, onResult: (String) -> Unit) {
        val chatId = "group_${System.currentTimeMillis()}_$myUid"
        val chatRef = db.child("chats").child(chatId)
        chatRef.child("isGroup").setValue(true)
        chatRef.child("title").setValue(title)
        chatRef.child("iconBase64").setValue(iconBase64)
        chatRef.child("members").child(myUid).setValue(true)
        onResult(chatId)
    }

    fun sendTextMessage(chatId: String, senderId: String, text: String) {
        val msgId = db.child("chats").child(chatId).child("messages").push().key ?: return
        val message = ChatMessage(
            id = msgId,
            senderId = senderId,
            type = "text",
            text = text,
            timestamp = System.currentTimeMillis()
        )
        db.child("chats").child(chatId).child("messages").child(msgId).setValue(message)
        db.child("chats").child(chatId).child("lastMessage").setValue(text)
        db.child("chats").child(chatId).child("lastTimestamp").setValue(System.currentTimeMillis())
    }

    fun sendImageMessage(chatId: String, senderId: String, imageBase64: String) {
        val msgId = db.child("chats").child(chatId).child("messages").push().key ?: return
        val message = ChatMessage(
            id = msgId,
            senderId = senderId,
            type = "image",
            imageBase64 = imageBase64,
            timestamp = System.currentTimeMillis()
        )
        db.child("chats").child(chatId).child("messages").child(msgId).setValue(message)
        db.child("chats").child(chatId).child("lastMessage").setValue("📷 Photo")
        db.child("chats").child(chatId).child("lastTimestamp").setValue(System.currentTimeMillis())
    }

    fun listenToMessages(chatId: String, onMessages: (List<ChatMessage>) -> Unit): ValueEventListener {
        val ref = db.child("chats").child(chatId).child("messages")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }
                    .sortedBy { it.timestamp }
                onMessages(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        return listener
    }

    fun stopListening(chatId: String, listener: ValueEventListener) {
        db.child("chats").child(chatId).child("messages").removeEventListener(listener)
    }

    fun listenToMyChats(myUid: String, onChats: (List<ChatSummary>) -> Unit): ValueEventListener {
        val ref = db.child("chats")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatSummary>()
                for (child in snapshot.children) {
                    val members = child.child("members")
                    if (members.hasChild(myUid)) {
                        val memberIds = members.children.mapNotNull { it.key }
                        val summary = ChatSummary(
                            chatId = child.key ?: "",
                            isGroup = child.child("isGroup").getValue(Boolean::class.java) ?: false,
                            title = child.child("title").getValue(String::class.java) ?: "",
                            iconBase64 = child.child("iconBase64").getValue(String::class.java) ?: "",
                            memberIds = memberIds,
                            lastMessage = child.child("lastMessage").getValue(String::class.java) ?: "",
                            lastTimestamp = child.child("lastTimestamp").getValue(Long::class.java) ?: 0L
                        )
                        list.add(summary)
                    }
                }
                onChats(list.sortedByDescending { it.lastTimestamp })
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        return listener
    }
}
