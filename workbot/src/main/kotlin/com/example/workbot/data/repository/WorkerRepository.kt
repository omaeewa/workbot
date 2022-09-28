package com.example.workbot.data.repository

import com.example.workbot.data.model.WorkerEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param


interface WorkerRepository : CrudRepository<WorkerEntity, Long> {
    @Query("SELECT we FROM WorkerEntity we WHERE we.chatId = :chatId")
    fun get(@Param("chatId") chatId: String): WorkerEntity?
}