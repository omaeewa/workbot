package com.example.workbot.data.model

import com.example.workbot.domain.model.BuyerOrder
import com.example.workbot.domain.model.Worker
import javax.persistence.*

@Entity
@Table(name = "buyer_order")
class BuyerOrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "message_id")
    var messageId: Long,
    @Column(name = "buyer_id")
    var buyerId: Long,
)

fun BuyerOrderEntity.toDomainModel() = BuyerOrder(id, messageId, buyerId)

fun Iterable<BuyerOrderEntity>.toDomainModel() = this.map { it.toDomainModel() }

fun BuyerOrder.toEntity() = BuyerOrderEntity(id, messageId, buyerId)