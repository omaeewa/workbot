package com.example.workbot.domain.model

data class Worker(
    var id: Long = 0,
    var chatId: String,
    var username: String,
    var position: String,
    var state: String
)