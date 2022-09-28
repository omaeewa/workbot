package com.example.workbot.domain.model

data class Message(
    var id: Long = 0,
    var fromChatId: String,
    var messageId: Int,
)