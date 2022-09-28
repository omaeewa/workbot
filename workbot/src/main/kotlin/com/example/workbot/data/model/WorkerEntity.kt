package com.example.workbot.data.model

import com.example.workbot.domain.model.Worker
import javax.persistence.*

@Entity
@Table(name = "worker")
class WorkerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "chat_id")
    var chatId: String,
    var username: String,
    var position: String,
    var state: String
)

fun WorkerEntity.toDomainModel() = Worker(id, chatId, username, position, state)

fun Iterable<WorkerEntity>.toDomainModel() = this.map { it.toDomainModel() }

fun Worker.toEntity() = WorkerEntity(id, chatId, username, position, state)