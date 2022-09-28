package com.example.workbot.domain.model

data class BuyerOrder(
    var id: Long = 0,
    var messageId: Long,
    var buyerId: Long,
)