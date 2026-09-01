package com.rikky.blankct.data

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val type: String = "text",
    val text: String = "",
    val imageBase64: String = "",
    val timestamp: Long = 0L
)

data class ChatSummary(
    val chatId: String = "",
    val isGroup: Boolean = false,
    val title: String = "",
    val iconBase64: String = "",
    val memberIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L
)
