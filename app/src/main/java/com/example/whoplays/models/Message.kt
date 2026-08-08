package com.example.whoplays.models

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderProfileImageUrl: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
