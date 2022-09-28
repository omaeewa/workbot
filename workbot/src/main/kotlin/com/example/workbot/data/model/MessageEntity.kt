package com.example.workbot.data.model

import com.example.workbot.domain.model.BuyerOrder
import com.example.workbot.domain.model.Message
import com.example.workbot.domain.model.Worker
import javax.persistence.*

@Entity
@Table(name = "message")
class MessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "from_chat_id")
    var fromChatId: String,
    @Column(name = "message_id")
    var messageId: Int,
)

fun MessageEntity.toDomainModel() = Message(id, fromChatId, messageId)

fun Iterable<MessageEntity>.toDomainModel() = this.map { it.toDomainModel() }

fun Message.toEntity() = MessageEntity(id, fromChatId, messageId)