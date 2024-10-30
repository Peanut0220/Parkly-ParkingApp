package com.example.parkly.data

data class ChatMessage(
    val id: String = "",
    val senderID: String = "",
    val message: String = "",
    val sendTime: Long = 0
)
